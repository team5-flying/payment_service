package com.bootcamp.paymentdemo.domain.member.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.member.dto.SaveMemberRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String memberUid;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String phoneNo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column(nullable = false)
    private LocalDateTime gradeAt;

    @Column(nullable = false)
    private Long point;

    @Column(nullable = false)
    private Long totalPriceAmount;

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletedAt;

    private String refreshToken;

    public static Member register(SaveMemberRequest request, String encodedPassword) {
        Member member = new Member();
        member.name = request.getName();
        member.email = request.getEmail();
        member.password = encodedPassword;
        member.phoneNo = request.getPhone();
        member.memberUid = "CUST-" + UUID.randomUUID();
        member.role = MemberRole.ROLE_USER;
        member.grade = Grade.BRONZE;
        member.gradeAt = LocalDateTime.now();
        member.point = 0L;
        member.totalPriceAmount = 0L;
        member.deleted = false;
        return member;
    }

    public void addPoint(Long amount) {
        this.point += amount;
    }

    public void minusPoint(Long amount) {
        this.point -= amount;
    }

    public void addTotalPriceAmount(Long amount) {
        if (amount == null || amount < 0) return;

        if (this.totalPriceAmount == null) {
            this.totalPriceAmount = 0L;
        }
        this.totalPriceAmount += amount;
    }

    public void subtractTotalPriceAmount(Long amount) {
        if (amount == null || amount < 0) return;

        if (this.totalPriceAmount == null) {
            this.totalPriceAmount = 0L;
        }

        // 누적 금액이 차감액보다 적으면 0으로 초기화 (음수 방지)
        if (this.totalPriceAmount >= amount) {
            this.totalPriceAmount -= amount;
        } else {
            this.totalPriceAmount = 0L;
        }
    }

    public void updateGrade(Grade grade) {
        this.grade = grade;
        this.gradeAt = LocalDateTime.now();
    }

    public void rotateRefreshToken(String refreshTokenHash) {
        this.refreshToken = refreshTokenHash;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }
}
