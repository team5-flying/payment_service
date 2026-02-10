package com.bootcamp.paymentdemo.domain.member.dto;

import com.bootcamp.paymentdemo.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginInfo {

    private final Boolean success;
    private final Long id;
    private final String email;
    private final String token;

    public static LoginInfo register(Member member, String token) {
        return new LoginInfo(true, member.getMemberId(), member.getEmail(), token);
    }
}
