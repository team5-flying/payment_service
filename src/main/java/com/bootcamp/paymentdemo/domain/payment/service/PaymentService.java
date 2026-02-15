package com.bootcamp.paymentdemo.domain.payment.service;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.dto.PaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentRequest;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.payment.validator.PaymentValidator;
import com.bootcamp.paymentdemo.domain.point.service.PointService;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentValidator paymentValidator;

    private final PointService pointService;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;

    // 결제 시도
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // 주문 및 상품 정보 조회
        Order order = orderRepository.findByOrderIdAndDeletedFalse(Long.valueOf(request.getOrderId()))
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_ORDER));
        List<ProductOrder> productOrderList = productOrderRepository.findByOrderAndDeletedFalse(order);

        // 검증 (재고, 포인트)
        paymentValidator.validateForCreate(order.getMember(), request.getPointsToUse(), order, productOrderList);

        // 포인트 사용 정보에 따라 포인트 적립
        Long usedPoints = (request.getPointsToUse() != null && request.getPointsToUse() > 0) ? request.getPointsToUse() : 0L;
        order.updateUsedPoints(usedPoints);

        Long earnedPoints = pointService.calculateEarnedPoints(
                order.getTotalAmount(),
                usedPoints,
                order.getMember().getGrade().getPointRate()
        );
        order.updateEarnedPoints(earnedPoints);

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

        // 결제 생성, 저장
        Payment payment = Payment.register(order, order.getFinalAmount());
        Payment savedPayment = paymentRepository.save(payment);

        // 0원 결제 (포인트 전액 결제)
        if (order.getFinalAmount() == 0) {
            log.info("0원 결제 확정 처리: portOneId - {}", savedPayment.getPortOneId());
            PaymentResponse confirmResult = confirmPayment(savedPayment.getPortOneId());
            return CreatePaymentResponse.register(
                    confirmResult.getSuccess()
                    , savedPayment.getPortOneId()
                    , confirmResult.getStatus()
            );
        }

        return CreatePaymentResponse.register(
                true
                , savedPayment.getPortOneId()
                , savedPayment.getStatus().name()
        );
    }

    // 결제 확정
    @Transactional
    public PaymentResponse confirmPayment(String paymentId) {
        Payment payment = paymentRepository.findByPortOneIdWithLock(paymentId)
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PAYMENT));

        List<ProductOrder> productOrderList = productOrderRepository.findByOrderAndDeletedFalse(payment.getOrder());

        // 멱등성 처리
        if (payment.getStatus().equals(PaymentStatus.COMPLETE)) {
            return PaymentResponse.register(true, payment.getPortOneId(), payment.getStatus().name());
        }

        if (payment.getStatus().equals(PaymentStatus.FAIL)) {
            return PaymentResponse.register(false, payment.getPortOneId(), payment.getStatus().name());
        }

        Order order = payment.getOrder();
        Member member = order.getMember();

        try {
            // 재고 및 포인트 재검증
            paymentValidator.validateForConfirm(member, order, productOrderList);

            // 결제 및 주문 상태 변경
            payment.updateStatus(PaymentStatus.COMPLETE);
            order.updateStatus(OrderStatus.COMPLETE);

            // 동시에 접근 시 여러 상품의 락을 획득할 때는 항상 일정한 순서로 진입해야 데드락을 막을 수 있음 (Lock Ordering)
            productOrderList.sort(Comparator.comparing(productOrder -> productOrder.getProduct().getId()));

            // 재고 차감
            for (ProductOrder productOrder : productOrderList) {
                Product product = productRepository.findByIdWithLock(productOrder.getProduct().getId())
                        .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PRODUCT));
                product.updateStock(productOrder.getQuantity());
            }

            // 포인트 적립 및 차감 처리
            pointService.processPointInOrder(member, order);

            // 회원 구매금액 누적, 등급 변경
            member.addTotalPriceAmount(order.getFinalAmount());
            Grade newGrade = Grade.determineGrade(member.getTotalPriceAmount());
            member.updateGrade(newGrade);
        } catch (Exception e) {
            log.error("결제 확정 진행 중 오류 : {}", e.getMessage());
            payment.updateStatus(PaymentStatus.FAIL);

            // PaymentId 가 기록된 채로 실패한 경우, 재시도가 불가함 (paymentId 재활용 처리라 처리 불가 발생)
            order.updateStatus(OrderStatus.FAIL);

            return PaymentResponse.register(false, payment.getPortOneId(), PaymentStatus.FAIL.name());
        }

        return PaymentResponse.register(
                true,
                order.getOrderId().toString(),
                PaymentStatus.COMPLETE.name()
        );
    }
}
