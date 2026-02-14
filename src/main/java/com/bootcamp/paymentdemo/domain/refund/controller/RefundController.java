package com.bootcamp.paymentdemo.domain.refund.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.refund.service.RefundService;
import com.bootcamp.paymentdemo.domain.webhook.service.PortOneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<BaseResponse<Void>> cancelPayment(
            @PathVariable String paymentId,
            @AuthenticationPrincipal UserDetails loginMemberInfo
    ) {
        log.info("결제 취소 요청 수신: paymentId {}", paymentId);

        // 포트원에 결제 취소 명령
        portOneService.cancelPayment(paymentId, "사용자 요청 환불");

        // RefundService를 호출하여 환불 로직 수행
        refundService.processRefund(paymentId);

        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "환불이 성공적으로 처리되었습니다.", null));
    }
}
