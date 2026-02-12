package com.bootcamp.paymentdemo.domain.member.repository;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<Member> findByEmailAndDeletedFalse(String email);

    // 연속 결제에 의한 포인트 데이터 처리 오류 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId AND m.deleted = false")
    Optional<Member> findByIdWithLock(@Param("memberId") Long memberId);
}
