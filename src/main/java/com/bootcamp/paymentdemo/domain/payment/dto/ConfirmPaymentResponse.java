package com.bootcamp.paymentdemo.domain.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfirmPaymentResponse {
    private Boolean success;
    private String orderId;
    private String status;

    public static ConfirmPaymentResponse register(
            Boolean success,
            String orderId,
            String status
    ) {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse();
        response.success = success;
        response.orderId = orderId;
        response.status = status;
        return response;
    }
}
