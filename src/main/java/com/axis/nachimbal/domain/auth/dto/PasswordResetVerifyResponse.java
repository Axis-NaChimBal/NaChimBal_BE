package com.axis.nachimbal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetVerifyResponse {
    private String resetToken;
}