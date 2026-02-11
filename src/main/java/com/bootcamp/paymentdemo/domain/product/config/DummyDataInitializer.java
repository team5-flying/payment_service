package com.bootcamp.paymentdemo.domain.product.config;

import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.entity.ProductStatus;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile({"dev", "local", "test"}) // 운영 환경 실행 제한
public class DummyDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DummyDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        productRepository.deleteAll();

        List<Product> products = new ArrayList<>();

        products.add(Product.register(
                "귤 1박스","과일", "제주도 달콤상큼한 귤", 50000L, 0L, ProductStatus.SOLDOUT));
        products.add(Product.register(
                "달걀 30개", "신선 식품", "싱싱한 1급 달걀",10000L, 50L, ProductStatus.SALES));
        products.add(Product.register(
                "거위털 롱패딩", "패션/잡화", "러시아에서도 버틸 수 있는 따뜻한 롱패딩", 200000L, 90L, ProductStatus.SALES));
        products.add(Product.register(
                "샘성 노트북", "전자 제품", "신기술 탑재 2026 신상 노트북", 2100000L, 150L, ProductStatus.SALES));
        productRepository.saveAll(products);
    }
}
