package com.bootcamp.paymentdemo.domain.product.dto;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.product.entity.ProductStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductGetResponse extends Base {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Long price;
    private Long stock;
    private ProductStatus status;

    private ProductGetResponse(Long id, String name, String description, String category,  Long price, Long stock, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public static ProductGetResponse register(Long id, String name, String description, String category,  Long price, Long stock, ProductStatus status) {
        return new ProductGetResponse(id, name, description, category, price, stock, status);
    }

    // 프론트앤드 JSON ID 타입 String 변환
    public String getId() {
        return (id != null) ? id.toString() : "";
    }
}
