package com.bootcamp.paymentdemo.domain.order.dto;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderGetResponse extends Base {

    private final String orderId;
    private final Long memberId;
    private final String orderNumber;
    private final Long totalAmount;
    private final Long usedPoints;
    private final Long finalAmount;
    private final Long earnedPoints;
    private final String currency;
    private final OrderStatus status;
    private final LocalDateTime orderAt;
    private final Boolean deleted;
    private final LocalDateTime deletedAt;

    public static OrderGetResponse register(
            String orderId,
            Long memberId,
            String orderNumber,
            Long totalAmount,
            Long usePoints,
            Long finalAmount,
            Long earnedPoints,
            String currency,
            OrderStatus status,
            LocalDateTime orderAt,
            Boolean deleted,
            LocalDateTime deletedAt
    ) {
        return new OrderGetResponse(
                orderId, memberId, orderNumber, totalAmount, usePoints, finalAmount, earnedPoints, currency, status, orderAt, deleted, deletedAt
        );
    }

}
