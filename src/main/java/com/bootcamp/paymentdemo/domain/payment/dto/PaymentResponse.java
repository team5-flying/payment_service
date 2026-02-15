package com.bootcamp.paymentdemo.domain.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResponse {
    private Boolean success;
    private String orderId;
    private String status;
    private String message;

    public static PaymentResponse register(
            Boolean success,
            String orderId,
            String status
    ) {
        PaymentResponse response = new PaymentResponse();
        response.success = success;
        response.orderId = orderId;
        response.status = status;
        return response;
    }

    public static PaymentResponse register(
            Boolean success,
            String orderId,
            String status,
            String message
    ) {
        PaymentResponse response = new PaymentResponse();
        response.success = success;
        response.orderId = orderId;
        response.status = status;
        response.message = message;
        return response;
    }
}
