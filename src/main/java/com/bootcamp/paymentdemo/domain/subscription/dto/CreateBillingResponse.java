package com.bootcamp.paymentdemo.domain.subscription.dto;

import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import com.bootcamp.paymentdemo.domain.subscription.entity.BillingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillingResponse {

    private boolean success;
    private String billingId;
    private String portOneId;
    private Long amount;
    private String status;

    public static CreateBillingResponse from(BillingHistory history) {
        return new CreateBillingResponse(
                history.getStatus() == BillingStatus.COMPLETED,
                history.getBillingId(),
                history.getPortOneId(),
                history.getAmount(),
                history.getStatus().name()
        );
    }
}
