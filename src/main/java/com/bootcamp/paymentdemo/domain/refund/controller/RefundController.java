package com.bootcamp.paymentdemo.domain.refund.controller;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
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

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_FAIL_REFUND;
import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_NOT_FOUND_PAYMENT;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final PortOneService portOneService;
    private final PaymentRepository paymentRepository;

    // 결제 취소는 success 필드의 응답이 중요하므로 일단 공통 해제
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable String paymentId
    ) {
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(paymentId)
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PAYMENT));

        // 0원 결제가 아닌 경우에만 PortOne 취소 호출
        if (payment.getOrder().getFinalAmount() > 0) {
            portOneService.cancelPayment(paymentId, "사용자 요청 환불");
        }

        if (payment.getOrder().getFinalAmount() == 0) {
            // 0원 결제의 경우엔 복구만 시켜주면 됨
            refundService.processRefund(paymentId);
        }

        if(payment.getOrder().getFinalAmount() < 0) {
            log.error("결제금 음수 환불 수행됨, PaymentId - {}", paymentId);
            throw new ServiceErrorException(ERR_FAIL_REFUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(refundService.processRefund(paymentId));
    }
}
