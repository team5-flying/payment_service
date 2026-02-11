package com.bootcamp.paymentdemo.domain.order.repository;

import com.bootcamp.paymentdemo.domain.order.entity.OrderNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderNumberSequenceRepository extends JpaRepository<OrderNumberSequence, String> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s From OrderNumberSequence s WHERE s.sequenceDate = :date")
    Optional<OrderNumberSequence> findWithLockBySequenceDate(@Param("date") String date);
}
