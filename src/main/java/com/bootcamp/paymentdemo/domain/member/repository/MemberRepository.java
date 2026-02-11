package com.bootcamp.paymentdemo.domain.member.repository;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmailAndDeletedFalse(String email);
    Optional<Member> findByEmailAndDeletedFalse(String email);
}
