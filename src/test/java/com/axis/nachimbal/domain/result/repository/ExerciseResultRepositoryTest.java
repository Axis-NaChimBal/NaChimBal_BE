package com.axis.nachimbal.domain.result.repository;

import com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import com.axis.nachimbal.domain.running.entity.ExerciseSession;
import com.axis.nachimbal.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExerciseResultRepositoryTest {

    @Autowired
    private ExerciseResultRepository resultRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void 주간_요약_통계가_정상적으로_계산된다() {
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("dummy-hash")
                .age(25)
                .restingHr(60)
                .build();
        em.persist(user);

        ExerciseSession session = ExerciseSession.builder()
                .userId(user.getId())
                .routeDistanceKm(5.0)
                .totalTimeSec(1800)
                .build();
        session.end();
        em.persist(session);

        ExerciseResult result = ExerciseResult.create(session, 5.0, 1800, 6.0);
        em.persist(result);

        em.flush();

        ExerciseSummaryDto summary = resultRepository.findSummary(
                user.getId(),
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().plusMinutes(1)
        );

        assertThat(summary.sessionCount()).isEqualTo(1);
        assertThat(summary.avgDistanceKm()).isEqualTo(5.0);
    }

    @Test
    void 세션이_없으면_0으로_채워진_요약을_반환한다() {
        Long userId = 999L;

        ExerciseSummaryDto summary = resultRepository.findSummary(
                userId,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        );

        assertThat(summary.sessionCount()).isEqualTo(0);
        assertThat(summary.avgDistanceKm()).isEqualTo(0.0);
    }
}