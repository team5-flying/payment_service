package com.bootcamp.paymentdemo.domain.product.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 225)
    private String description;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long stock;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private LocalDateTime deletedAt;

    public static Product register(
            String name,
            String category,
            String description,
            Long price,
            Long stock,
            ProductStatus status
    ) {
        Product product = new Product();

        product.productUid = UUID.randomUUID().toString();
        product.name = name;
        product.category = category;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.status = status;
        product.deleted = false;
        product.deletedAt = null;

        return product;
    }

    public void updateStock(Long stock) {
        this.stock -= stock;

        if(this.stock > 0) {
            this.status = ProductStatus.SALES;
        } else {
            this.status = ProductStatus.SOLDOUT;
        }
    }
}
