package com.bootcamp.paymentdemo.domain.plan.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.plan.dto.PlanResponse;
import com.bootcamp.paymentdemo.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<PlanResponse>>> getPlans() {
        List<PlanResponse> plans = planService.getAllPlans();
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "플랜 목록 조회 성공", plans));
    }
}
