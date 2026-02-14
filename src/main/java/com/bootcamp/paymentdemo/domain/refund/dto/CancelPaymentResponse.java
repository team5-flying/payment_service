package com.bootcamp.paymentdemo.domain.refund.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelPaymentResponse {
    private Boolean success;
    private String orderId;
    private String status;

    public static CancelPaymentResponse register(
            Boolean success,
            String orderId,
            String status
    ) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.success = success;
        response.orderId = orderId;
        response.status = status;
        return response;
    }
}
