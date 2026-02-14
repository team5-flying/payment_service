package com.bootcamp.paymentdemo.domain.subscription.repository;

import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    @Query("select s from Subscription s" +
            "where s.status = :status and s.currentPeriodEnd <= :now")
    List<Subscription> findAllByStatusAndCurrentPeriodEndBefore(@Param("status")SubscriptionStatus status, @Param("now") LocalDateTime now);
}
