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

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api.base-url}")
    private String baseUrl;

    @Value("${portone.api.secret}")
    private String apiSecret;

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
}
