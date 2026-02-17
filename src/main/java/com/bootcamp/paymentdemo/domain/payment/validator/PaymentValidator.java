package com.bootcamp.paymentdemo.domain.payment.validator;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.*;

// 검증 컴포넌트
@Component
@RequiredArgsConstructor
public class PaymentValidator {
    private final ProductRepository productRepository;

    // 결제 시도 검증
    // request에 포인트가 있으면 request 값으로 검증
    // request에 포인트가 없으면 order에 저장된 값으로 검증 (결제창만 닫았다 사용하는 기존 결제 재사용 건일 수 있으므로 찾아봐야함)
    public void validateForCreate(Member member, Long requestedPoints, Order order, List<ProductOrder> productOrderList) {
        // 포인트 검증: request 값 우선, 없으면 order에 저장된 값 사용
        Long pointsToValidate = (requestedPoints != null && requestedPoints > 0) ? requestedPoints : order.getUsedPoints();

        validateZeroActualPrice(order);
        validatePoint(member, pointsToValidate);
        validateStock(productOrderList);
    }

    // 결제 확정 시 검증
    public void validateForConfirm(Member member, Order order, List<ProductOrder> productOrderList) {
        if (order.getUsedPoints() > 0) {
            validatePointAtConfirm(member, order.getUsedPoints());
        }

        validateStockAtConfirm(productOrderList);
    }

    // 포인트 부족 검증
    private void validatePoint(Member member, Long requiredPoints) {
        if (requiredPoints > 0 && member.getPoint() < requiredPoints) {
            throw new ServiceErrorException(ERR_NOT_ENOUGH_POINT);
        }
    }

    // 재고 부족 검증
    private void validateStock(List<ProductOrder> productOrderList) {
        for (ProductOrder productOrder : productOrderList) {
            if (productOrder.getProduct().getStock() < productOrder.getQuantity()) {
                throw new ServiceErrorException(ERR_NOT_ENOUGH_STOCK);
            }
        }
    }

    // 포인트 부족 검증_확정
    private void validatePointAtConfirm(Member member, Long requiredPoints) {
        if (requiredPoints > 0 && member.getPoint() < requiredPoints) {
            throw new ServiceErrorException(ERR_NOT_ENOUGH_POINT);
        }
    }

    // 재고 부족 검증_확정
    private void validateStockAtConfirm(List<ProductOrder> productOrderList) {
        for (ProductOrder productOrder : productOrderList) {
            Product product = productRepository.findByIdWithLock(productOrder.getProduct().getId())
                    .orElseThrow(() -> new ServiceErrorException(ERR_NOT_FOUND_PRODUCT));

            if (product.getStock() < productOrder.getQuantity()) {
                throw new ServiceErrorException(ERR_NOT_ENOUGH_STOCK);
            }
        }
    }

    // 결제 시도시 0원 금액 검증
    // Portone 우회 결제가 필요한데, front-end 수정 필요로 우선 막음
    private void validateZeroActualPrice(Order order) {
        if (order.getFinalAmount() <= 0) {
            throw new ServiceErrorException(ERR_ZERO_ACTUAL_PRICE);
        }
    }
}
