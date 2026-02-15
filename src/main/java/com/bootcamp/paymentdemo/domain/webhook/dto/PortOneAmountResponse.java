package com.bootcamp.paymentdemo.domain.webhook.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneAmountResponse {
    Integer total;
    Integer taxFree;
    Integer vat;
    Integer supply;
    Integer discount;
    Integer paid;
    Integer cancelled;
    Integer cancelledTaxFree;

    private PortOneAmountResponse(Integer total, Integer taxFree, Integer vat, Integer supply, Integer discount, Integer paid, Integer cancelled, Integer cancelledTaxFree) {
        this.total = total;
        this.taxFree = taxFree;
        this.vat = vat;
        this.supply = supply;
        this.discount = discount;
        this.paid = paid;
        this.cancelled = cancelled;
        this.cancelledTaxFree = cancelledTaxFree;
    }

    public static PortOneAmountResponse register(Integer total, Integer taxFree, Integer vat, Integer supply, Integer discount, Integer paid, Integer cancelled, Integer cancelledTaxFree) {
        return new PortOneAmountResponse(total, taxFree, vat, supply, discount, paid, cancelled, cancelledTaxFree);
    }
}
