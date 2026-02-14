package com.bootcamp.paymentdemo.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BillingListResponse {
    private List<BillingHistoryResponse> billings;
}
