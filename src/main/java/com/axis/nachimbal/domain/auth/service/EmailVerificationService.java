package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.auth.enums.EmailPurpose;
import com.axis.nachimbal.global.exception.EmailSendFailedException;
import com.axis.nachimbal.global.exception.InvalidVerificationCodeException;
import com.axis.nachimbal.global.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String CODE_PREFIX = "email:code:";
    private static final String ATTEMPT_PREFIX = "email:attempts:";
    private static final String COOLDOWN_PREFIX = "email:cooldown:";
    private static final String VERIFIED_PREFIX = "email:verified:signup:"; // 회원가입 전용
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    // ── 회원가입용 (기존 호출부 유지) ──
    public void sendCode(String email) {
        sendCode(email, EmailPurpose.SIGNUP);
    }

    public void verifyCode(String email, String inputCode) {
        verifyCode(email, inputCode, EmailPurpose.SIGNUP);
    }

    // 인증번호 발송 (목적별)
    public void sendCode(String email, EmailPurpose purpose) {
        // 60초 재발송 제한 — setIfAbsent는 원자적이라 연타·동시 요청도 한 번만 통과
        String cooldownKey = cooldownKey(purpose, email);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", COOLDOWN_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            Long remaining = redisTemplate.getExpire(cooldownKey); // 초 단위
            long seconds = (remaining != null && remaining > 0) ? remaining : COOLDOWN_TTL.toSeconds();
            throw new TooManyRequestsException("인증번호는 " + seconds + "초 후에 다시 요청할 수 있습니다.");
        }

        String code = generateCode();
        redisTemplate.opsForValue().set(codeKey(purpose, email), code, CODE_TTL);
        redisTemplate.delete(attemptKey(purpose, email)); // 새 번호 발급 시 실패 횟수 초기화

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("나침발 <" + fromAddress + ">");
            message.setTo(email);
            message.setSubject("[나침발] 이메일 인증번호");
            message.setText("인증번호: " + code + " (5분 이내 입력해주세요)");
            mailSender.send(message);
        } catch (Exception e) {
            redisTemplate.delete(cooldownKey); // 메일 서버 문제는 사용자 잘못이 아니므로 제한 해제
            log.error("[Email] 발송 실패: purpose={} email={} error={}", purpose, email, e.getMessage());
            throw new EmailSendFailedException("인증 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    // 인증번호 확인 — 5회 실패 시 인증번호 폐기
    public void verifyCode(String email, String inputCode, EmailPurpose purpose) {
        String codeKey = codeKey(purpose, email);
        String attemptKey = attemptKey(purpose, email);
        String savedCode = redisTemplate.opsForValue().get(codeKey);

        if (savedCode == null) {
            throw new InvalidVerificationCodeException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        if (!savedCode.equals(inputCode)) {
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(attemptKey, CODE_TTL);
            }
            if (attempts != null && attempts >= MAX_ATTEMPTS) {
                redisTemplate.delete(codeKey);
                redisTemplate.delete(attemptKey);
                throw new InvalidVerificationCodeException("인증 시도 횟수를 초과했습니다. 인증번호를 다시 받아주세요.");
            }
            throw new InvalidVerificationCodeException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptKey);

        // 회원가입만 "인증 완료" 플래그를 남김 (가입 폼 작성 시간 확보용)
        if (purpose == EmailPurpose.SIGNUP) {
            redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "true", VERIFIED_TTL);
        }

    }

    // signUp 시점 최종 확인 (회원가입 전용)
    public boolean isVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(VERIFIED_PREFIX + email));
    }

    private String codeKey(EmailPurpose purpose, String email) {
        return CODE_PREFIX + purpose.name().toLowerCase() + ":" + email;
    }

    private String attemptKey(EmailPurpose purpose, String email) {
        return ATTEMPT_PREFIX + purpose.name().toLowerCase() + ":" + email;
    }

    private String cooldownKey(EmailPurpose purpose, String email) {
        return COOLDOWN_PREFIX + purpose.name().toLowerCase() + ":" + email;
    }

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}