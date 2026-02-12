package com.bootcamp.paymentdemo.domain.product.repository;

import com.bootcamp.paymentdemo.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository <Product,Long> {
    // 연속 결제에 의한 재고 데이터 처리 오류 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.deleted = false")
    Optional<Product> findByIdWithLock(Long productId);
}
