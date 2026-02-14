package com.bootcamp.paymentdemo.domain.subscription.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentMethod;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentMethodRepository;
import com.bootcamp.paymentdemo.domain.plan.entity.Plan;
import com.bootcamp.paymentdemo.domain.plan.repository.PlanRepository;
import com.bootcamp.paymentdemo.domain.subscription.dto.*;
import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.entity.SubscriptionStatus;
import com.bootcamp.paymentdemo.domain.subscription.repository.BillingHistoryRepository;
import com.bootcamp.paymentdemo.domain.subscription.repository.SubscriptionRepository;
import com.bootcamp.paymentdemo.domain.webhook.service.PortOneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PlanRepository planRepository;
    private final BillingHistoryRepository billingHistoryRepository;
    private final PortOneService portOneService;

    @Transactional
    public CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request, Member member) {
        // 플랜 조회
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PLAN));

        // 빌링키 유효성 검증
        portOneService.validateBillingKey(request.getBillingKey());

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

    @Transactional
    public CreateBillingResponse createBilling(String subscriptionId, CreateBillingRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_SUBSCRIPTION));

        // 결제 ID 및 빌링 ID 생성
        String billingId = "BILL-" + UUID.randomUUID().toString().substring(0,8);
        String portOneId = "PAY-SUB-" + UUID.randomUUID().toString().substring(0,12);

        // 청구 내역 생성
        BillingHistory billingHistory = BillingHistory.create(
                billingId,
                subscription,
                portOneId,
                subscription.getAmount(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        try {
            // 포트원 빌링키 결제 요청
            String orderName = subscription.getPlan().getName() + " 정기 결제";
            portOneService.payWithBillingKey(portOneId, subscription.getPaymentMethod().getBillingKey(), orderName, subscription.getAmount());

            billingHistory.complete();

            // 다음 결제일 갱신
            subscription.renewSubscription();
        } catch (Exception e) {
            billingHistory.fail(e.getMessage());

            // 결제 실패 시 구독 상태 변경
            subscription.expireSubscription();
        }

        billingHistoryRepository.save(billingHistory);
        return CreateBillingResponse.from(billingHistory);
    }

    @Transactional
    public BillingListResponse getBillingHistories(String subscriptionId) {
        // 구독 존재 확인
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_SUBSCRIPTION);
        }

        List<BillingHistoryResponse> histories = billingHistoryRepository.findAllBySubscriptionId(subscriptionId).stream()
                .map(BillingHistoryResponse::from)
                .collect(Collectors.toList());

        return new BillingListResponse(histories);
    }

    @Transactional
    public UpdateSubscriptionResponse updateSubscription(String subscriptionId,UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_SUBSCRIPTION));

        if ("cancel".equalsIgnoreCase(request.getAction())) {
            if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
                throw new ServiceErrorException(ErrorEnum.ERR_ALREADY_CANCELLED_SUBSCRIPTION);
            }
            subscription.cancel(request.getReason());
        } else {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_REFUND_STATUS);
        }

        return new UpdateSubscriptionResponse(true, subscription.getSubscriptionId(), subscription.getStatus().name());
    }
}
