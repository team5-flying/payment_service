package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final PortOneService portOneService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PaymentRepository paymentRepository;

    @Value("${portone.api.webhook-secret}")
    private String webhookSecret;

    @Transactional
    public void process(String recWebhookId, String signature, WebhookRequest request) {
        // 시그니처
        if (signature == null) {
            log.error("웹훅 시그니처가 없습니다");
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        }

        if (!signature.contains(",") && !signature.equals(webhookSecret)) {
            log.error("웹훅 시그니처 형식이 잘못되었거나 불일치 합니다 ID: {}", recWebhookId);
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        } else {
            log.info("웹훅 보안 검증 통과 (또는 테스트 모드)");
        }

        // 멱등성
        if (webhookRepository.existsByRecWebhookId(recWebhookId)) {
            log.info("중복 웹훅 멱등성 처리, 수신된 웹훅 ID: {}", recWebhookId);
            return;
        }

        // 웹훅 저장
        Webhook webhook = Webhook.register(recWebhookId, WebhookStatus.PENDING, null);
        webhook.updateEventStatus(request.getStatus());
        webhookRepository.save(webhook);

        // Payment 검증 준비
        String portOneId = request.getPaymentId();
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(portOneId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

        // PAID -> 결제 완료
        // CANCELLED -> 결제 취소, 환불
        // FAILED -> 결제 실패 (결제 창을 닫거나 잔액이 없거나 하는 등의 결제 실패)
        try {
            // 결제 완료
            if ("PAID".equals(request.getStatus())) {
                Order order = payment.getOrder();
                long actualAmount = portOneService.getPaymentAmount(portOneId);

                // 실 결제 금액과 주문에 있는 마지막 실 결제 금액이 일치하면 확정 로직 수행
                if (actualAmount == order.getFinalAmount()) {
                    log.info("웹훅을 통한 결제 확정 수행: portOneId - {}", portOneId);
                    paymentService.confirmPayment(portOneId);
                }
            } else if ("CANCELLED".equals(request.getStatus())) {
                // 결제 취소, 환불
                if (payment.getStatus() == PaymentStatus.FAIL) {
                    log.info("재고 또는 포인트 문제로 결제단에서 취소 수행한 결제 : portOneId - {}", portOneId);
                } else {
                    log.info("웹훅을 통한 환불 수행: portOneId - {}", portOneId);
                    refundService.processRefund(portOneId);
                }
            } else if ("FAILED".equals(request.getStatus())) {
                // 결제창 진행 중 실패
                log.info("웹훅을 통한 결제 실패 수행: portOneId - {}", portOneId);
                payment.updateStatus(PaymentStatus.FAIL);
                Order order = payment.getOrder();
                order.updateStatus(OrderStatus.PENDING);
            }

            webhook.complete();
        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            throw e;
        }
    }
}
