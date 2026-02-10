package com.bootcamp.paymentdemo.domain.order.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreateRequest {

    private Long memberId;
    private Integer totalAmount;
    private Integer usePoints;
    private Integer finalAmount;
    private Integer EarnedPoints;
    private Integer quantity;
    private String currency;

    private List<OrderItemRequest> items;
}
