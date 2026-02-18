package com.bootcamp.paymentdemo.domain.payment.entity;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod {

    @Id
    private String paymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String billingKey;
    private String customerUid;
    private String pgName;
    private boolean defaultPaymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentMethodStatus status;

    public static PaymentMethod createPaymentMethod(String id, Member member, String billingKey, String customerUid) {
        PaymentMethod pm = new PaymentMethod();
        pm.paymentMethodId = id;
        pm.member = member;
        pm.billingKey = billingKey;
        pm.customerUid = customerUid;
        pm.status = PaymentMethodStatus.ACTIVE;
        return pm;
    }
}
