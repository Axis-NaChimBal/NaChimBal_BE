package com.axis.nachimbal.domain.user.service;

import com.axis.nachimbal.domain.user.dto.*;
import com.axis.nachimbal.domain.user.entity.User;
import com.axis.nachimbal.domain.user.repository.UserRepository;
import com.axis.nachimbal.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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

    // 나이 입력
    public Integer updateAge(Long userId, UpdateAgeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        user.updateAge(req.getAge());

        return req.getAge();
    }

    // 페이스 조절
    public Boolean updatePaceControl(Long userId, UpdatePaceControlRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        user.updatePaceControl(req.getPaceControlEnabled());

        return req.getPaceControlEnabled();
    }

    // 운동목표
    public String updateExerciseGoal(Long userId, UpdateExerciseGoalRequest req) {
        User user = userRepository.findById(userId)
               .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));

        user.updateExerciseGoal(req.getExerciseGoal());

        return req.getExerciseGoal().name();
    }

    // 보폭 업데이트
    @Transactional
    public void updateStride(UpdateStrideRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다. userId=" + request.getUserId()));
        user.updateStride(request.getStrideLength());
        log.info("[Stride] 보폭 저장: userId={} strideLength={}m",
                user.getId(), request.getStrideLength());
    }
}