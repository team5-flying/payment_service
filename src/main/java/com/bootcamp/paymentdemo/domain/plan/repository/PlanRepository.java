package com.bootcamp.paymentdemo.domain.plan.repository;

import com.bootcamp.paymentdemo.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, String> {
}
