package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.service.OrderService;
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
            log.error("웹훅 시그니처가 없습니다.");
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        }

        if (!signature.contains(",") && !signature.equals(webhookSecret)) {
            log.error("웹훅 시그니처 형식이 잘못되었거나 불일치합니다. ID: {}", recWebhookId);
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        } else {
            log.info("웹훅 보안 검증 통과 (또는 테스트 모드)");
        }

        // 멱등성
        if (webhookRepository.existsByRecWebhookId(recWebhookId)) {
            log.info("중복 웹훅 무시 ID: {}", recWebhookId);
            return;
        }

        // 웹훅 저장
        Webhook webhook = Webhook.register(recWebhookId, WebhookStatus.PENDING, null);
        webhook.updateEventStatus(request.getStatus());
        webhookRepository.save(webhook);

        String portOneId = request.getPaymentId();

        try {
            if ("PAID".equals(request.getStatus())) {
                long actualAmount = portOneService.getPaymentAmount(portOneId);
                Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(portOneId)
                        .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

                Order order = payment.getOrder();

                if (actualAmount == order.getFinalAmount()) {
                    //orderService.paidOrder(order.getOrderNumber());
                    paymentService.confirmPayment(portOneId);
                }
            } else if ("CANCELLED".equals(request.getStatus())) {
                try {
                    // 환불 로직 수행
                    refundService.processRefund(portOneId);
                    log.info("웹훅을 통한 환불 처리 성공: portOneId {}", portOneId);
                } catch (ServiceErrorException e) {
                    if (e.getMessage().contains("이미 환불") || e.getMessage().contains("이미 취소")) {
                        log.info("이미 처리된 환불 건입니다. 웹훅 기록을 마칩니다: {}", portOneId);
                    } else {
                        throw e;
                    }
                }
            } else if ("FAILED".equals(request.getStatus())) {
                // 결제 실패 상태 수신시 결제 상태는 FAIL 로 변경
                Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(portOneId)
                        .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));
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
