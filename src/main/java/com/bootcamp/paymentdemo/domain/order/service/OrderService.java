package com.bootcamp.paymentdemo.domain.order.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateRequest;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderGetResponse;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    // 구현 : 주문 생성, 목록 조회, 단건 조회
    @Transactional
    public OrderCreateResponse save(OrderCreateRequest request) {

        // TODO 테스트용, 삭제 필요
        Long memberId = 1L;

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );

        Order order = Order.register (
                 member
                , request.getTotalAmount()
                , request.getUsePoints()
                , request.getFinalAmount()
                , request.getEarnedPoints()
                , request.getQuantity()
                ,"KRW"
        );

        Order savedOrder = orderRepository.save(order);

        // TODO : 상품 리스트 (product_orders) 저장 로직 추가 (진행중)

        return OrderCreateResponse.register(
                savedOrder.getOrderId()
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
                                , order.getUsePoints()
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
                , order.getUsePoints()
                , order.getFinalAmount()
                , order.getEarnedPoints()
                , order.getCurrency()
                , order.getStatus()
                , order.getCreatedAt()
                , order.isDeleted()
                , order.getDeletedAt()
        );
    }
}
