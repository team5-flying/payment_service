package com.bootcamp.paymentdemo.domain.order.dto;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatePrefixedSequenceIdGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        // 1. 오늘 날짜 "20260211" 로 바꾼다
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // DB에 미리 만들어둔 'order_number-seq' 시퀀스에서 값 가져오기 + DB가 알아서 Lock 걸어서 번호 중복 막음
        session.createNativeQuery("UPDATE order_number_seq SET id = LAST_INSERT_ID(id+1)").executeUpdate();
        Long nextVal = ((Number) session.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

        // 숫자 7자리로, 빈 자리는 0으로 채우는 걸로 설정
        return String.format("ORDER-%s-%07d",datePrefix,nextVal);
    }
}
