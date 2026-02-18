package com.bootcamp.paymentdemo.domain.plan.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends Base {

    @Id
    @Column(name = "plan_id")
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
