package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderItemRequest {

    private Long productId;
    private String productName;
    private Integer finalAmount;
    private Integer quantity;

}
