package com.bootcamp.paymentdemo.domain.order.config;

import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmScheduler {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    // 오전 10시 확정
    @Scheduled(cron = "0 0 10 * * *")

    // 테스트 케이스 (매분 30초)
    //@Scheduled(cron = "30 * * * * *")
    public void confirmOrderSchedule() {
        // 7일전 데이터부터 조회
        List<Order> orderList = orderRepository.findByStatusAndUpdatedAtBefore(OrderStatus.COMPLETE, LocalDateTime.now().minusDays(7));

        // 테스트 케이스 (30초 전 데이터부터 조회)
        //List<Order> orderList = orderRepository.findByStatusAndUpdatedAtBefore(OrderStatus.COMPLETE, LocalDateTime.now().minusSeconds(30));

        if(orderList.isEmpty()) {
            return;
        }

        orderList.forEach(
            order -> {
                try {
                    orderService.confirmOrder(order.getOrderId());
                    log.info("주문 확정 스케줄러 작동\n orderId - {}", order.getOrderId());
                } catch (Exception e) {
                    log.error("주문 확정 스케줄러 실패\n orderId - {}\n message - {}", order.getOrderId(), e.getMessage());
                }
            }
        );
    }
}
