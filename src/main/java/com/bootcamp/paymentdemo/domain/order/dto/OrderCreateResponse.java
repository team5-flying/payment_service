package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateResponse {

    private Long memberId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private Long totalAmount;
    private String orderNumber;

    private OrderCreateResponse(
            Long memberId,
            Long orderId,
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
            Long orderId,
            Long totalAmount,
            String orderNumber
    ) {
        return new OrderCreateResponse(memberId, orderId, totalAmount, orderNumber);
    }
}
