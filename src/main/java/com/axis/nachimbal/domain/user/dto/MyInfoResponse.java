package com.axis.nachimbal.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyInfoResponse {
    private String loginId;
    private String email;
    private boolean socialLogin;
}