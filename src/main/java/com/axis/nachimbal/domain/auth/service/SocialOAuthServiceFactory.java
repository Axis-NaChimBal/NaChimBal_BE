package com.axis.nachimbal.domain.auth.service;

import com.axis.nachimbal.domain.user.enums.AuthProvider;
import com.axis.nachimbal.global.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SocialOAuthServiceFactory {

    private final List<SocialOAuthService> services;

    private Map<AuthProvider, SocialOAuthService> serviceMap;

    private Map<AuthProvider, SocialOAuthService> getServiceMap() {
        if (serviceMap == null) {
            serviceMap = services.stream()
                    .collect(Collectors.toMap(SocialOAuthService::getProvider, s -> s));
        }
        return serviceMap;
    }

    public SocialOAuthService getService(AuthProvider provider) {
        SocialOAuthService service = getServiceMap().get(provider);
        if (service == null) {
            throw new InvalidCredentialsException("지원하지 않는 소셜 로그인입니다: " + provider);
        }
        return service;
    }
}