package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateResponse {
    private final Long memberId;
    private final String orderId;
    private final Long totalAmount;
    private final String orderNumber;

    public static OrderCreateResponse register(
            Long memberId,
            String orderId,
            Long totalAmount,
            String orderNumber
    ) {
        return new OrderCreateResponse(memberId, orderId, totalAmount, orderNumber);
    }
}
