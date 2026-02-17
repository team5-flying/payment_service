package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.payment.service.PaymentService;
import com.bootcamp.paymentdemo.domain.refund.service.RefundService;
import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import com.bootcamp.paymentdemo.domain.subscription.entity.BillingStatus;
import com.bootcamp.paymentdemo.domain.subscription.repository.BillingHistoryRepository;
import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.entity.Webhook;
import com.bootcamp.paymentdemo.domain.webhook.entity.WebhookStatus;
import com.bootcamp.paymentdemo.domain.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookInternalService {
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PaymentRepository paymentRepository;
    private final WebhookRepository webhookRepository;
    private final BillingHistoryRepository billingHistoryRepository;

    @Transactional
    public PaymentResponse webhookInternalProcess(String recWebhookId, WebhookRequest request, long actualAmount) {
        PaymentResponse response = null;

        // 웹훅 저장 후 반환
        Webhook webhook = saveWebhook(recWebhookId, request);

        // payment 검증 준비
        String portOneId = request.getPaymentId();
        String status = request.getStatus();

        // 빌링키 발급 관련 웹훅 무시 (paymentId가 없거나 status가 BillingKey로 시작)
        if (portOneId == null || status.startsWith("BillingKey")) {
            log.info("빌링키 발급 관련 웹훅. status: {}", status);
            webhook.complete();
            return null;
        }

        // 구독 결제 웹훅 처리
        if (portOneId.startsWith("PAY-SUB-")) {
            BillingHistory billingHistory = billingHistoryRepository.findByPortOneId(portOneId)
                    .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

            // 멱등성
            if (billingHistory.getStatus() != BillingStatus.COMPLETED) {
                if ("PAID".equals(status)) {
                    log.info("웹훅: 구독 결제 성공 확인. portOneId: {}", portOneId);
                    billingHistory.complete();
                    billingHistory.getSubscription().renewSubscription();
                } else {
                    log.info("웹훅: 구독 결제 실패 확인. portOneId: {}", portOneId);
                    billingHistory.fail("웹훅 수신: 결제 실패 (" + status + ")");
                    billingHistory.getSubscription().expireSubscription();
                }
            }
            webhook.complete();
            return null;
        }

        // 일반 결제 처리
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(portOneId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

        // PAID -> 결제 완료
        // CANCELLED -> 결제 취소, 환불
        // FAILED -> 결제 실패 (결제 창을 닫거나 잔액이 없거나 하는 등의 결제 실패)
        try {
            // 결제 완료
            if ("PAID".equals(request.getStatus())) {
                response = paidProcess(portOneId, payment, actualAmount);
            } else if ("CANCELLED".equals(request.getStatus())) {
                response = cancelledProcess(portOneId, payment);
            } else if ("FAILED".equals(request.getStatus())) {
                response = failedProcess(portOneId, payment);
            } else {
                return PaymentResponse.register(false, portOneId, request.getStatus());
            }

            webhook.complete();
        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            throw e;
        }

        return response;
    }

    // 웹훅 저장 후 반환
    private Webhook saveWebhook(String recWebhookId, WebhookRequest request) {
        Webhook webhook = Webhook.register(recWebhookId, WebhookStatus.PENDING, null);
        webhook.updateEventStatus(request.getStatus());
        webhookRepository.save(webhook);

        return webhook;
    }

    // PAID status 일 경우 결제 확정 처리 수행
    private PaymentResponse paidProcess(String portOneId, Payment payment, long actualAmount) {
        Order order = payment.getOrder();

        // 실 결제 금액과 주문에 있는 마지막 실 결제 금액이 일치하면 확정 로직 수행
        if(actualAmount == order.getFinalAmount()) {
            log.info("웹훅을 통한 결제 확정 수행: portOneId - {}", portOneId);
            return paymentService.confirmPayment(portOneId);
        } else {
            log.error("결제 금액 불일치 발생");
            log.error("실결제 금액 : {}, 주문 최종 금액 : {}, portOneId : {}", actualAmount, order.getFinalAmount(), portOneId);
            payment.updateStatus(PaymentStatus.FAIL);
            order.updateStatus(OrderStatus.FAIL);
            return PaymentResponse.register(false, portOneId, PaymentStatus.FAIL.name());
        }
    }

    // CANCELLED status 일 경우 환불 처리 수행
    private PaymentResponse cancelledProcess(String portOneId, Payment payment) {
        // 정상 결제 확정 건에 대한 환불
        if(payment.getStatus() == PaymentStatus.COMPLETE) {
            log.info("웹훅을 통한 환불 수행: portOneId - {}", portOneId);
            return refundService.processRefund(portOneId);
        } else {
            // 결제 확정 후 재고, 포인트 부족, 결제 금액 불일치 등으로 인한 확정 실패시 결제 취소
            log.info("결제 확정 실패 건의 취소 웹훅 처리: portOneId - {}", portOneId);
            payment.updateStatus(PaymentStatus.CANCELLED);
            return PaymentResponse.register(true, portOneId, PaymentStatus.CANCELLED.name());
        }
    }

    // FAILED status 일 경우 실패 처리 수행
    private PaymentResponse failedProcess(String portOneId, Payment payment) {
        // 결제창 진행 중 실패
        log.info("웹훅을 통한 결제 실패 수행: portOneId - {}", portOneId);
        payment.updateStatus(PaymentStatus.FAIL);

        Order order = payment.getOrder();
        order.updateStatus(OrderStatus.PENDING);

        return PaymentResponse.register(true, portOneId, PaymentStatus.FAIL.name());
    }
}
