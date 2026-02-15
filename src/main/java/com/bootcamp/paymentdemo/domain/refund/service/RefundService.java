package com.bootcamp.paymentdemo.domain.refund.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;

import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
    private final PaymentRepository paymentRepository;
    private final MemberPointLogRepository memberPointLogRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PaymentResponse processRefund(String portOneId) {
        // 중복 환불 방지 락
        Payment payment = paymentRepository.findByPortOneIdWithLock(portOneId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

        // 멱등성 처리
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("이미 취소된 결제, portOneId: {}", portOneId);
            return PaymentResponse.register(true, portOneId, payment.getStatus().name());
        }

        // 결제 취소를 위한 상태 체크 - 결제가 된 건만 수행
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

        // 결제에 사용한 포인트 복구
        handlePointRefund(member, order);

        // 재고 복구
        List<ProductOrder> productOrders = productOrderRepository.findByOrderAndDeletedFalse(order);

        // 동시에 접근 시 여러 상품의 락을 획득할 때는 항상 일정한 순서로 진입해야 데드락을 막을 수 있음 (Lock Ordering)
        productOrders.sort(Comparator.comparing(productOrder -> productOrder.getProduct().getId()));

        for (ProductOrder po : productOrders) {
            Product product = productRepository.findByIdWithLock(po.getProduct().getId())
                    .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PRODUCT));

            // 주문 시 소모했던 수량(positive)을 빼기 위해 음수(-) 전달
            product.updateStock(-po.getQuantity());
        }

        return PaymentResponse.register(
                true,
                order.getOrderId().toString(),
                PaymentStatus.COMPLETE.name()
        );
    }

    private void validateRefundAvailability(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_REFUND_STATUS);
        }

        if (payment.getOrder().getStatus() == OrderStatus.CONFIRMED) {
            throw new ServiceErrorException(ErrorEnum.ERR_ALREADY_CONFIRMED);
        }
    }

    private void handlePointRefund(Member member, Order order) {
        // 사용 포인트 돌려주기
        if (order.getUsedPoints() > 0) {
            member.addPoint(order.getUsedPoints());
            memberPointLogRepository.save(MemberPointLog.create(
                    order.getOrderNumber(), order.getUsedPoints(), MemberPointLogStatus.RECOVER, member));
        }
    }
}
