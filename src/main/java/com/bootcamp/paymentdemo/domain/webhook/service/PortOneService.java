package com.bootcamp.paymentdemo.domain.webhook.service;


import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class PortOneService {

    @Value("${portone.api.base-url}")
    private String baseUrl;

    @Value("${portone.api.secret}")
    private String apiSecret;

    @Value("{portOne.stor.id}")
    private String storeId;

    @Value("{portOne.channel.kg-inicis}")
    private String kgChannelKey;

    @Value("{portOne.channel.toss}")
    private String tossChannelKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public long getPaymentAmount(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format("%s/payments/%s", baseUrl, paymentId);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> payment = (Map<String, Object>) response.getBody();
            Map<String, Object> amountMap = (Map<String, Object>) payment.get("amount");

            return Long.parseLong(amountMap.get("total").toString());
        } catch (Exception e) {
            log.error("포트원 API 호출 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_NOT_FOUND_PAYMENT);
        }
    }

    public void cancelPayment(String paymentId, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        headers.set("Accept", "application/json");

        Map<String, String> body = Map.of("reason", reason);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        String url = String.format("%s/payments/%s/cancel", baseUrl, paymentId);

        try {
            log.info("포트원 결제 취소 API 호출: paymentId={}, reason={}", paymentId, reason);
            // POST 요청으로 취소 명령 전송
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("포트원 결제 취소 API 호출 성공: {}", paymentId);
        } catch (Exception e) {
            log.error("포트원 결제 취소 API 호출 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_FAIL_REFUND);
        }
    }

    // 빌링키 결제 요청
    public void payWithBillingKey(String paymentId, String billingKey, String orderName, Long amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        headers.set("Content-Type", "application/json");

        Map<String, Object> amountMap = Map.of("total", amount);
        Map<String, Object> body = Map.of(
                "billingKey", billingKey,
                "orderName", orderName,
                "amount", amountMap,
                "currency", "KRW"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = String.format("%s/payments/%s/billing-key", baseUrl, paymentId);

        try {
            log.info("포트원 빌링키 결제 API 호출, payment={}, amount={}", paymentId, amount);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("포트원 빌링키 결제 성공, 결제번호: {}", paymentId);
        } catch (Exception e) {
            log.error("포트원 빌링키 결제 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_NOT_SUCCESS_PAYMENT);
        }
    }

    // 프론트엔드 결제창 연동 시 필요한 설정값 반환
    public Map<String, String> getPublicConfig() {
        return Map.of(
                "storeId", storeId,
                "kgChannelKey", kgChannelKey,
                "tossChannelKey", tossChannelKey
        );
    }
}
