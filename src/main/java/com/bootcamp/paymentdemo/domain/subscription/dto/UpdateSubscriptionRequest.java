package com.bootcamp.paymentdemo.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionRequest {

    private String action;
    private String reason;
}
