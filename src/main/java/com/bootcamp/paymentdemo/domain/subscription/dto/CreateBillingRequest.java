package com.bootcamp.paymentdemo.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillingRequest {
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
