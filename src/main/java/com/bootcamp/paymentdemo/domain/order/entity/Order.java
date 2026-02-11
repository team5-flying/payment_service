package com.bootcamp.paymentdemo.domain.order.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long finalAmount;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long usedPoints;

    @Column(nullable = false)
    private Long earnedPoints;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private boolean deleted;

    @OneToOne(mappedBy = "order")
    private Payment payment;

    private LocalDateTime orderAt;
    private LocalDateTime deletedAt;

    public static Order register(
            Member member,
            Long totalAmount,
            Long usedPoints,
            Long finalAmount,
            Long earnedPoints,
            Long quantity
    ) {
        Order order = new Order();

        order.member = member;
        order.totalAmount = totalAmount;
        order.usedPoints = usedPoints == null ? 0 : usedPoints; // null 이면 0으로 처리
        order.finalAmount = finalAmount;
        order.earnedPoints = earnedPoints == null ? 0 : earnedPoints; // null 이면 0으로 처리
        order.quantity = quantity;
        order.currency = "KRW";
        order.orderNumber = "ORDER-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)+ UUID.randomUUID().toString().substring(0, 8);
        order.status = OrderStatus.PENDING;
        order.deleted = false;
        order.deletedAt = null;

        return order;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
        if(status == OrderStatus.REFUNDED) {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
        }
    }

    // 포인트 적립 업데이트
    public void updateEarnedPoints(Long earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    // 포인트 소모 업데이트
    // 실 결제 금액도 같이 수정됨
    public void updateUsedPoints(Long usedPoints) {
        this.usedPoints = usedPoints;
        this.finalAmount = totalAmount - usedPoints;
    }

}
