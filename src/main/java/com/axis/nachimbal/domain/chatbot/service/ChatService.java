package com.axis.nachimbal.domain.chatbot.service;

import com.axis.nachimbal.domain.chatbot.client.OpenAiClient;
import com.axis.nachimbal.domain.chatbot.dto.ChatRequestDto;
import com.axis.nachimbal.domain.chatbot.dto.ChatResponseDto;
import com.axis.nachimbal.domain.chatbot.dto.ExerciseSummaryDto;
import com.axis.nachimbal.domain.result.entity.ExerciseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ExerciseSummaryService summaryService;
    private final OpenAiClient openAiClient;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            너는 나침발 앱의 AI 러닝 코치야. 사용자의 운동 데이터를 참고해서
            친근하고 격려하는 톤으로 답변해줘. 사용자 개인 기록에 관한 숫자는
            이미 계산되어 주어지니, 그 값을 벗어나서 사용자의 실제 기록인 것처럼
            새로운 개인 수치를 지어내지 마.

            데이터는 최근 7일 요약, 최근 한 달 요약, 가장 최근 러닝 1건으로 주어져.
            질문에 맞는 기간을 골라서 써. "지난번이랑 비교" 같은 질문에는
            가장 최근 러닝을 최근 7일·한 달 평균과 비교해서 답해.
            심박수가 "측정 기록 없음"이면 밴드 없이 달린 것이니 심박에 대한
            판단은 하지 마.

            단, 사용자 러닝 기록이 없거나 부족해서 훈련 계획을 구체적으로
            짜야 할 때는 예외야. 이 경우엔 "가볍게", "편안하게" 같은 추상적인
            표현만 쓰지 말고, 일반적인 초보자 기준의 시간(분)이나 거리(km) 같은
            구체적인 가이드를 제시해. 예: "10~15분 정도 걷기부터 시작해보세요"
            처럼. 이건 사용자의 실제 기록이 아니라 일반적인 초보자 권장 기준이라는
            걸 자연스럽게 알 수 있게 표현하면 돼.

            중요: 답변은 앱의 일반 텍스트 채팅창에 그대로 표시돼. 마크다운 문법
            (**굵게**, - 목록, # 제목, `코드` 등)을 절대 쓰지 말고 순수 텍스트로만
            답변해. 강조하고 싶으면 이모지나 문장 구조로 표현해.

            %s
            """;

    public ChatResponseDto chat(Long userId, ChatRequestDto request) {

        // 요약 통계 조회 + 조합
        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(buildExerciseContext(userId));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", systemPrompt));

        // 이전 대화 히스토리 (프론트에서 최근 10턴 잘라서 보낸 것 그대로 사용)
        if (request.previousMessages() != null) {
            for (ChatRequestDto.ChatMessageDto m : request.previousMessages()) {
                messages.add(buildMessage(m.role(), m.content()));
            }
        }

        // 이번 질문
        messages.add(buildMessage("user", request.question()));

        // LLM 호출
        String answer = openAiClient.getChatCompletion(messages);

        // 지금은 텍스트 그대로 반환 (구조화는 필요해지면 여기서 확장)
        return new ChatResponseDto(answer);
    }

    // 최근 7일·한 달 요약 + 직전 러닝 1건을 프롬프트용 텍스트로 조합
    private String buildExerciseContext(Long userId) {
        Optional<ExerciseResult> latest = summaryService.getLatestSession(userId);

        // 기록이 아예 없는 신규 사용자 → GPT가 초보자 기준으로 안내하도록 명시
        if (latest.isEmpty()) {
            return "[운동 기록]\n아직 러닝 기록이 없는 신규 사용자야. 일반적인 초보자 기준으로 안내해줘.";
        }

        ExerciseSummaryDto weekly  = summaryService.getWeeklySummary(userId);
        ExerciseSummaryDto monthly = summaryService.getMonthlySummary(userId);

        return """
                [최근 7일 요약]
                %s

                [최근 한 달 요약]
                %s

                [가장 최근 러닝]
                %s""".formatted(
                weekly.toPromptContext(),
                monthly.toPromptContext(),
                formatLatest(latest.get())
        );
    }

    // 직전 러닝 1건 포맷 (바이탈 기능 추가 전 기록은 null일 수 있어 null 처리)
    private String formatLatest(ExerciseResult r) {
        Integer hr = r.getAvgHeartRate();
        return """
                날짜: %s
                거리: %.2fkm
                시간: %d분 %d초
                평균 속도: %.1fkm/h
                평균 심박수: %s
                소모 칼로리: %skcal
                페이스 조절 횟수: %s회""".formatted(
                r.getSession().getStartedAt().toLocalDate(),
                r.getDistanceKm(),
                r.getDurationSec() / 60, r.getDurationSec() % 60,
                r.getAvgSpeedKmh(),
                (hr != null && hr > 0) ? hr + " bpm" : "측정 기록 없음",   // 0 = 밴드 미연결
                r.getCaloriesKcal() != null ? Math.round(r.getCaloriesKcal()) : "-",
                r.getPaceAdjustCount() != null ? r.getPaceAdjustCount() : 0
        );
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }
}