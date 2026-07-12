package com.axis.nachimbal.domain.route.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.axis.nachimbal.domain.route.dto.PathPointRequest;

import java.util.List;
import java.util.stream.Collectors;

public final class PolylineCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PolylineCodec() {}

    public record LatLng(double lat, double lng) {}

    /** RouteSetupScreen이 보낸 fullPath({x,y}, x=경도 y=위도)를 저장용 JSON 문자열로 직렬화 */
    public static String encode(List<PathPointRequest> fullPath) {
        List<LatLng> points = fullPath.stream()
                .map(p -> new LatLng(p.getY(), p.getX()))
                .collect(Collectors.toList());
        try {
            return MAPPER.writeValueAsString(points);
        } catch (Exception e) {
            throw new IllegalArgumentException("경로 좌표(fullPath)를 저장 형식으로 변환하는 데 실패했습니다.", e);
        }
    }

    /** 저장된 JSON 문자열을 LatLng 리스트로 역직렬화 */
    public static List<LatLng> decode(String polylineJson) {
        try {
            return MAPPER.readValue(polylineJson, new TypeReference<List<LatLng>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("저장된 경로 좌표(polyline)를 읽는 데 실패했습니다.", e);
        }
    }

    /** 첫 좌표(보통 출발지) - 역지오코딩에 사용 */
    public static LatLng firstPoint(List<PathPointRequest> fullPath) {
        PathPointRequest first = fullPath.get(0);
        return new LatLng(first.getY(), first.getX());
    }
}
