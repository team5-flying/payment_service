package com.bootcamp.paymentdemo.domain.point.service;

import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLog;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLogStatus;
import com.bootcamp.paymentdemo.domain.point.repository.MemberPointLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bootcamp.paymentdemo.common.exception.ErrorEnum.ERR_SAVED_DATA_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberPointLogRepository memberPointLogRepository;

    // 적립 포인트 계산
    public Long calculateEarnedPoints(Long totalAmount, Long usedPoints, int pointRate) {
        return (long) Math.floor((totalAmount - usedPoints) * pointRate * 0.01);
    }

    // 주문에서의 포인트 적립 및 차감
    public void processPointInOrder(Member member, Order order) {
        try {
            // 포인트 적립
            earnPoint(member, order);

            // 포인트 차감 (사용한 포인트가 있는 경우만)
            if (order.getUsedPoints() > 0) {
                usePoint(member, order);
            }
        } catch (Exception e) {
            log.error("포인트 로그 저장 실패: orderId={}, message={}", order.getOrderId(), e.getMessage(), e);
            throw new ServiceErrorException(ERR_SAVED_DATA_FAILED);
        }
    }

    // 포인트 적립 및 로그 생성
    private void earnPoint(Member member, Order order) {
        member.addPoint(order.getEarnedPoints());
        MemberPointLog earnLog = MemberPointLog.create(
                order.getOrderNumber(),
                order.getEarnedPoints(),
                MemberPointLogStatus.SAVE,
                member
        );
        memberPointLogRepository.save(earnLog);
    }

    // 포인트 차감 및 로그 생성
    private void usePoint(Member member, Order order) {
        member.minusPoint(order.getUsedPoints());
        MemberPointLog useLog = MemberPointLog.create(
                order.getOrderNumber(),
                order.getUsedPoints(),
                MemberPointLogStatus.USE,
                member
        );
        memberPointLogRepository.save(useLog);
    }
}
