package com.bootcamp.paymentdemo.domain.plan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    private String planId;

    private String name;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    public static Plan createPlan(String planId, String name, Long amount, BillingCycle billingCycle) {
        Plan plan = new Plan();
        plan.planId = planId;
        plan.name = name;
        plan.amount = amount;
        plan.billingCycle = billingCycle;
        return plan;
    }
}
