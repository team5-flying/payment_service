package com.bootcamp.paymentdemo.domain.plan.service;

import com.bootcamp.paymentdemo.domain.plan.dto.PlanResponse;
import com.bootcamp.paymentdemo.domain.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(PlanResponse::from)
                .collect(Collectors.toList());
    }
}
