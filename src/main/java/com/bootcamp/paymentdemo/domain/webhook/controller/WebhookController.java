package com.bootcamp.paymentdemo.domain.webhook.controller;

import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.service.WebhookExternalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookExternalService webhookExternalService;
    private final ObjectMapper objectMapper;

    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestHeader(value = "webhook-id", required = false) String webhookId,
            @RequestHeader(value = "webhook-signature", required = false) String signature,
            @RequestHeader(value = "webhook-timestamp", required = false) String timestamp,
            @RequestBody String rawBody
    ) {
        // PortOne SDK 에서 Json Raw text 가 필요하여 후속 처리용 ObjectMapper
        WebhookRequest request = objectMapper.readValue(rawBody, WebhookRequest.class);

        log.info("V2 웹훅 수신 확인 - paymentId: {}\n, status: {}\n, signature: {}\n, timestamp: {}\n, rawBody: {}",
                request.getPaymentId(), request.getStatus(), signature, timestamp, rawBody);

        // webhookId가 없는 경우 paymentId를 대체 키로 사용
        String effectiveId = (webhookId != null) ? webhookId : "test-" + request.getPaymentId();

        webhookExternalService.webhookExternalProcess(effectiveId, signature, timestamp, rawBody, request);
        return ResponseEntity.ok().build();
    }
}
