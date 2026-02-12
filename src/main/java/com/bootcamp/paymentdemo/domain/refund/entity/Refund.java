package com.bootcamp.paymentdemo.domain.refund.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    private Long price;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    //FIXME 어쩌면 ENUM?
    private String reason;

    private LocalDateTime refundAt;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deletedAt;

    public static Refund register(Payment payment, Long price, RefundStatus status, String reason) {
        Refund refund = new Refund();
        refund.payment = payment;
        refund.price = price;
        refund.status = status;
        refund.reason = reason;
        refund.refundAt = LocalDateTime.now();
        refund.deleted = false;

        return refund;
    }

    public void delete() {
        deleted = true;
        deletedAt = LocalDateTime.now();
    }

    public void restore() {
        deleted = false;
        deletedAt = null;
    }
}
