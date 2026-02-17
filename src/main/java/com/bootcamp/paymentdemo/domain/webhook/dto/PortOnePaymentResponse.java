package com.bootcamp.paymentdemo.domain.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOnePaymentResponse {
    // 다른 항목이 필요하면 추가, 현재로선 가격으로만 비교하므로 amount 부분만 받아옴
    private PortOneAmountResponse amount;
}
