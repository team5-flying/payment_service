package com.bootcamp.paymentdemo.domain.order.entity;


import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "products_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOrder extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productOrderId;

    @Column(nullable = false, length = 100)
    private String nameSnap;

    @Column(nullable = false)
    private Long priceSnap;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="order_id", nullable = false)
    private Order order;

    private LocalDateTime deletedAt;

    public static ProductOrder register(
            Product product,
            Order order,
            String nameSnap,
            Long priceSnap,
            Long quantity
    ) {
        ProductOrder productOrder = new ProductOrder();

        productOrder.product = product;
        productOrder.order = order;
        productOrder.nameSnap = nameSnap;
        productOrder.priceSnap = priceSnap;
        productOrder.quantity = quantity;
        productOrder.deleted = false;
        productOrder.deletedAt = null;

        return productOrder;
    }
}
