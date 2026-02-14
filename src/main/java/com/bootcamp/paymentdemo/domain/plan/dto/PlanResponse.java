package com.bootcamp.paymentdemo.domain.plan.dto;

import com.bootcamp.paymentdemo.domain.plan.entity.BillingCycle;
import com.bootcamp.paymentdemo.domain.plan.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private String planId;
    private String name;
    private Long amount;
    private BillingCycle billingCycle;

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getPlanId(),
                plan.getName(),
                plan.getAmount(),
                plan.getBillingCycle()
        );
    }
}
