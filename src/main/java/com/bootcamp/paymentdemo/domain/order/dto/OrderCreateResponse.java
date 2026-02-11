package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateResponse {

    private Long memberId;
    private String orderId;
    private Long totalAmount;
    private String orderNumber;

    private OrderCreateResponse(
            Long memberId,
            String orderId,
            Long totalAmount,
            String orderNumber
    ) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderNumber = orderNumber;
    }

    public static OrderCreateResponse register(
            Long memberId,
            String orderId,
            Long totalAmount,
            String orderNumber
    ) {
        return new OrderCreateResponse(memberId, orderId, totalAmount, orderNumber);
    }
}
