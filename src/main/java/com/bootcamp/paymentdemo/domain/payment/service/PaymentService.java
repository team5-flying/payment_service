package com.bootcamp.paymentdemo.domain.payment.service;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.dto.ConfirmPaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentRequest;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLog;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLogStatus;
import com.bootcamp.paymentdemo.domain.point.repository.MemberPointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.*;
import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_ALREADY_PAYMENT_COMPLETED;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductOrderRepository productOrderRepository;
    private final MemberPointLogRepository memberPointLogRepository;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        Order order = orderRepository.findById(Long.valueOf(request.getOrderId())).orElseThrow(
                () -> new ServiceErrorException(ERR_NOT_FOUND_ORDER)
        );

        Payment payment = Payment.register(
                order
                , request.getTotalAmount()
        );

        // 소모 포인트가 있을 경우 - 실 결제 금액 계산, 계산된 내역으로 포인트 적립
        if(request.getPointsToUse() != null && request.getPointsToUse() > 0) {
            order.updateUsedPoints(request.getPointsToUse());
            order.updateEarnedPoints((long) Math.floor((order.getTotalAmount() - request.getPointsToUse()) * order.getMember().getGrade().getPointRate() * 0.01));
        }

        // 기존 주문 건이 있을 경우 (결제 창 닫아 취소 했을 경우 해당 주문 건 재활용)
        Optional<Payment> setPayment = paymentRepository.findByOrderId(Long.valueOf(request.getOrderId()));
        if(setPayment.isPresent()) {
            Payment existingPayment = setPayment.get();
            return CreatePaymentResponse.register(
                    true
                    , existingPayment.getPortOneId()
                    , existingPayment.getStatus().name()
            );
        } else {
            Payment savedPayment = paymentRepository.save(payment);
            return CreatePaymentResponse.register(
                    true
                    , savedPayment.getPortOneId()
                    , savedPayment.getStatus().name()
            );
        }
    }

    @Transactional
    public ConfirmPaymentResponse confirmPayment(String paymentId, String email) {
        // TODO 실패의 경우?
        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_MEMBER));
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(paymentId).orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PAYMENT));
        List<ProductOrder> productOrderList = productOrderRepository.findByOrderAndDeletedFalse(payment.getOrder());

        // 멱등성 검증 (이미 처리된 상태 일 경우 throw)
        if (payment.getStatus().equals(PaymentStatus.COMPLETE)) {
            throw new ServiceErrorException(ERR_ALREADY_PAYMENT_COMPLETED);
        }

        // 결제 확정 상태로 변경
        payment.updateStatus(PaymentStatus.COMPLETE);

        // 주문 확정 상태로 변경
        payment.getOrder().updateStatus(OrderStatus.COMPLETE);

        // 재고 변경
        // FIXME 주문을 확정하기 전에 상품의 재고가 모자란 경우가 분명히 발생 할 수 있음, 이 부분은 어떻게 제어할 것인지 생각
        for (ProductOrder productOrder : productOrderList) {
            productOrder.getProduct().updateStock(productOrder.getQuantity());
        }

        // 고객 누적 구매금액 반영
        // FIXME 누적 구매금액 적용할 때 등급 반영 로직 추가되어야함
        member.addTotalPriceAmount(payment.getPriceSnap());

        // 고객 포인트 적립, 포인트 적립 이력 생성
        member.addPoint(payment.getOrder().getEarnedPoints());
        MemberPointLog savedSavePointLog = MemberPointLog.create(payment.getOrder().getOrderNumber(), payment.getOrder().getEarnedPoints(), MemberPointLogStatus.SAVE, member);
        memberPointLogRepository.save(savedSavePointLog);

        // 고객 포인트 소모, 포인트 소모 이력 생성
        if (payment.getOrder().getUsedPoints() > 0) {
            member.minusPoint(payment.getOrder().getUsedPoints());
            MemberPointLog savedUsePointLog = MemberPointLog.create(payment.getOrder().getOrderNumber(), payment.getOrder().getUsedPoints(), MemberPointLogStatus.USE, member);
            memberPointLogRepository.save(savedUsePointLog);
        }

        return ConfirmPaymentResponse.register(true, payment.getOrder().getOrderId().toString(), PaymentStatus.COMPLETE.name());
    }
}
