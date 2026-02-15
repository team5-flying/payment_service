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

    @Transactional
    public PaymentResponse webhookInternalProcess(String recWebhookId, WebhookRequest request, long actualAmount) {
        PaymentResponse response;

        // 웹훅 저장 후 반환
        Webhook webhook = saveWebhook(recWebhookId, request);

        // payment 검증 준비
        String portOneId = request.getPaymentId();
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
                return PaymentResponse.register(false, portOneId, request.getStatus(), "지원하지 않는 웹훅 상태 : " + request.getStatus());
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
            return paymentService.completePayment(portOneId);
        } else {
            log.error("결제 금액 불일치 발생");
            log.error("실결제 금액 : {}, 주문 최종 금액 : {}, portOneId : {}", actualAmount, order.getFinalAmount(), portOneId);
            payment.updateStatus(PaymentStatus.FAIL);
            order.updateStatus(OrderStatus.FAIL);
            return PaymentResponse.register(false, portOneId, PaymentStatus.FAIL.name()
                    , "결제 금액 불일치 - 실 결제금액 : " + actualAmount + ", 주문금액: " + order.getFinalAmount());
        }
    }

    // CANCELLED status 일 경우 환불 처리 수행
    private PaymentResponse cancelledProcess(String portOneId, Payment payment) {
        if (payment.getStatus() == PaymentStatus.COMPLETE) {
            // 구매 확정 건은 환불 불가
            if (payment.getOrder().getStatus() == OrderStatus.CONFIRMED) {
                log.info("구매 확정 건 환불 불가: portOneId - {}", portOneId);
                return PaymentResponse.register(false, portOneId, PaymentStatus.CANCELLED.name()
                        , "구매 확정 건 환불 불가");
            }

            // 정상 결제 확정 건 환불
            log.info("웹훅을 통한 환불 수행: portOneId - {}", portOneId);
            return refundService.processRefund(portOneId);
        }

        // 결제 실패 건 또는 기처리 건의 취소 웹훅 처리
        log.info("결제 실패 건 또는 기처리 건의 취소 웹훅 처리: portOneId - {}", portOneId);
        payment.updateStatus(PaymentStatus.CANCELLED);
        payment.getOrder().updateStatus(OrderStatus.FAIL);
        return PaymentResponse.register(true, portOneId, PaymentStatus.CANCELLED.name()
                , "결제 실패 건 취소 처리 완료");
    }

    // FAILED status 일 경우 실패 처리 수행
    private PaymentResponse failedProcess(String portOneId, Payment payment) {
        // 결제창 진행 중 실패
        log.info("웹훅을 통한 결제 실패 수행: portOneId - {}", portOneId);
        payment.updateStatus(PaymentStatus.FAIL);

        Order order = payment.getOrder();
        order.updateStatus(OrderStatus.FAIL);

        return PaymentResponse.register(true, portOneId, PaymentStatus.FAIL.name()
                , "결제창 진행 중 실패");
    }
}
