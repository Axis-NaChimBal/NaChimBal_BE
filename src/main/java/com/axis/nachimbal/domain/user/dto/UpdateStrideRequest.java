package com.axis.nachimbal.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//보폭 업데이트 요청 DTO
@Getter
@NoArgsConstructor
public class UpdateStrideRequest {

    // 임시: 추후 JWT 인증 구현 시 제거 예정
    private Long userId;

    // 세션 중 실측된 보폭 (m) - 중앙값 기준
    private Double strideLength;
}
