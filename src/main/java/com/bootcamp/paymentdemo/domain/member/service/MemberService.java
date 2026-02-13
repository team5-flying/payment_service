package com.bootcamp.paymentdemo.domain.member.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.common.security.JwtProvider;
import com.bootcamp.paymentdemo.domain.member.dto.*;
import com.bootcamp.paymentdemo.domain.member.entity.AccessTokenBlacklist;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.entity.MemberUserDetails;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

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

        Claims claims = jwtProvider.getClaims(refreshToken);
        member.rotateRefreshToken(refreshToken, claims.getId(), toLocalDateTime(claims.getExpiration()));

        return LoginInfo.register(userDetails.getMember(), accessToken, refreshToken);
    }

    @Transactional
    public TokenPair refresh(String refreshTokenRaw) {
        String refreshToken = getTokenByRaw(refreshTokenRaw);

        if (refreshToken == null || refreshToken.isBlank() || !jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        String email = claims.getSubject();

        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER)
        );

        // DB에 저장된 refreshToken과 비교
        boolean matched = refreshToken.equals(member.getRefreshToken())
                && claims.getId().equals(member.getRefreshJti())
                && member.getRefreshExpiresAt() != null
                && member.getRefreshExpiresAt().isAfter(LocalDateTime.now());

        if (!matched) {
            // 탈취/로그아웃/회전된 토큰 등 의심 -> 로그아웃
            member.clearRefreshToken();
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        // 토큰 회전(rotate): refresh도 새로 발급해서 탈취 대응
        String newAccessToken = jwtProvider.createAccessToken(member.getEmail(), member.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());
        Claims newClaims = jwtProvider.getClaims(newRefreshToken);

        member.rotateRefreshToken(newRefreshToken, newClaims.getId(), toLocalDateTime(newClaims.getExpiration()));

        return TokenPair.register(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String accessTokenRaw) {
        String accessToken = getTokenByRaw(accessTokenRaw);

        if (accessToken != null && jwtProvider.validateToken(accessToken) && !jwtProvider.isRefreshToken(accessToken)) {
            Claims claims = jwtProvider.getClaims(accessToken);

            // access 블랙리스트 등록(만료 전까지 무효화)
            accessTokenBlacklistService.blacklist(claims.getId(), toLocalDateTime(claims.getExpiration()));

            // refresh 폐기
            String email = claims.getSubject();
            memberRepository.findByEmailAndDeletedFalse(email).ifPresent(Member::clearRefreshToken);
        }
    }

    @Transactional(readOnly = true)
    public SearchMemberResponse findOne(Member member) {
        return SearchMemberResponse.register(member);
    }

    private String getTokenByRaw(String refreshTokenRaw) {
        if (refreshTokenRaw != null && refreshTokenRaw.startsWith("Bearer ")) {
            return refreshTokenRaw.substring(7);
        }

        return null;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
