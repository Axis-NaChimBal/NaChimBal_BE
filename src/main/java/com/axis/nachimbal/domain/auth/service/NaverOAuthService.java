package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.global.exception.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class NaverOAuthService implements SocialOAuthService {

    private final RestClient restClient = RestClient.create("https://openapi.naver.com");

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/v1/nid/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> response = (Map<String, Object>) body.get("response");
            if (response == null) {
                throw new InvalidCredentialsException("네이버 사용자 정보를 가져올 수 없습니다.");
            }

            String providerId = (String) response.get("id");
            String email = (String) response.get("email");

            if (email == null) {
                throw new InvalidCredentialsException("네이버 계정에서 이메일 제공에 동의하지 않았습니다.");
            }

            return new SocialUserInfo(email, providerId);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[NaverOAuth] 토큰 검증 실패: {}", e.getMessage());
            throw new InvalidCredentialsException("네이버 토큰 검증에 실패했습니다.");
        }
    }
}