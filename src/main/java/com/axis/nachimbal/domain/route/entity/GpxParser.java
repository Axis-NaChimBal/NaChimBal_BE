package com.axis.nachimbal.domain.route.entity;

import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GPX 파일에서 경로 좌표를 추출하는 유틸리티.
 * GPX 포맷: <trkpt lat="위도" lon="경도"> 태그에서 좌표를 읽습니다.
 */
public final class GpxParser {

    private GpxParser() {}

    public record GpxPoint(double lat, double lng) {}

    /**
     * MultipartFile로 업로드된 GPX 파일을 파싱해서 좌표 목록을 반환합니다.
     */
    public static List<GpxPoint> parse(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // XXE 공격 방지
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList trkpts = doc.getElementsByTagName("trkpt");
            List<GpxPoint> points = new ArrayList<>();

            for (int i = 0; i < trkpts.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) trkpts.item(i);
                double lat = Double.parseDouble(el.getAttribute("lat"));
                double lon = Double.parseDouble(el.getAttribute("lon"));
                points.add(new GpxPoint(lat, lon));
            }

            if (points.isEmpty()) {
                throw new IllegalArgumentException("GPX 파일에서 경로 좌표를 찾을 수 없습니다.");
            }

            return points;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("GPX 파일 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 좌표 목록에서 총 거리를 계산합니다 (Haversine 공식, 단위: km).
     */
    public static double calculateDistanceKm(List<GpxPoint> points) {
        double total = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            total += haversine(points.get(i), points.get(i + 1));
        }
        return Math.round(total * 10) / 10.0; // 소수점 첫째 자리
    }

    private static double haversine(GpxPoint a, GpxPoint b) {
        final double R = 6371.0; // 지구 반지름 (km)
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLon = Math.toRadians(b.lng() - a.lng());
        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(a.lat()))
                * Math.cos(Math.toRadians(b.lat()))
                * sinLon * sinLon;
        return 2 * R * Math.asin(Math.sqrt(h));
    }
}
