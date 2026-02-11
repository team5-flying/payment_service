package com.bootcamp.paymentdemo.domain.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderNumberSequence {

    @Id
    private String sequenceDate;
    private Long lastNumber;

    public void increment() {
        this.lastNumber++;
    }

    public OrderNumberSequence(String sequenceDate) {
        this.sequenceDate = sequenceDate;
        this.lastNumber = 1L;
    }
}
