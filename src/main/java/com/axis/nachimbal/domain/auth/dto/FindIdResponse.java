package com.axis.nachimbal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindIdResponse {
    private String loginId;      // 소셜 계정이면 null
    private String provider;     // LOCAL / GOOGLE / KAKAO / NAVER
    private String providerName; // 화면 표시용: Google / Kakao / Naver
}