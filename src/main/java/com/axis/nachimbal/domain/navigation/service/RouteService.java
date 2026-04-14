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
        JsonNode root = objectMapper.readTree(body);
        JsonNode features = root.path("features");

        int totalDistance = 0, totalTime = 0;

        List<RouteResponse.StepInfo> steps = new ArrayList<>();
        List<RouteResponse.Coordinate> fullPath = new ArrayList<>();

        List<JsonNode> featureList = new ArrayList<>();
        features.forEach(featureList::add);

        for (int i = 0; i < featureList.size(); i++) {
            JsonNode feature = featureList.get(i);
            JsonNode props = feature.path("properties");
            JsonNode geometry = feature.path("geometry");
            String geoType = geometry.path("type").asText();

            // 전체 경로 polyline 구성
            if ("LineString".equals(geoType)) {
                JsonNode coords = geometry.path("coordinates");
                for (int k = 0; k < coords.size(); k++) {
                    JsonNode coord = coords.get(k);
                    double x = coord.get(0).asDouble();
                    double y = coord.get(1).asDouble();

                    if (!fullPath.isEmpty()) {
                        RouteResponse.Coordinate last = fullPath.get(fullPath.size() - 1);
                        if (isSameCoordinate(last.getX(), last.getY(), x, y)) {
                            continue;
                        }
                    }

                    fullPath.add(new RouteResponse.Coordinate(x, y));
                }
            }

            // 안내 이벤트 처리
            if (!"Point".equals(geoType)) {
                continue;
            }

            JsonNode coords = geometry.path("coordinates");
            if (!coords.isArray() || coords.size() < 2) {
                continue;
            }

            // totalDistance / totalTime은 보통 시작 Point에 들어있음
            if (props.has("totalDistance")) {
                totalDistance = props.path("totalDistance").asInt();
                totalTime = props.path("totalTime").asInt();
            }

            double pointX = coords.get(0).asDouble();
            double pointY = coords.get(1).asDouble();

            String description = props.path("description").asText("");
            String turnType = props.path("turnType").asText("");

            // 1) incomingLine 찾기 -> noticePoint 계산용
            JsonNode incomingLine = findPreviousLineString(featureList, i);

            double noticePointX = pointX;
            double noticePointY = pointY;

            if (incomingLine != null) {
                List<RouteResponse.Coordinate> incomingPolyline = extractCoordinates(incomingLine.path("geometry").path("coordinates"));

                if (!incomingPolyline.isEmpty()) {
                    RouteResponse.Coordinate noticePoint =
                            calcNoticePointFromPolyline(incomingPolyline, pointX, pointY, NOTICE_DISTANCE);

                    noticePointX = noticePoint.getX();
                    noticePointY = noticePoint.getY();
                }
            }

            // 2) outgoingLines 거리 합산 -> 현재 Point 이후 다음 Point 전까지
            int distanceToNextPoint = 0;
            for (int j = i + 1; j < featureList.size(); j++) {
                JsonNode next = featureList.get(j);
                String nextType = next.path("geometry").path("type").asText();

                if ("LineString".equals(nextType)) {
                    distanceToNextPoint += next.path("properties").path("distance").asInt();
                } else if ("Point".equals(nextType)) {
                    break;
                }
            }

            steps.add(RouteResponse.StepInfo.builder()
                    .description(description)
                    .turnType(turnType)
                    .pointX(pointX)
                    .pointY(pointY)
                    .distance(distanceToNextPoint)
                    .noticePointX(noticePointX)
                    .noticePointY(noticePointY)
                    .hapticType(resolveHapticType(turnType))
                    .build());
        }

        return RouteResponse.builder()
                .totalDistance(totalDistance)
                .totalTime(totalTime)
                .noticeDistance(NOTICE_DISTANCE)
                .steps(steps)
                .fullPath(fullPath)
                .build();
    }

    // LineString 찾기
    private JsonNode findPreviousLineString(List<JsonNode> featureList, int currentIndex) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            JsonNode feature = featureList.get(i);
            String geoType = feature.path("geometry").path("type").asText();

            if ("LineString".equals(geoType)) {
                return feature;
            } else if ("Point".equals(geoType)) {
                // Point가 또 나왔다면 그 이전에 incoming line이 없는 구조일 수 있음
                break;
            }
        }
        return null;
    }

    // LineString 좌표 변환
    private List<RouteResponse.Coordinate> extractCoordinates(JsonNode coordsNode) {
        List<RouteResponse.Coordinate> result = new ArrayList<>();

        if (coordsNode == null || !coordsNode.isArray()) {
            return result;
        }

        for (JsonNode coord : coordsNode) {
            if (coord.isArray() && coord.size() >= 2) {
                result.add(new RouteResponse.Coordinate(
                        coord.get(0).asDouble(),
                        coord.get(1).asDouble()
                ));
            }
        }

        return result;
    }

    // noticePoint 계산
    private RouteResponse.Coordinate calcNoticePointFromPolyline(
            List<RouteResponse.Coordinate> polyline,
            double pointX,
            double pointY,
            double noticeDistance
    ) {
        // 안전 처리
        if (polyline == null || polyline.isEmpty()) {
            return new RouteResponse.Coordinate(pointX, pointY);
        }

        if (polyline.size() == 1) {
            return polyline.get(0);
        }

        // turnPoint에 가장 가까운 점을 찾아서 거기서부터 역추적
        double remaining = noticeDistance;

        int startIndex = findClosestIndex(polyline, pointX, pointY);
        for (int i = startIndex; i > 0; i--) {
            RouteResponse.Coordinate to = polyline.get(i);
            RouteResponse.Coordinate from = polyline.get(i - 1);

            double segmentDistance = distanceMeters(from.getX(), from.getY(), to.getX(), to.getY());

            if (segmentDistance <= 0) {
                continue;
            }

            if (remaining <= segmentDistance) {
                // 현재 선분 안에서 notice point를 찾을 수 있음
                double ratio = remaining / segmentDistance;

                double noticeX = to.getX() - (to.getX() - from.getX()) * ratio;
                double noticeY = to.getY() - (to.getY() - from.getY()) * ratio;

                return new RouteResponse.Coordinate(noticeX, noticeY);
            }

            remaining -= segmentDistance;
        }

        // noticeDistance가 incoming polyline 전체 길이보다 길면 시작점으로 clamp
        return polyline.get(0);
    }

    // 거리 계산 -> 하버사인
    private double distanceMeters(double fromX, double fromY, double toX, double toY) {
        final double R = 6371000.0;

        double dLat = Math.toRadians(toY - fromY);
        double dLon = Math.toRadians(toX - fromX);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(fromY)) * Math.cos(Math.toRadians(toY))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // 가장 가깐운 회전지점 찾기
    private int findClosestIndex(List<RouteResponse.Coordinate> polyline, double px, double py) {
        int closest = polyline.size() - 1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < polyline.size(); i++) {
            double d = distanceMeters(polyline.get(i).getX(), polyline.get(i).getY(), px, py);
            if (d < minDist) {
                minDist = d;
                closest = i;
            }
        }
        return closest;
    }

    // 중복 좌표 비교
    private boolean isSameCoordinate(double x1, double y1, double x2, double y2) {
        double epsilon = 1e-10;
        return Math.abs(x1 - x2) < epsilon && Math.abs(y1 - y2) < epsilon;
    }

    // 햅틱 패턴
    private HapticType resolveHapticType(String turnType) {
        if (turnType == null || turnType.isEmpty()) return HapticType.GO_STRAIGHT;

        return switch (turnType) {
            // 출발
            case "200" -> HapticType.START;

            // 직진
            case "11"  -> HapticType.GO_STRAIGHT;
            case "233" -> HapticType.GO_STRAIGHT;

            // 좌회전 계열
            case "12"  -> HapticType.TURN_LEFT;
            case "16"  -> HapticType.TURN_LEFT;
            case "17"  -> HapticType.TURN_LEFT;
            case "212"  -> HapticType.TURN_LEFT;
            case "214"  -> HapticType.TURN_LEFT;
            case "215"  -> HapticType.TURN_LEFT;

            // 우회전 계열
            case "13"  -> HapticType.TURN_RIGHT;
            case "18"  -> HapticType.TURN_RIGHT;
            case "19"  -> HapticType.TURN_RIGHT;
            case "213"  -> HapticType.TURN_RIGHT;
            case "216"  -> HapticType.TURN_RIGHT;
            case "217"  -> HapticType.TURN_RIGHT;

            // 도착
            case "201" -> HapticType.ARRIVED;

            // 1~7, 경유지 등은 일단 직진으로 처리
            default    -> HapticType.GO_STRAIGHT;
        };
    }
}
