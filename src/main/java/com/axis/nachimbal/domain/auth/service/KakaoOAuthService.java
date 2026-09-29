package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.global.exception.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class KakaoOAuthService implements SocialOAuthService {

    private final RestClient restClient = RestClient.create("https://kapi.kakao.com");

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            String providerId = String.valueOf(response.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            if (email == null) {
                throw new InvalidCredentialsException("카카오 계정에서 이메일 제공에 동의하지 않았습니다.");
            }

            return new SocialUserInfo(email, providerId);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[KakaoOAuth] 토큰 검증 실패: {}", e.getMessage());
            throw new InvalidCredentialsException("카카오 토큰 검증에 실패했습니다.");
        }
    }
}