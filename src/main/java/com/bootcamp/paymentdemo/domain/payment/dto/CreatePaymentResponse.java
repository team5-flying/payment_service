package com.bootcamp.paymentdemo.domain.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatePaymentResponse {
    private final Boolean success;
    private final String paymentId;
    private final String status;

    public static CreatePaymentResponse register(Boolean success, String paymentId, String status) {
        return new CreatePaymentResponse(success, paymentId, status);
    }
}
