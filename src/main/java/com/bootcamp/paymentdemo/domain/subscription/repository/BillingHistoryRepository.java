package com.bootcamp.paymentdemo.domain.subscription.repository;

import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillingHistoryRepository extends JpaRepository<BillingHistory, String> {

    @Query("select b from BillingHistory  b where b.subscription.subscriptionId = :subscriptionId order by b.attemptDate desc")
    List<BillingHistory> findAllBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    Optional<BillingHistory> findByPortOneId(String portOneId);
}
