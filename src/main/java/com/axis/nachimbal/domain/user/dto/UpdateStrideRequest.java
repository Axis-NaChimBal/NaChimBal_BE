package com.axis.nachimbal.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 보폭 업데이트 요청 DTO
@Getter
@NoArgsConstructor
public class UpdateStrideRequest {

    // 세션 중 실측된 보폭 (m) - 중앙값 기준
    private Double strideLength;
}