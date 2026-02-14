package com.bootcamp.paymentdemo.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    private String customerUid;
    private String planId;
    private String billingKey;
    private Long amount;
}
