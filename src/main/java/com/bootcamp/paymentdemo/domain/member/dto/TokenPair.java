package com.bootcamp.paymentdemo.domain.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenPair {

    private final String accessToken;
    private final String refreshToken;

    public static TokenPair register(String accessToken, String refreshToken) {
        return new TokenPair(accessToken, refreshToken);
    }
}
