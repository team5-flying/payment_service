package com.bootcamp.paymentdemo.domain.order.repository;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByMemberAndDeletedFalse(Member member);
    Optional<Order> findByOrderIdAndDeletedFalse(Long orderId);
    Optional<Order> findByOrderIdAndMemberAndDeletedFalse(Long orderId, Member member);
    List<Order> findByStatusAndOrderAtAfter(OrderStatus status, LocalDateTime dateTime);
}
