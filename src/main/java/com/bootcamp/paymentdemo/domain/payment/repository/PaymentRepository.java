package com.bootcamp.paymentdemo.domain.payment.repository;

import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.order o
            WHERE p.deleted = false
            AND p.status = 'PENDING'
            AND o.orderId = :orderId
          """)
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPortOneIdAndDeletedFalse(String portOneId);
}
