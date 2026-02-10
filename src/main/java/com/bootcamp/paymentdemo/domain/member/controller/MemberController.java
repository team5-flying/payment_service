package com.bootcamp.paymentdemo.domain.member.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.member.dto.*;
import com.bootcamp.paymentdemo.domain.member.entity.MemberUserDetails;
import com.bootcamp.paymentdemo.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SaveMemberResponse>> signup(@RequestBody SaveMemberRequest request) {
        SaveMemberResponse response = memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success("201", "회원가입 성공", response));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginMemberResponse> login(@RequestBody LoginMemberRequest request) {
        LoginInfo info = memberService.login(request);

        // 로그인 info에서 토큰을 헤더에 담아 응답
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + info.getToken());

        // 로그인 info에서 로그인 정보를 토대로 response 객체 생성 후 응답
        LoginMemberResponse response = LoginMemberResponse.register(info);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<SearchMemberResponse> getUsers(@AuthenticationPrincipal MemberUserDetails userDetails) {
        SearchMemberResponse response = memberService.findOne(userDetails.getMember());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
