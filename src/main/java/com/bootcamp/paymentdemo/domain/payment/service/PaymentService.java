package com.bootcamp.paymentdemo.domain.payment.service;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
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
import com.bootcamp.paymentdemo.domain.payment.validator.PaymentValidator;
import com.bootcamp.paymentdemo.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;

    private final PaymentValidator paymentValidator;
    private final PointService pointService;

    // 결제 시도
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // 주문 및 상품 정보 조회
        Order order = orderRepository.findByOrderIdAndDeletedFalse(Long.valueOf(request.getOrderId()))
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_ORDER));
        List<ProductOrder> productOrderList = productOrderRepository.findByOrderAndDeletedFalse(order);

        // 검증 (재고, 포인트)
        paymentValidator.validateForCreate(order.getMember(), request.getPointsToUse(), order, productOrderList);

        // 포인트 사용 시 주문 정보 업데이트
        if (request.getPointsToUse() != null && request.getPointsToUse() > 0) {
            order.updateUsedPoints(request.getPointsToUse());

            Long earnedPoints = pointService.calculateEarnedPoints(
                    order.getTotalAmount()
                    , request.getPointsToUse()
                    , order.getMember().getGrade().getPointRate()
            );

            order.updateEarnedPoints(earnedPoints);
        }

        // 기존 결제 확인 (결제 창 닫았다가 재시도한 경우)
        Optional<Payment> existsPayment = paymentRepository.findByOrderId(Long.valueOf(request.getOrderId()));
        if (existsPayment.isPresent()) {
            Payment existingPayment = existsPayment.get();
            return CreatePaymentResponse.register(
                    true,
                    existingPayment.getPortOneId(),
                    existingPayment.getStatus().name()
            );
        }

        // 결제 생성, 저장, 응답
        Payment payment = Payment.register(order, request.getTotalAmount());
        Payment savedPayment = paymentRepository.save(payment);
        return CreatePaymentResponse.register(
                true
                , savedPayment.getPortOneId()
                , savedPayment.getStatus().name()
        );
    }

    // 결제 확정
    @Transactional(rollbackFor = Exception.class)
    public ConfirmPaymentResponse confirmPayment(String paymentId) {
        // 결제 및 주문 상품 조회
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(paymentId)
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PAYMENT));
        List<ProductOrder> productOrderList = productOrderRepository.findByOrderAndDeletedFalse(payment.getOrder());

        // 멱등성 검증 (이미 완료된 결제는 재처리 안함)
        if (payment.getStatus().equals(PaymentStatus.COMPLETE)) {
            return ConfirmPaymentResponse.register(true, payment.getPortOneId(), payment.getStatus().name());
        }

        Order order = payment.getOrder();
        Member member = order.getMember();

        // 재고 및 포인트 재검증
        paymentValidator.validateForConfirm(member, order, productOrderList);

        // 결제 및 주문 상태 변경
        payment.updateStatus(PaymentStatus.COMPLETE);
        order.updateStatus(OrderStatus.COMPLETE);

        // 재고 차감
        for (ProductOrder productOrder : productOrderList) {
            productOrder.getProduct().updateStock(productOrder.getQuantity());
        }

        // 포인트 적립 및 차감 처리
        pointService.processPointInOrder(member, order);

        // 회원 구매금액 누적, 등급 변경
        member.addTotalPriceAmount(payment.getPriceSnap());
        Grade newGrade = Grade.determineGrade(member.getTotalPriceAmount());
        member.updateGrade(newGrade);

        return ConfirmPaymentResponse.register(
                true,
                order.getOrderId().toString(),
                PaymentStatus.COMPLETE.name()
        );
    }
}
