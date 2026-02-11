package com.bootcamp.paymentdemo.domain.order.repository;

import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByOrderAndDeletedFalse(Order order);
}
