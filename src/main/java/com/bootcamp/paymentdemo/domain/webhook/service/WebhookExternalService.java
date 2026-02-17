package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookExternalService {
    private final WebhookInternalService webhookInternalService;
    private final PortOneService portOneService;
    private final WebhookRepository webhookRepository;

    @Value("${portone.api.webhook-secret}")
    private String webhookSecret;

    public void webhookExternalProcess(String recWebhookId, String signature, WebhookRequest request) {
        String portOneId = request.getPaymentId();
        long actualAmount = 0L;

        // 시그니처 검증
        validateSignature(signature, recWebhookId);

        // 멱등성 검증
        if (webhookRepository.existsByRecWebhookId(recWebhookId)) {
            log.info("중복 웹훅 멱등성 처리, 수신된 웹훅 ID: {}", recWebhookId);
            return;
        }

        // PortOne 에 기록된 결제 금액 받아오기 (PAID status 검증에 필요)
        if(request.getStatus().equals("PAID")) {
            actualAmount = portOneService.getPaymentAmount(portOneId);
        }

        // 트랜잭션 프로세스 처리
        PaymentResponse paymentResponse = webhookInternalService.webhookInternalProcess(recWebhookId, request, actualAmount);

        // 결제 확정 처리 중 실패할 경우 포트원 결제 취소 API 호출
        if(!paymentResponse.getSuccess() && request.getStatus().equals("PAID")) {
            portOneService.cancelPayment(request.getPaymentId(), "결제 확정 실패 : 결제건의 재고 또는 포인트 부족 또는 최종 결제 금액 상충으로 인한 실패");
        }
    }

    private void validateSignature(String signature, String recWebhookId) {
        // 시그니처 검증
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
    }
}
