package com.bootcamp.paymentdemo.domain.refund.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;

import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLog;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLogStatus;
import com.bootcamp.paymentdemo.domain.point.repository.MemberPointLogRepository;
import com.bootcamp.paymentdemo.domain.refund.entity.Refund;
import com.bootcamp.paymentdemo.domain.refund.entity.RefundStatus;
import com.bootcamp.paymentdemo.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final MemberPointLogRepository memberPointLogRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void processRefund(String paymentId) {
        Payment payment = paymentRepository.findByPortOneId(paymentId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));
        Order order = payment.getOrder();
        Member member = order.getMember();

        validateRefundAvailability(payment);

        // 환불 기록 및 상태 변경
        Refund refund = Refund.register(payment, payment.getPriceSnap(), RefundStatus.REFUNDED, "웹훅 취소");
        refundRepository.save(refund);
        payment.updateStatus(PaymentStatus.REFUNDED);
        order.updateStatus(OrderStatus.REFUNDED);

        // 누적 결제 금액 차감
        member.subtractTotalPriceAmount(order.getFinalAmount());

        // 등급 재계산 호출
        updateMemberGrade(member);

        // 포인트 복구
        if (order.getUsedPoints() > 0) {
            member.addPoint(order.getUsedPoints());
            memberPointLogRepository.save(MemberPointLog.create(order.getOrderNumber(), order.getUsedPoints(), MemberPointLogStatus.RECOVER, member));
        }

        // 적립 취소
        if (order.getEarnedPoints() > 0) {
            member.minusPoint(order.getEarnedPoints());
            memberPointLogRepository.save(MemberPointLog.create(order.getOrderNumber(), order.getEarnedPoints(), MemberPointLogStatus.CANCEL_EARN, member));
        }
    }

    private void validateRefundAvailability(Payment payment) {
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ServiceErrorException(ErrorEnum.ERR_ALREADY_REFUNDED);
        }

        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_REFUND_STATUS);
        }
    }

    private void updateMemberGrade(Member member) {
        Integer totalAmount = member.getTotalPriceAmount();
        Grade newGrade;

        if (totalAmount >= 300000) {
            newGrade = Grade.DIAMOND;
        } else if (totalAmount >= 200000) {
            newGrade = Grade.GOLD;
        } else if (totalAmount >= 100000) {
            newGrade = Grade.SILVER;
        } else {
            newGrade = Grade.BRONZE;
        }

        member.updateGrade(newGrade);
    }
}
