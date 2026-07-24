package com.axis.nachimbal.domain.running.repository;

import com.axis.nachimbal.domain.running.entity.ExerciseSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {
}
