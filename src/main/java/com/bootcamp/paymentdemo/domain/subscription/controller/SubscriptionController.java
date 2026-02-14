package com.bootcamp.paymentdemo.domain.subscription.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.subscription.dto.*;
import com.bootcamp.paymentdemo.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateSubscriptionResponse>> createSubscription(
            @RequestBody CreateSubscriptionRequest request,
            @AuthenticationPrincipal UserDetails loginMemberInfo
    ) {
        Member member = memberRepository.findByEmailAndDeletedFalse(loginMemberInfo.getUsername())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        CreateSubscriptionResponse response = subscriptionService.createSubscription(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "구독이 생성되었습니다.", response));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<BaseResponse<SubscriptionResponse>> getSubscription(@PathVariable String subscriptionId) {
        SubscriptionResponse response = subscriptionService.getSubscription(subscriptionId);
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "구독 조회 성공", response));
    }

    @PostMapping("/{subscriptionId}/billings")
    public ResponseEntity<BaseResponse<CreateBillingResponse>> createBilling(
            @PathVariable String subscriptionId,
            @RequestBody CreateBillingRequest request
    ) {
        CreateBillingResponse response = subscriptionService.createBilling(subscriptionId, request);
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "결제 요청 처리 완료", response));
    }

    @GetMapping("/{subscriptionId}/billings")
    public ResponseEntity<BaseResponse<BillingListResponse>> getBillingHistories(@PathVariable String subscriptionId) {
        BillingListResponse response = subscriptionService.getBillingHistories(subscriptionId);
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "청구 내역 조회 성공", response));
    }

    @PatchMapping("/{subscriptionId}")
    public ResponseEntity<BaseResponse<UpdateSubscriptionResponse>> updateSubscription(
            @PathVariable String subscriptionId,
            @RequestBody UpdateSubscriptionRequest request
    ) {
        UpdateSubscriptionResponse response = subscriptionService.updateSubscription(subscriptionId, request);
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "구독 상태 변경 성공", response));
    }
}
