package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderCreateRequest {

    private Long memberId;
    private Long totalAmount;
    private Long usedPoints;
    private Long finalAmount;
    private Long earnedPoints;
    private Long quantity;
    private String currency;

    private List<OrderItemRequest> items = new ArrayList<>();
}
