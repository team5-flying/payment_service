package com.bootcamp.paymentdemo.domain.order.dto;
// TODO 결제 테스트용, 삭제 필요
public record SearchOrderResponse(
       String orderNumber
       , String orderId
       , Long totalAmount
       , Long usedPoints
       , Long finalAmount
       , Long earnedPoints
       , String currency
       , String status
       , String createdAt
) {
}
