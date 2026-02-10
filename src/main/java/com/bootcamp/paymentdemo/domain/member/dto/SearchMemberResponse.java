package com.bootcamp.paymentdemo.domain.member.dto;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchMemberResponse {

    private final String customerUid;
    private final String email;
    private final String name;
    private final String phone;
    private final Integer pointBalance;

    public static SearchMemberResponse register(Member member) {
        return new SearchMemberResponse(
                member.getMemberUid(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNo(),
                member.getPoint()
        );
    }
}
