package com.axis.nachimbal.domain.session.repository;

import com.axis.nachimbal.domain.session.entity.ExerciseSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {
    List<ExerciseSession> findAllByUserId(Long userId);
}