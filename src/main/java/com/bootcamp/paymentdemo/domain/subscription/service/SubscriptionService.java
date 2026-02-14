package com.bootcamp.paymentdemo.domain.subscription.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentMethod;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentMethodRepository;
import com.bootcamp.paymentdemo.domain.plan.entity.Plan;
import com.bootcamp.paymentdemo.domain.plan.repository.PlanRepository;
import com.bootcamp.paymentdemo.domain.subscription.dto.CreateSubscriptionRequest;
import com.bootcamp.paymentdemo.domain.subscription.dto.CreateSubscriptionResponse;
import com.bootcamp.paymentdemo.domain.subscription.dto.SubscriptionResponse;
import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PlanRepository planRepository;

    @Transactional
    public CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request, Member member) {
        // 플랜 조회
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PLAN));

        // 빌링키 저장 (이미 존재하면 그대로 사용하고 없으면 새로 생성)
        PaymentMethod paymentMethod = paymentMethodRepository.findByBillingKey(request.getBillingKey())
                .orElseGet(() -> {
                    String paymentMethodId = "PM-" + UUID.randomUUID().toString().substring(0,8);
                    PaymentMethod newPaymentMethod = PaymentMethod.createPaymentMethod(
                            paymentMethodId,
                            member,
                            request.getBillingKey(),
                            request.getCustomerUid()
                    );
                    return paymentMethodRepository.save(newPaymentMethod);
                });

        // 구독 생성
        String subscriptionId = "SUB-" + UUID.randomUUID().toString().substring(0,8);

        Subscription subscription = Subscription.createSubscription(subscriptionId, plan, member, paymentMethod, request.getAmount());
        subscriptionRepository.save(subscription);

        return CreateSubscriptionResponse.from(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_SUBSCRIPTION));

        return SubscriptionResponse.from(subscription);
    }
}
