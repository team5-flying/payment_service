package com.bootcamp.paymentdemo.domain.order.controller;

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

//    // TODO 결제 테스트용, 삭제 필요
//    @PostMapping
//    public ResponseEntity<CreateOrderResponse> createOrders() {
//        return ResponseEntity.status(HttpStatus.OK).body(
//                new CreateOrderResponse(
//                        "1"
//                        , 1000L
//                        , "ORDER-20260209-0001"
//                )
//        );
//    }
//
//    // TODO 결제 테스트용, 삭제 필요
//    @GetMapping
//    public ResponseEntity<List<SearchOrderResponse>> searchOrders() {
//        return ResponseEntity.status(HttpStatus.OK).body(
//                List.of(
//                        new SearchOrderResponse(
//                                "ORDER-20260209-0001"
//                                , "1"
//                                , 1000
//                                , 0
//                                , 1000
//                                , 10
//                                , "KRW"
//                                , "PENDING"
//                                , LocalDateTime.now().toString()
//                        )
//                )
//        );
//    }
}
