package com.bootcamp.paymentdemo.domain.member.dto;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveMemberResponse {

    private final Long memberId;
    private final String name;
    private final String email;
    private final String phone;

    public static SaveMemberResponse register(Member member) {
        return new SaveMemberResponse(
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                member.getPhoneNo()
        );
    }
}
