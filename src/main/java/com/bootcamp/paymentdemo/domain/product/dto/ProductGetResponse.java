package com.bootcamp.paymentdemo.domain.product.dto;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.product.entity.ProductStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductGetResponse extends Base {
    private final Long id;
    private final String name;
    private final String description;
    private final String category;
    private final Long price;
    private final Long stock;
    private final ProductStatus status;

    public static ProductGetResponse register(Long id, String name, String description, String category, Long price, Long stock, ProductStatus status) {
        return new ProductGetResponse(id, name, description, category, price, stock, status);
    }

    // 프론트앤드 JSON ID 타입 String 변환
    public String getId() {
        return (id != null) ? id.toString() : "";
    }
}
