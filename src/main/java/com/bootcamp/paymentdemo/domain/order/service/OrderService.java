package com.bootcamp.paymentdemo.domain.order.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.order.dto.*;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderNumberSequence;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.OrderNumberSequenceRepository;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.point.service.PointService;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;
    private final MemberRepository memberRepository;
    private final OrderNumberSequenceRepository orderNumberSequenceRepository;
    private final PointService pointService;

    @Transactional
    public OrderCreateResponse save(OrderCreateRequest request) {
        Member member = getCurrentMember();

        // 주문번호 생성
        String dateSeq = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        OrderNumberSequence sequence = orderNumberSequenceRepository.findWithLockBySequenceDate(dateSeq)
                .orElseGet(() -> orderNumberSequenceRepository.save(new OrderNumberSequence(dateSeq)));

        String orderNumber = String.format("ORDER-%s-%06d", dateSeq, sequence.getLastNumber());

        sequence.increment();

        // 서버측 총액 계산 한번 더 진행 (보안)
        long calculateTotalAmount = 0;
        long totalQuantity = 0;

        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow(
                    () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PRODUCT)
            );

            // 재고 확인
            if (product.getStock() < item.getQuantity()) {
                throw new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PRODUCT);
            }
            calculateTotalAmount += product.getPrice() * item.getQuantity();
            totalQuantity += item.getQuantity();
        }

        // 포인트 및 최종 결제 금액 계산
        Grade grade = member.getGrade();

        long usedPoints = 0;
        long finalAmount = calculateTotalAmount - usedPoints;
        long earnedPoints = (long) (finalAmount * (grade.getPointRate()/100.0));

        Order order = Order.register(
                member
                , calculateTotalAmount
                , usedPoints
                , finalAmount
                , earnedPoints
                , totalQuantity
                , orderNumber
        );

        Order savedOrder = orderRepository.save(order);

        // 상품 주문 리스트 저장
        List<ProductOrder> items = request.getItems()
                .stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElseThrow(
                            () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PRODUCT));

                    return ProductOrder.register(
                            product
                            , savedOrder
                            , product.getName()
                            , product.getPrice()
                            , item.getQuantity()
                    );
                })
                .collect(Collectors.toList());

        productOrderRepository.saveAll(items);

        return OrderCreateResponse.register(
                savedOrder.getMember().getMemberId()
                , String.valueOf(savedOrder.getOrderId())
                , savedOrder.getTotalAmount()
                , savedOrder.getOrderNumber()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderGetResponse> findAll() {

        Member member = getCurrentMember();

        List<Order> orders = orderRepository.findAllByMemberAndDeletedFalse(member);

        return orders.stream()
                .map(order ->
                        OrderGetResponse.register(
                                String.valueOf(order.getOrderId())
                                , order.getMember().getMemberId()
                                , order.getOrderNumber()
                                , order.getTotalAmount()
                                , order.getUsedPoints()
                                , order.getFinalAmount()
                                , order.getEarnedPoints()
                                , order.getCurrency()
                                , order.getStatus()
                                , order.getOrderAt()
                                , order.isDeleted()
                                , order.getDeletedAt()
                        )).toList();
    }

    @Transactional(readOnly = true)
    public OrderGetResponse findOne(Long orderId) {

        Member member = getCurrentMember();

        Order order = orderRepository.findByOrderIdAndMemberAndDeletedFalse(orderId, member).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ORDER)
        );

        return OrderGetResponse.register(
                String.valueOf(order.getOrderId())
                , order.getMember().getMemberId()
                , order.getOrderNumber()
                , order.getTotalAmount()
                , order.getUsedPoints()
                , order.getFinalAmount()
                , order.getEarnedPoints()
                , order.getCurrency()
                , order.getStatus()
                , order.getCreatedAt()
                , order.isDeleted()
                , order.getDeletedAt()
        );
    }

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );
    }

    // 주문 확정
    @Transactional
    public OrderConfirmResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_ORDER));

        // COMPLETE 건만 확정 하도록 수행
        if (order.getStatus() != OrderStatus.COMPLETE) {
            throw new ServiceErrorException(ERR_NOT_COMPLETE_ORDER);
        }

        Member member = memberRepository.findByIdWithLock(order.getMember().getMemberId()).orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_MEMBER));

        // 포인트 적립
        pointService.earnPoint(member, order);

        // 회원 구매금액 누적, 등급 변경
        member.addTotalPriceAmount(order.getFinalAmount());
        Grade newGrade = Grade.determineGrade(member.getTotalPriceAmount());
        member.updateGrade(newGrade);

        // 주문 상태 확정으로 변경
        order.updateStatus(OrderStatus.CONFIRMED);

        return OrderConfirmResponse.register(String.valueOf(orderId), OrderStatus.CONFIRMED.name());
    }
}
