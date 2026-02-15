package com.bootcamp.paymentdemo.domain.order.repository;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByMemberAndDeletedFalse(Member member);
    Optional<Order> findByOrderIdAndDeletedFalse(Long orderId);
    Optional<Order> findByOrderIdAndMemberAndDeletedFalse(Long orderId, Member member);
}
