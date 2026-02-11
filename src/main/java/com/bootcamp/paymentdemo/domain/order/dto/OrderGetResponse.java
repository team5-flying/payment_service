package com.bootcamp.paymentdemo.domain.order.dto;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderGetResponse extends Base {

    private String orderId;
    private Long memberId;
    private String orderNumber;
    private Long totalAmount;
    private Long usedPoints;
    private Long finalAmount;
    private Long earnedPoints;
    private String currency;
    private OrderStatus status;
    private LocalDateTime orderAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;

    private OrderGetResponse(
            String orderId,
            Long memberId,
            String orderNumber,
            Long totalAmount,
            Long usedPoints,
            Long finalAmount,
            Long earnedPoints,
            String currency,
            OrderStatus status,
            LocalDateTime orderAt,
            Boolean deleted,
            LocalDateTime deletedAt
    ) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.usedPoints = usedPoints;
        this.finalAmount = finalAmount;
        this.earnedPoints = earnedPoints;
        this.currency = currency;
        this.status = status;
        this.orderAt = orderAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

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
