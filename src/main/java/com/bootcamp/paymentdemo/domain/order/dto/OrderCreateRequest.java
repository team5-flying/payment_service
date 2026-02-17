package com.bootcamp.paymentdemo.domain.order.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderCreateRequest {
    private List<OrderItemRequest> items = new ArrayList<>();
}
