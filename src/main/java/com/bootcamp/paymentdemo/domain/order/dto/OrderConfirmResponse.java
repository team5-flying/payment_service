package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderConfirmResponse {
    private String orderId;
    private String status;

    public static OrderConfirmResponse register(
            String orderId,
            String status
    ) {
        OrderConfirmResponse response = new OrderConfirmResponse();
        response.orderId = orderId;
        response.status = status;
        return response;
    }
}
