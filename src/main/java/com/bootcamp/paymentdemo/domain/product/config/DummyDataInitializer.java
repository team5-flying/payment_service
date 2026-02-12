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

        if (productRepository.findAll().isEmpty()) {
            List<Product> products = new ArrayList<>();

            //FIXME Category Enum 으로 관리하자
            products.add(Product.register(
                    "귤 1박스", "FOOD", "제주도 달콤상큼한 귤", 1000L, 10L, ProductStatus.SOLDOUT));
            products.add(Product.register(
                    "달걀 한판", "FOOD", "싱싱한 1급 달걀", 1500L, 10L, ProductStatus.SALES));
            products.add(Product.register(
                    "거위털 롱패딩", "CLOTH", "러시아에서도 버틸 수 있는 따뜻한 롱패딩", 2000L, 10L, ProductStatus.SALES));
            products.add(Product.register(
                    "샘성 노트북", "ELECTRONIC", "신기술 탑재 2026 신상 노트북", 2500L, 10L, ProductStatus.SALES));
            products.add(Product.register(
                    "테스트", "테스트제품", "테스트 제품입니다", 1000L, 100L, ProductStatus.SALES));
            productRepository.saveAll(products);
        }

    }
}
