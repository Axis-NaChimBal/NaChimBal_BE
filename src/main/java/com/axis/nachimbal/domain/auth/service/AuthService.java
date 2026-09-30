package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.auth.dto.*;
import com.axis.nachimbal.domain.security.jwt.JwtTokenProvider;
import com.axis.nachimbal.domain.security.jwt.RefreshToken;
import com.axis.nachimbal.domain.security.jwt.RefreshTokenRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.DuplicateEmailException;
import com.axis.nachimbal.global.exception.DuplicateLoginIdException;
import com.axis.nachimbal.global.exception.EmailNotVerifiedException;
import com.axis.nachimbal.global.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final SocialOAuthServiceFactory socialOAuthServiceFactory;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // ms 단위

    // 회원가입
    public SignUpResponse signUp(SignUpRequest request) {

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new DuplicateLoginIdException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
        if (!emailVerificationService.isVerified(request.getEmail())) {
            throw new EmailNotVerifiedException("이메일 인증이 완료되지 않았습니다.");
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .build();
        user.verifyEmail();

        userRepository.save(user);

        return new SignUpResponse(user.getId(), user.getLoginId());
    }

    // 로그인
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new InvalidCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(user.getId(), refreshTokenValue, refreshTokenExpiration / 1000)
        );

        return new TokenResponse(accessToken, refreshTokenValue);
    }

    // 토큰 재발급
    @Transactional(readOnly = true)
    public TokenResponse reissue(ReissueRequest request) {

        String refreshTokenValue = request.getRefreshToken();

        if (!jwtTokenProvider.validateRefreshToken(refreshTokenValue)) {
            throw new InvalidCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshTokenValue);

        RefreshToken saved = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("재로그인이 필요합니다."));

        if (!saved.getToken().equals(refreshTokenValue)) {
            throw new InvalidCredentialsException("리프레시 토큰이 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenRepository.save(
                new RefreshToken(userId, newRefreshTokenValue, refreshTokenExpiration / 1000)
        );

        return new TokenResponse(newAccessToken, newRefreshTokenValue);
    }

    // 소셜 로그인 (신규면 자동 가입, 기존이면 로그인)
    public TokenResponse socialLogin(AuthProvider provider, String token) {

        SocialOAuthService oAuthService = socialOAuthServiceFactory.getService(provider);
        SocialUserInfo userInfo = oAuthService.getUserInfo(token);

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.providerId())
                .orElseGet(() -> registerSocialUser(provider, userInfo));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(user.getId(), refreshTokenValue, refreshTokenExpiration / 1000)
        );

        return new TokenResponse(accessToken, refreshTokenValue);
    }

    // 소셜 신규 유저 자동 가입
    private User registerSocialUser(AuthProvider provider, SocialUserInfo userInfo) {

        if (userRepository.existsByEmail(userInfo.email())) {     // ← 이 3줄만 새로 추가
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다. 다른 방법으로 로그인해주세요.");
        }

        User newUser = User.builder()
                .loginId(generateSocialLoginId(provider))
                .email(userInfo.email())
                .passwordHash(null)
                .provider(provider)
                .providerId(userInfo.providerId())
                .build();
        newUser.verifyEmail();

        User saved = userRepository.save(newUser);

        return saved;
    }

    // 로그아웃
    public void logout(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    // 탈퇴
    public void withdraw(Long userId) {
        refreshTokenRepository.deleteById(userId);
        userRepository.deleteById(userId);
    }

    // 소셜 유저용 loginId 자동 생성 (provider + 랜덤값, 예: google_a1b2c3d4)
    private String generateSocialLoginId(AuthProvider provider) {
        return provider.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}