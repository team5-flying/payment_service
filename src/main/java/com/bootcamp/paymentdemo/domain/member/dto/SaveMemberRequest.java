package com.bootcamp.paymentdemo.domain.member.dto;

import lombok.Getter;

@Getter
public class SaveMemberRequest {
    // lombok valid 사용
    private String name;
    private String email;
    private String password;
    private String phone;
}
