package com.bootcamp.paymentdemo.domain.subscription.dto;

import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BillingHistoryResponse {

    private String billingId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Long amount;
    private String status;
    private String paymentId;
    private LocalDateTime attemptDate;
    private String failureMessage;

    public static BillingHistoryResponse from(BillingHistory history) {
        return new BillingHistoryResponse(
                history.getBillingId(),
                history.getPeriodStart(),
                history.getPeriodEnd(),
                history.getAmount(),
                history.getStatus().name(),
                history.getPortOneId(),
                history.getAttemptDate(),
                history.getFailureMessage()
        );
    }
}
