package com.bootcamp.paymentdemo.domain.webhook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortOneAmountResponse {
    private Integer total;
    private Integer taxFree;
    private Integer vat;
    private Integer supply;
    private Integer discount;
    private Integer paid;
    private Integer cancelled;
    private Integer cancelledTaxFree;
}
