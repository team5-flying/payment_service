package com.bootcamp.paymentdemo.domain.payment.repository;

import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.order o
            WHERE p.deleted = false
            AND o.orderId = :orderId
          """)
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPortOneIdAndDeletedFalse(String portOneId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p " +
            "from Payment p " +
            "where p.portOneId = :portOneId " +
            "and p.deleted = false")
    Optional<Payment> findByPortOneIdWithLock(@Param("portOneId") String portOneId);
}
