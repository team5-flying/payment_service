package com.bootcamp.paymentdemo.domain.subscription.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.subscription.dto.CreateSubscriptionRequest;
import com.bootcamp.paymentdemo.domain.subscription.dto.CreateSubscriptionResponse;
import com.bootcamp.paymentdemo.domain.subscription.dto.SubscriptionResponse;
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
}
