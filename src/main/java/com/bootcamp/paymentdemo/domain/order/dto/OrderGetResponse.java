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

    private Long orderId;
    private Long memberId;
    private String orderNumber;
    private Integer totalAmount;
    private Integer usePoints;
    private Integer finalAmount;
    private Integer earnedPoints;
    private String currency;
    private OrderStatus status;
    private LocalDateTime orderAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;

    private OrderGetResponse(
            Long orderId,
            Long memberId,
            String orderNumber,
            Integer totalAmount,
            Integer usePoints,
            Integer finalAmount,
            Integer earnedPoints,
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
        this.usePoints = usePoints;
        this.finalAmount = finalAmount;
        this.earnedPoints = earnedPoints;
        this.currency = currency;
        this.status = status;
        this.orderAt = orderAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    public static OrderGetResponse register(
            Long orderId,
            Long memberId,
            String orderNumber,
            Integer totalAmount,
            Integer usePoints,
            Integer finalAmount,
            Integer earnedPoints,
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
