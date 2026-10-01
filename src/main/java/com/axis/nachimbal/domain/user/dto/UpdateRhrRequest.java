package com.axis.nachimbal.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// [ 안정 심박수 업데이트 요청 DTO ]
@Getter
@NoArgsConstructor
public class UpdateRhrRequest {

    // 측정된 안정 심박수 (BPM)
    private Integer restingHr;
}