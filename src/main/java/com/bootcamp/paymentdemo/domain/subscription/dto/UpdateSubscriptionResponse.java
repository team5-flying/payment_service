package com.bootcamp.paymentdemo.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionResponse {

    private boolean success;
    private String subscriptionId;
    private String status;
}
