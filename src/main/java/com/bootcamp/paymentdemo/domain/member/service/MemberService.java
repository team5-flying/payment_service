package com.bootcamp.paymentdemo.domain.member.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.common.security.JwtProvider;
import com.bootcamp.paymentdemo.domain.member.dto.*;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.entity.MemberUserDetails;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public SaveMemberResponse signup(SaveMemberRequest request) {
        if (memberRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_EMAIL);
        }

        Member member = Member.register(request, passwordEncoder.encode(request.getPassword()));
        memberRepository.save(member);
        return SaveMemberResponse.register(member);
    }

    @Transactional
    public LoginInfo login(LoginMemberRequest request) {
        // authentication 객체 생성, 인증 로직 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // authentication 객체에서 MemberUserDetails 추출
        MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();

        // UserDetails로 토큰 생성
        String token = jwtProvider.createToken(userDetails.getUsername(), userDetails.getMember().getRole());

        return LoginInfo.register(userDetails.getMember(), token);
    }

    @Transactional(readOnly = true)
    public SearchMemberResponse findOne(Member member) {
        return SearchMemberResponse.register(member);
    }
}
