package com.axis.nachimbal.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "토큰이 필요합니다.")
    private String token; // 구글은 idToken, 카카오/네이버는 accessToken
}