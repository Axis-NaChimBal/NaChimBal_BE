package com.axis.nachimbal.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// [ 안정 심박수 업데이트 요청 DTO ]
@Getter
@NoArgsConstructor
public class UpdateRhrRequest {

    // 임시: 인증 미구현 단계 - FE에서 전달하는 사용자 ID
    // 추후 JWT 인증 구현 시 제거 예정
    private Long userId;

    // 측정된 안정 심박수 (BPM)
    private Integer restingHr;
}
