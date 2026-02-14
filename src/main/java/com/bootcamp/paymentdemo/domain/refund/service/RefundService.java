package com.bootcamp.paymentdemo.domain.refund.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;

import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLog;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLogStatus;
import com.bootcamp.paymentdemo.domain.point.repository.MemberPointLogRepository;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
    private final PaymentRepository paymentRepository;
    private final MemberPointLogRepository memberPointLogRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRefund(String portOneId) {
        // 중복 환불 방지 락
        Payment payment = paymentRepository.findByPortOneIdWithLock(portOneId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("이미 취소된 결제입니다. portOneId: {}", portOneId);
            return;
        }

        validateRefundAvailability(payment);

        Order order = payment.getOrder();
        Member member = order.getMember();

        // 상태 변경
        payment.updateStatus(PaymentStatus.CANCELLED);
        order.updateStatus(OrderStatus.CANCELLED);

        // 누적 결제 금액 차감
        if (payment.getPriceSnap() != null) {
            member.subtractTotalPriceAmount(order.getFinalAmount());
        }

        // 등급 재계산
        Grade newGrade = Grade.determineGrade(member.getTotalPriceAmount());
        member.updateGrade(newGrade);
        handlePointRefund(member, order);

        // 재고 복구
        List<ProductOrder> productOrders = productOrderRepository.findByOrderAndDeletedFalse(order);
        for (ProductOrder po : productOrders) {
            // 재고 복구 시에도 동시성 이슈 방지를 위해 락
            Product product = productRepository.findByIdWithLock(po.getProduct().getId())
                    .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PRODUCT));
            // 주문 시 소모했던 수량(positive)을 빼기 위해 음수(-) 전달
            product.updateStock(-po.getQuantity());
        }
    }

    private void validateRefundAvailability(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new ServiceErrorException(ErrorEnum.ERR_ALREADY_CANCELLED);
        }

        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_REFUND_STATUS);
        }
    }

    private void handlePointRefund(Member member, Order order) {
        // 사용 포인트 돌려주기
        if (order.getUsedPoints() > 0) {
            member.addPoint(order.getUsedPoints());
            memberPointLogRepository.save(MemberPointLog.create(
                    order.getOrderNumber(), order.getUsedPoints(), MemberPointLogStatus.RECOVER, member));
        }

        // 적립 포인트 취소(회수)
        if (order.getEarnedPoints() > 0) {
            member.minusPoint(order.getEarnedPoints());
            memberPointLogRepository.save(MemberPointLog.create(
                    order.getOrderNumber(), order.getEarnedPoints(), MemberPointLogStatus.CANCEL_EARN, member));
        }
    }
}
