package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.user.enums.AuthProvider;

public interface SocialOAuthService {

    AuthProvider getProvider();

    SocialUserInfo getUserInfo(String token);
}