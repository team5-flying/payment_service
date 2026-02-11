package com.bootcamp.paymentdemo.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreatePaymentRequest {
    @NotBlank(message = "주문 ID는 필수 입니다")
    private String orderId;
    @NotNull(message = "결제 금액은 필수 입니다")
    @Min(value = 1, message = "결제 금액은 1원 이상이여야 합니다")
    private Long totalAmount;
    @Min(value = 0, message = "포인트 결제 금액은 0원 이상이여야 합니다")
    private Long pointsToUse;
}
