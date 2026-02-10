package com.bootcamp.paymentdemo.domain.member.dto;

import lombok.Getter;

@Getter
public class LoginMemberRequest {

    // lombok valid 추후 추가
    private String email;
    private String password;
}
