package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.repository.WebhookRepository;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookExternalService {
    private final WebhookInternalService webhookInternalService;
    private final PortOneService portOneService;
    private final WebhookRepository webhookRepository;

    @Value("${portone.api.webhook-secret}")
    private String webhookSecret;

    public void webhookExternalProcess(String webhookId, String signature, String timeStamp, String rawBody, WebhookRequest request) {
        String portOneId = request.getPaymentId();
        long actualAmount = 0L;

        // 시그니처 검증
        validateSignature(webhookId, signature, timeStamp, rawBody);

        // 멱등성 검증
        if (webhookRepository.existsByRecWebhookId(webhookId)) {
            log.info("중복 웹훅 멱등성 처리, 수신된 웹훅 ID: {}", webhookId);
            return;
        }

        // PortOne 에 기록된 결제 금액 받아오기 (PAID status 검증에 필요)
        if(request.getStatus().equals("PAID")) {
            actualAmount = portOneService.getPaymentAmount(portOneId);
        }

        // 트랜잭션 프로세스 처리
        PaymentResponse paymentResponse = webhookInternalService.webhookInternalProcess(webhookId, request, actualAmount);

        // 웹훅 처리 결과 로깅
        if (paymentResponse.getSuccess()) {
            log.info("웹훅 처리 완료:\n portOneId - {}\n 웹훅 상태 - {}\n 처리 결과 - {}\n message - {}",
                    portOneId, request.getStatus(), paymentResponse.getStatus(), paymentResponse.getMessage());
        } else {
            log.info("웹훅 처리 실패 혹은 무시:\n portOneId - {}\n 웹훅 상태 - {}\n 처리 결과 - {}\n message - {}",
                    portOneId, request.getStatus(), paymentResponse.getStatus(), paymentResponse.getMessage());
        }

        // 결제 확정 처리 중 실패할 경우 포트원 결제 취소 API 호출
        if(!paymentResponse.getSuccess() && request.getStatus().equals("PAID")) {
            portOneService.cancelPayment(request.getPaymentId(), "결제 확정 실패 : 결제건의 재고 또는 포인트 부족 또는 최종 결제 금액 상충으로 인한 실패");
        }
    }

    private void validateSignature(String webhookId, String signature, String timestamp, String rawBody) {
        // 시그니처 검증
        if (signature == null) {
            log.error("웹훅 시그니처가 없음");
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        }

        try {
            // 웹훅 검증기 (PortOne SDK 제공)
            WebhookVerifier verifier = new WebhookVerifier(webhookSecret);
            verifier.verify(rawBody, webhookId, signature, timestamp);
            log.info("웹훅 검증 통과");
        } catch (WebhookVerificationException e) {
            log.error("웹훅 검증 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_INVALID_SIGNATURE);
        }
    }
}
