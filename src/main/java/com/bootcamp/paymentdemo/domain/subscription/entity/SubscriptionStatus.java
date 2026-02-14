package com.bootcamp.paymentdemo.domain.subscription.entity;

public enum SubscriptionStatus {
    TRIALING,        // 체험판 (결제 전)
    ACTIVE,          // 활성 (결제 후)
    PAST_DUE,        // 연체
    CANCELED,        // 해지
    EXPIRED          // 만료
}
