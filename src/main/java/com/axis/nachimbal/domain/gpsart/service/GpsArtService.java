package com.axis.nachimbal.domain.gpsart.service;

import com.axis.nachimbal.domain.gpsart.dto.GpsArtRequest;
import com.axis.nachimbal.domain.gpsart.dto.GpsArtResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsArtService {

    // application.properties: ai.gps-art.base-url=http://<AI서버IP>:8000
    @Value("${ai.gps-art.base-url}")
    private String AI_SERVER_BASE_URL;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // AI 서버로 보낼 요청 바디 (필드명이 Python Pydantic 모델과 정확히 일치해야 함)
    private record AiServerRequestBody(Double latitude, Double longitude, Integer searchRadius) {}

    // GPS 아트 경로 생성 요청 -> Python AI 서버(gps_art_server.py) 호출
    public GpsArtResponse generate(GpsArtRequest request) {
        try {
            int radius = request.getRadius() != null ? request.getRadius() : 1200;

            String requestBody = objectMapper.writeValueAsString(
                    new AiServerRequestBody(request.getLatitude(), request.getLongitude(), radius)
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(AI_SERVER_BASE_URL + "/gps-art/generate"))
                    .header("Content-Type", "application/json")
                    // AI 서버 처리(그래프 계산) 넉넉히 타임아웃 설정
                    .timeout(Duration.ofSeconds(400))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                log.error("GPS 아트 AI 서버 호출 실패: HTTP {}", response.statusCode());
                return GpsArtResponse.builder().found(false).build();
            }

            return parseAiServerResponse(response.body());

        } catch (Exception e) {
            log.error("GPS 아트 생성 중 오류", e);
            return GpsArtResponse.builder().found(false).build();
        }
    }

    // AI 서버 응답(JSON) -> GpsArtResponse 변환
    private GpsArtResponse parseAiServerResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        boolean found = root.path("found").asBoolean(false);
        if (!found) {
            return GpsArtResponse.builder().found(false).build();
        }

        List<GpsArtResponse.Coordinate> fullPath = new ArrayList<>();
        for (JsonNode node : root.path("fullPath")) {
            fullPath.add(GpsArtResponse.Coordinate.builder()
                    .x(node.path("x").asDouble())
                    .y(node.path("y").asDouble())
                    .build());
        }

        List<GpsArtResponse.WaypointDto> waypoints = new ArrayList<>();
        for (JsonNode node : root.path("waypoints")) {
            waypoints.add(GpsArtResponse.WaypointDto.builder()
                    .latitude(node.path("latitude").asDouble())
                    .longitude(node.path("longitude").asDouble())
                    .build());
        }

        return GpsArtResponse.builder()
                .found(true)
                .fullPath(fullPath)
                .waypoints(waypoints)
                .totalDistanceMeters(root.path("totalDistanceMeters").asDouble())
                .distScore(root.path("distScore").asDouble())
                .build();
    }
}