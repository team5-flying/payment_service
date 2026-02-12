package com.bootcamp.paymentdemo.domain.payment.validator;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_NOT_ENOUGH_POINT;
import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_NOT_ENOUGH_STOCK;

// 검증 컴포넌트
@Component
public class PaymentValidator {

    // 결제 시도 검증
    // - request에 포인트가 있으면 request 값으로 검증
    // - request에 포인트가 없으면 order에 저장된 값으로 검증 (기존 결제 재사용)
    public void validateForCreate(Member member, Long requestedPoints, Order order, List<ProductOrder> productOrderList) {
        // 포인트 검증: request 값 우선, 없으면 order에 저장된 값 사용
        Long pointsToValidate = (requestedPoints != null && requestedPoints > 0) ? requestedPoints : order.getUsedPoints();

        validatePoint(member, pointsToValidate);
        validateStock(productOrderList);
    }

    // 결제 확정 시 검증
    public void validateForConfirm(Member member, Order order, List<ProductOrder> productOrderList) {
        if (order.getUsedPoints() > 0) {
            validatePoint(member, order.getUsedPoints());
        }
        validateStock(productOrderList);
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
}
