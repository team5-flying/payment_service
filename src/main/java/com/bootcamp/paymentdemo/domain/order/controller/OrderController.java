package com.bootcamp.paymentdemo.domain.order.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderConfirmResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateRequest;
import com.bootcamp.paymentdemo.domain.order.dto.OrderCreateResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderGetResponse;
import com.bootcamp.paymentdemo.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> save(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.save(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderGetResponse>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAll());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderGetResponse> findOne(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOne(orderId));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<BaseResponse<OrderConfirmResponse>> confirm(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(String.valueOf(HttpStatus.OK.value()), null, orderService.confirmOrder(orderId))
        );
    }
}
