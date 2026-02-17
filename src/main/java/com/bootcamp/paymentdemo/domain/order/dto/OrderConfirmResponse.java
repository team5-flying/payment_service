package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderConfirmResponse {
    private final String orderId;
    private final String status;

    public static OrderConfirmResponse register(
            String orderId,
            String status
    ) {
        return new OrderConfirmResponse(orderId, status);
    }
}
