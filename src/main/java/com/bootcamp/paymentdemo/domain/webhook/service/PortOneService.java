package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.webhook.dto.PortOneCancelRequest;
import com.bootcamp.paymentdemo.domain.webhook.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api.base-url}")
    private String baseUrl;

    @Value("${portone.api.secret}")
    private String apiSecret;

    @Value("${portOne.stor.id}")
    private String storeId;

    @Value("${portOne.channel.kg-inicis}")
    private String kgChannelKey;

    @Value("${portOne.channel.toss}")
    private String tossChannelKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public long getPaymentAmount(String paymentId) {
        String url = String.format("%s/payments/%s", baseUrl, paymentId);

        try {
            log.info("포트원 API Payment 정보 조회 호출: PaymentId - {}", paymentId);
            ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(createHeaders()), PortOnePaymentResponse.class);

            if (response.getBody() == null) {
                log.error("포트원 API Payment 정보 조회 결과 없음: PaymentId - {}", paymentId);
                throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_NOT_FOUND_PAYMENT);
            }

            log.info("포트원 API Payment 정보 조회 호출 성공: PaymentId - {}", paymentId);
            return response.getBody().getAmount().getTotal();
        } catch (ServiceErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("포트원 API Payment 정보 조회 호출 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_NOT_FOUND_PAYMENT);
        }
    }

    public void cancelPayment(String paymentId, String reason) {
        String url = String.format("%s/payments/%s/cancel", baseUrl, paymentId);

        PortOneCancelRequest request = new PortOneCancelRequest();
        request.setReason(reason);

        try {
            log.info("포트원 결제 취소 API 호출: paymentId={}, reason={}", paymentId, reason);
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, createHeaders()), String.class);
        } catch (HttpStatusCodeException e) {
            // 400, 500 대 코드로 떨어지면 catch 됨
            log.error("포트원 결제 취소 API 수행 결과 실패: PaymentId - {}", paymentId);
            log.error("포트원 결제 취소 API 수행 결과 실패: 사유 - {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("포트원 결제 취소 API 호출 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_FAIL_REFUND);
        }

        log.info("포트원 결제 취소 API 호출 성공: PaymentId - {}", paymentId);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        return headers;
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

    // 빌링키 유효성 검증
    public void validateBillingKey(String billingKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = String.format("%s/billing-keys/%s", baseUrl, billingKey);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            log.info("빌링키 유효성 검증 성공: {}", billingKey);
        } catch (Exception e) {
            log.error("빌링키 유효성 검증 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_BILLING_KEY);
        }
    }
}
