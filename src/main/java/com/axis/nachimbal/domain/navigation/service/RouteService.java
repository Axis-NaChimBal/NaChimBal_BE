package com.axis.nachimbal.domain.navigation.service;

import com.axis.nachimbal.domain.navigation.dto.RouteRequest;
import com.axis.nachimbal.domain.navigation.dto.RouteResponse;
import com.axis.nachimbal.domain.navigation.dto.TmapPedestrianRequest;
import com.axis.nachimbal.domain.navigation.enums.HapticType;
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
public class RouteService {

    @Value("${api.key}")
    private String APIKEY;

    @Value("${api.base-url}")
    private String BASEURL;

    private static final double NOTICE_DISTANCE = 5.0;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // TMAP 정보 조회
    public RouteResponse getPedestrianRoute(RouteRequest request) {
        try {
            TmapPedestrianRequest tmapRequest = TmapPedestrianRequest.builder()
                    .startX(String.valueOf(request.getStartX()))
                    .startY(String.valueOf(request.getStartY()))
                    .endX(String.valueOf(request.getEndX()))
                    .endY(String.valueOf(request.getEndY()))
                    .startName(request.getStartName() != null ? request.getStartName() : "출발지")
                    .endName(request.getEndName() != null ? request.getEndName() : "도착지")
                    .searchOption("0")
                    .reqCoordType("WGS84GEO")
                    .resCoordType("WGS84GEO")
                    .build();

            String requestBody = objectMapper.writeValueAsString(tmapRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASEURL + "/routes/pedestrian"))
                    .header("Content-Type", "application/json")
                    .header("appKey", APIKEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("TMAP 보행자 API 호출 실패: HTTP " + response.statusCode());
            }

            return parseRouteResponse(response.body());

        } catch (Exception e) {
            log.error("TMAP 보행자 경로 탐색 중 오류", e);
            throw new RuntimeException("보행자 경로 탐색 실패: " + e.getMessage(), e);
        }
    }

    // 응답 형식 파싱
    private RouteResponse parseRouteResponse(String body) throws Exception {
        // features 배열 순회
        JsonNode features = objectMapper.readTree(body).path("features");

        int totalDistance = 0, totalTime = 0;
        double prevX = 0, prevY = 0;
        List<RouteResponse.StepInfo> steps = new ArrayList<>();
        List<JsonNode> featureList = new ArrayList<>();
        features.forEach(featureList::add);

        for (int i = 0; i < featureList.size(); i++) {
            JsonNode feature  = featureList.get(i);
            JsonNode props    = feature.path("properties");
            JsonNode geometry = feature.path("geometry");

            // 방향 안내(Point)를 기준으로 정보 가져옴
            if ("Point".equals(geometry.path("type").asText())) {
                // 전체 요약 정보 저장
                if (props.has("totalDistance")) {
                    totalDistance = props.path("totalDistance").asInt();
                    totalTime     = props.path("totalTime").asInt();
                }

                // 안내 정보
                if (props.has("description")) {
                    JsonNode coords = geometry.path("coordinates");
                    if (coords.isArray() && coords.size() >= 2) {

                        // 다음 방향 전환(Point) 전까지의 거리 합 -> LineString의 거리를 모두 더함
                        int dist = 0;
                        double lastLineX = 0, lastLineY = 0;

                        for (int j = i + 1; j < featureList.size(); j++) {
                            JsonNode next         = featureList.get(j);
                            JsonNode nextGeometry = next.path("geometry");
                            String   nextType     = nextGeometry.path("type").asText();

                            if ("LineString".equals(nextType)) {
                                dist += next.path("properties").path("distance").asInt();

                                // 마지막 LineString의 끝 좌표 저장
                                JsonNode lineCoords = nextGeometry.path("coordinates");
                                if (lineCoords.isArray() && lineCoords.size() > 0) {
                                    JsonNode lastCoord = lineCoords.get(lineCoords.size() - 1);
                                    lastLineX = lastCoord.get(0).asDouble();
                                    lastLineY = lastCoord.get(1).asDouble();
                                }
                            } else if ("Point".equals(nextType)) {
                                break; // 다음 Point가 나오면 중단
                            }
                        }

                        double curX = coords.get(0).asDouble();
                        double curY = coords.get(1).asDouble();

                        // 1m 전 알림 좌표 계산
                        double noticeX = curX, noticeY = curY;

                        if (lastLineX != 0 && lastLineY != 0) {
                            // 마지막 LineString 끝점 기준으로 계산
                            double[] np = calcNoticePoint(lastLineX, lastLineY, curX, curY, NOTICE_DISTANCE);
                            noticeX = np[0];
                            noticeY = np[1];
                        } else if (prevX != 0 && prevY != 0) {
                            // LineString 없으면 이전 Point 기준
                            double[] np = calcNoticePoint(prevX, prevY, curX, curY, NOTICE_DISTANCE);
                            noticeX = np[0];
                            noticeY = np[1];
                        }

                        steps.add(RouteResponse.StepInfo.builder()
                                .description(props.path("description").asText())
                                .turnType(props.has("turnType") ? props.path("turnType").asText() : "")
                                .pointX(coords.get(0).asDouble())
                                .pointY(coords.get(1).asDouble())
                                .distance(dist)
                                .noticePointX(noticeX)
                                .noticePointY(noticeY)
                                .hapticType(resolveHapticType(
                                        props.has("turnType") ? props.path("turnType").asText() : ""
                                ))
                                .build());

                        prevX = curX;
                        prevY = curY;
                    }
                }
            }
        }

        return RouteResponse.builder()
                .totalDistance(totalDistance)
                .totalTime(totalTime)
                .noticeDistance(NOTICE_DISTANCE)
                .steps(steps)
                .build();
    }

    // 햅틱 패턴
    private HapticType resolveHapticType(String turnType) {
        if (turnType == null || turnType.isEmpty()) return HapticType.GO_STRAIGHT;

        return switch (turnType) {
            // 직진
            case "11"  -> HapticType.GO_STRAIGHT;
            case "233" -> HapticType.GO_STRAIGHT;

            // 좌회전 계열
            case "12"  -> HapticType.TURN_LEFT;
            case "16"  -> HapticType.TURN_LEFT;
            case "17"  -> HapticType.TURN_LEFT;

            // 우회전 계열
            case "13"  -> HapticType.TURN_RIGHT;
            case "18"  -> HapticType.TURN_RIGHT;
            case "19"  -> HapticType.TURN_RIGHT;

            // 도착
            case "201" -> HapticType.ARRIVED;

            // 1~7, 경유지 등은 일단 직진으로 처리
            default    -> HapticType.GO_STRAIGHT;
        };
    }

    // 1m 거리 계산 -> Haversine 공식 이용
    private double[] calcNoticePoint(double fromX, double fromY,  // 이전 Point (경도, 위도)
                                     double toX,   double toY,    // 현재 회전 Point (경도, 위도)
                                     double noticeDistance) {
        final int R = 6371000; // 지구 반지름

        // 라디안으로 변환
        double dLat = Math.toRadians(toY - fromY);
        double dLon = Math.toRadians(toX - fromX);

        // Haversine 공식
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(fromY)) * Math.cos(Math.toRadians(toY))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // 두 점 사이의 실제 거리
        double dist = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 두 점이 동일한 경우 회전 지점 좌표 그대로 반환
        if (dist == 0) return new double[]{toX, toY};

        // 전체 거리 중 noticeDistance가 차지하는 비율
        double ratio = noticeDistance / dist;

        // 회전 지점 noticeDistance 전 좌표
        return new double[]{
                toX - (toX - fromX) * ratio,  // noticePointX
                toY - (toY - fromY) * ratio   // noticePointY
        };
    }
}
