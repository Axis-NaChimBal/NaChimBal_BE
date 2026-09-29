package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.global.exception.InvalidCredentialsException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class GoogleOAuthService implements SocialOAuthService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidCredentialsException("유효하지 않은 구글 토큰입니다.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String providerId = payload.getSubject(); // 구글 고유 사용자 ID

            return new SocialUserInfo(email, providerId);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[GoogleOAuth] 토큰 검증 실패: {}", e.getMessage());
            throw new InvalidCredentialsException("구글 토큰 검증에 실패했습니다.");
        }
    }
}