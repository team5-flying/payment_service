package com.bootcamp.paymentdemo.domain.product.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
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
        if (this.stock < stock) {
            log.error("상품 재고 변경 실패 : {}", "변경할 재고 = " + stock + ", [ " + this.name + " ] 의 재고 = " + this.stock);
            throw new ServiceErrorException(ErrorEnum.ERR_NOT_ENOUGH_STOCK);
        }

        this.stock -= stock;

        if(this.stock > 0) {
            this.status = ProductStatus.SALES;
        } else {
            this.status = ProductStatus.SOLDOUT;
        }
    }
}
