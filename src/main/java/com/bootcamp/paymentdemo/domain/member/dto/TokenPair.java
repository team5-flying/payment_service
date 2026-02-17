package com.bootcamp.paymentdemo.domain.member.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenPair {

    private final String accessToken;
    private final String refreshToken;

    public static TokenPair register(String accessToken, String refreshToken) {
        return new TokenPair(accessToken, refreshToken);
    }
}
