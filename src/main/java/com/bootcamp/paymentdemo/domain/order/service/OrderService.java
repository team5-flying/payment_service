package com.bootcamp.paymentdemo.domain.order.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateRequest;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderGetResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderItemRequest;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderNumberSequence;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.OrderNumberSequenceRepository;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;
    private final MemberRepository memberRepository;
    private final OrderNumberSequenceRepository orderNumberSequenceRepository;

    @Transactional
    public OrderCreateResponse save(OrderCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );

        // 주문번호 생성
        String orderNumber = createOrderNumber();

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
        long earnedPoints = (int) (finalAmount * (grade.getPointRate() / 100.0));

        Order order = Order.register(
                member
                , calculateTotalAmount
                , usedPoints
                , finalAmount
                , earnedPoints
                , totalQuantity
                , "KRW"
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
                }).collect(Collectors.toList());

        productOrderRepository.saveAll(items);

        return OrderCreateResponse.register(
                savedOrder.getMember().getMemberId()
                , savedOrder.getOrderId()
                , savedOrder.getTotalAmount()
                , savedOrder.getOrderNumber()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderGetResponse> findAll() {
        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(order -> !order.isDeleted()).toList(); // 삭제된 주문 제외

        return orders.stream()
                .map(order ->
                        OrderGetResponse.register(
                                order.getOrderId()
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
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ORDER)
        );

        return OrderGetResponse.register(
                order.getOrderId()
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

    @Transactional
    public String createOrderNumber() {
        String dateSeq = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        OrderNumberSequence sequence = orderNumberSequenceRepository.findWithLockBySequenceDate(dateSeq)
                .orElseGet(() -> orderNumberSequenceRepository.save(new OrderNumberSequence(dateSeq)));

        String orderNumber = String.format("ORDER-%s-%06d", dateSeq, sequence.getLastNumber());

        sequence.increment();

        return orderNumber;
    }
}
