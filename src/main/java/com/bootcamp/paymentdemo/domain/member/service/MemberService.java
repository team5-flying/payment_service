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
        Member member = userDetails.getMember();

        // member로 accessToken, refreshToken 생성
        String accessToken = jwtProvider.createAccessToken(member.getEmail(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());

        member.rotateRefreshToken(refreshToken);

        return LoginInfo.register(userDetails.getMember(), accessToken, refreshToken);
    }

    @Transactional
    public TokenPair refresh(String refreshTokenRaw) {
        String refreshToken = getRefreshTokenByRaw(refreshTokenRaw);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        if (!jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        String email = jwtProvider.getClaims(refreshToken).getSubject();

        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );

        // DB에 저장된 refreshToken과 비교
        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(refreshToken)) {
            // 탈취/로그아웃/회전된 토큰 등
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        // 토큰 회전(rotate): refresh도 새로 발급해서 탈취 대응
        String newAccessToken = jwtProvider.createAccessToken(member.getEmail(), member.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());
        member.rotateRefreshToken(newRefreshToken);

        return TokenPair.register(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String email) {
        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );
        member.clearRefreshToken();
    }

    @Transactional(readOnly = true)
    public SearchMemberResponse findOne(Member member) {
        return SearchMemberResponse.register(member);
    }

    private String getRefreshTokenByRaw(String refreshTokenRaw) {
        if (refreshTokenRaw != null && refreshTokenRaw.startsWith("Bearer ")) {
            return refreshTokenRaw.substring(7);
        }

        return null;
    }
}
