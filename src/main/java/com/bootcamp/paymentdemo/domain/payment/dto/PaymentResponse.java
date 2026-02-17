package com.bootcamp.paymentdemo.domain.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResponse {
    // 결제 완료와 취소가 같은 Response 를 공유 중
    // 각각의 케이스의 성공 여부
    private final Boolean success;
    private final String orderId;
    private final String status;
    private final String message;

    public static PaymentResponse register(
            Boolean success,
            String orderId,
            String status,
            String message
    ) {
        return new PaymentResponse(success, orderId, status, message);
    }
}
