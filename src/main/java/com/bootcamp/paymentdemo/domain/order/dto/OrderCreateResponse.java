package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateResponse {

    private Long orderId;
    private Integer totalAmount;
    private String orderNumber;

    private OrderCreateResponse(
            Long orderId,
            Integer totalAmount,
            String orderNumber
    ) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderNumber = orderNumber;
    }

    public static OrderCreateResponse register(
            Long orderId,
            Integer totalAmount,
            String orderNumber
    ) {
        return new OrderCreateResponse(orderId, totalAmount, orderNumber);
    }
}
