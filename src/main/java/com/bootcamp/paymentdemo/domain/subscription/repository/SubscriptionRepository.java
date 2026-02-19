package com.bootcamp.paymentdemo.domain.subscription.repository;

import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    @Query("select s from Subscription s where s.status = :status and s.currentPeriodEnd <= :now")
    List<Subscription> findAllByStatusAndCurrentPeriodEndBefore(@Param("status")SubscriptionStatus status, @Param("now") LocalDateTime now);

    @Query("select s from Subscription s where s.status in :statuses and s.currentPeriodEnd <= :now")
    List<Subscription> findAllByStatusInAndCurrentPeriodEndBefore(@Param("statuses") List<SubscriptionStatus> statuses, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.member.memberId = :memberId " +
            "AND s.plan.planId = :planId " +
            "AND s.status IN ('ACTIVE', 'TRIALING', 'PAST_DUE')")
    boolean existsByMemberIdAndPlanIdAndActiveStatus(@Param("memberId") Long memberId, @Param("planId") String planId);
}
