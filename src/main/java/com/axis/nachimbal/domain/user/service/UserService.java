package com.axis.nachimbal.domain.user.service;

import com.axis.nachimbal.domain.user.dto.UpdateRhrRequest;
import com.axis.nachimbal.domain.user.dto.UpdateRhrResponse;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // [ 안정 심박수 업데이트] : 온보딩 최초 저장 + 마이페이지 재측정 동일 API 사용
    @Transactional
    public UpdateRhrResponse updateRhr(UpdateRhrRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다. userId=" + request.getUserId()));

        user.updateRhr(request.getRestingHr());

        log.info("[RHR] 안정 심박수 저장: userId={} restingHr={}",
                user.getId(), request.getRestingHr());

        return new UpdateRhrResponse(user.getId(), user.getRestingHr());
    }
}