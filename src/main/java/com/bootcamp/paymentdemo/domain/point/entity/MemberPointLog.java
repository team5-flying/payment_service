package com.bootcamp.paymentdemo.domain.point.entity;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member_point_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointLogId;

    @Column(nullable = false, length = 100)
    private String orderNo;

    @Column(nullable = false)
    private Long point;

    @Column(nullable = false)
    private LocalDateTime saveAt;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MemberPointLogStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static MemberPointLog register(String OrderNo, Long point, MemberPointLogStatus status, Member member) {
        MemberPointLog log = new MemberPointLog();
        log.orderNo = OrderNo;
        log.point = point;
        log.status = status;
        log.member = member;
        log.saveAt = LocalDateTime.now();
        log.expireAt = LocalDateTime.now().plusYears(1);
        return log;
    }
}
