package com.axis.nachimbal.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// [ 안정 심박수 업데이트 응답 DTO ]
@Getter
@AllArgsConstructor
public class UpdateRhrResponse {

    private Long userId;

    // 저장된 안정 심박수 (BPM) - FE 완료 화면 표시용
    private Integer restingHr;
}
