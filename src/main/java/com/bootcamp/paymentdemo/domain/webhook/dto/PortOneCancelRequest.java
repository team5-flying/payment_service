package com.bootcamp.paymentdemo.domain.webhook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneCancelRequest {
    String reason;
}
