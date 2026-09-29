package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.auth.dto.FindIdResponse;
import com.axis.nachimbal.domain.auth.dto.PasswordResetVerifyResponse;
import com.axis.nachimbal.domain.auth.enums.EmailPurpose;
import com.axis.nachimbal.domain.security.jwt.RefreshTokenRepository;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountRecoveryService {

    private static final String RESET_TOKEN_PREFIX = "pw-reset:token:";
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    // ── 아이디 찾기 ──
    @Transactional(readOnly = true)
    public void sendFindIdCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new EmailNotRegisteredException("가입된 이메일이 아닙니다.");
        }
        emailVerificationService.sendCode(email, EmailPurpose.FIND_ID);
    }

    @Transactional(readOnly = true)
    public FindIdResponse verifyFindId(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotRegisteredException("가입된 이메일이 아닙니다."));

        emailVerificationService.verifyCode(email, code, EmailPurpose.FIND_ID);

        AuthProvider provider = user.getProvider();
        // 소셜 계정은 loginId가 자동 생성값이라 알려주지 않고 가입 경로만 안내
        String loginId = provider == AuthProvider.LOCAL ? user.getLoginId() : null;
        return new FindIdResponse(loginId, provider.name(), displayName(provider));
    }

    // ── 비밀번호 재설정 ──
    @Transactional(readOnly = true)
    public void sendPasswordResetCode(String loginId, String email) {
        findResettableAccount(loginId, email);
        emailVerificationService.sendCode(email, EmailPurpose.PASSWORD_RESET);
    }

    @Transactional(readOnly = true)
    public PasswordResetVerifyResponse verifyPasswordReset(String loginId, String email, String code) {
        // 인증번호를 소모하기 전에 계정 일치부터 확인
        User user = findResettableAccount(loginId, email);
        emailVerificationService.verifyCode(email, code, EmailPurpose.PASSWORD_RESET);

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + resetToken, String.valueOf(user.getId()), RESET_TOKEN_TTL);

        return new PasswordResetVerifyResponse(resetToken);
    }

    public void confirmPasswordReset(String resetToken, String newPassword) {
        String tokenKey = RESET_TOKEN_PREFIX + resetToken;
        String userIdValue = redisTemplate.opsForValue().get(tokenKey);
        if (userIdValue == null) {
            throw new InvalidResetTokenException("인증 시간이 만료되었습니다. 처음부터 다시 진행해주세요.");
        }

        Long userId = Long.valueOf(userIdValue);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidResetTokenException("인증 시간이 만료되었습니다. 처음부터 다시 진행해주세요."));

        // 동일 비밀번호는 토큰을 유지한 채 재입력 기회 제공
        if (user.getPasswordHash() != null && passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new SamePasswordException("이전과 다른 비밀번호를 입력해주세요.");
        }

        user.updatePasswordHash(passwordEncoder.encode(newPassword));
        redisTemplate.delete(tokenKey);            // 일회용 토큰 폐기
        refreshTokenRepository.deleteById(userId); // 모든 기기의 재발급 차단

        log.info("[Auth] 비밀번호 재설정 완료: userId={}", userId);
    }

    // 이메일로 계정을 찾고, 소셜 계정 차단 + 아이디 일치 확인
    private User findResettableAccount(String loginId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("아이디와 이메일이 일치하는 계정을 찾을 수 없습니다."));

        if (user.getProvider() != AuthProvider.LOCAL) {
            String name = displayName(user.getProvider());
            throw new SocialAccountException(name + " 계정으로 가입된 이메일입니다. " + name + " 로그인을 이용해주세요.");
        }
        if (!user.getLoginId().equals(loginId)) {
            throw new AccountNotFoundException("아이디와 이메일이 일치하는 계정을 찾을 수 없습니다.");
        }
        return user;
    }

    private String displayName(AuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "Google";
            case KAKAO -> "Kakao";
            case NAVER -> "Naver";
            default -> provider.name();
        };
    }
}