package com.bootcamp.paymentdemo.domain.refund.controller;

import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.refund.service.RefundService;
import com.bootcamp.paymentdemo.domain.webhook.service.PortOneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final PortOneService portOneService;

    // 결제 취소는 success 필드의 응답이 중요하므로 일단 공통 해제
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable String paymentId
    ) {
        portOneService.cancelPayment(paymentId, "사용자 요청 환불");
        return ResponseEntity.status(HttpStatus.OK).body(refundService.processRefund(paymentId));
    }
}
