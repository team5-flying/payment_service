package com.bootcamp.paymentdemo.domain.payment.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.ConfirmPaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentRequest;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreatePaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), null, paymentService.createPayment(request)));
    }

    // 결제 확정은 success 필드의 응답이 중요하므로 일단 공통 해제
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(
            @PathVariable String paymentId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.confirmPayment(paymentId));
    }
}
