package com.axis.nachimbal.domain.running.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// [ 세션 종료 요청 DTO ]
@Getter
@NoArgsConstructor
public class SessionEndRequest {

    // 종료할 세션 ID
    private Long sessionId;

    // 총 경과 시간 (초)
    private Integer totalElapsed;
}
