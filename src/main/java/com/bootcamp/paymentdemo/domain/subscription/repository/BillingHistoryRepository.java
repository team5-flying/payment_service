package com.bootcamp.paymentdemo.domain.subscription.repository;

import com.bootcamp.paymentdemo.domain.subscription.entity.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingHistoryRepository extends JpaRepository<BillingHistory, String> {
}
