package com.axis.nachimbal.domain.result.repository;

import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Long> {
    List<ExerciseResult> findAllBySession_UserId(Long userId);
}