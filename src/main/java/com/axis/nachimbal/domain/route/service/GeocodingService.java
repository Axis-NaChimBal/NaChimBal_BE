package com.axis.nachimbal.domain.route.service;

import com.axis.nachimbal.domain.route.dto.NcpReverseGeocodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GeocodingService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String reverseGeocodeUrl;

    public GeocodingService(
            WebClient.Builder webClientBuilder,
            @Value("${ncp.maps.client-id}") String clientId,
            @Value("${ncp.maps.client-secret}") String clientSecret,
            @Value("${ncp.maps.reverse-geocode-url}") String reverseGeocodeUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.reverseGeocodeUrl = reverseGeocodeUrl;
    }

    public Optional<String> reverseGeocode(double latitude, double longitude) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.warn("NCP Maps API 키가 설정되지 않아 주소 변환을 건너뜁니다.");
            return Optional.empty();
        }

        try {
            String coords = longitude + "," + latitude;

            String url = UriComponentsBuilder.fromUriString(reverseGeocodeUrl)
                    .queryParam("coords", coords)
                    .queryParam("output", "json")
                    .queryParam("orders", "roadaddr,addr")
                    .build()
                    .toUriString();

            URI uri = URI.create(url);

            NcpReverseGeocodeResponse response = webClient.get()
                    .uri(uri)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .bodyToMono(NcpReverseGeocodeResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            return Optional.ofNullable(response)
                    .map(this::extractAddress)
                    .filter(addr -> !addr.isBlank());

        } catch (Exception e) {
            log.warn("NCP Reverse Geocoding 호출 실패 (lat={}, lng={}): {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }

    private String extractAddress(NcpReverseGeocodeResponse response) {
        if (response.getResults() == null || response.getResults().isEmpty()) {
            return "";
        }

        List<NcpReverseGeocodeResponse.Result> results = response.getResults();

        NcpReverseGeocodeResponse.Result preferred = results.stream()
                .filter(r -> "roadaddr".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> results.stream()
                        .filter(r -> "addr".equals(r.getName()))
                        .findFirst()
                        .orElse(results.get(0)));

        StringBuilder sb = new StringBuilder();
        if (preferred.getRegion() != null) {
            appendIfPresent(sb, preferred.getRegion().getArea1());
            appendIfPresent(sb, preferred.getRegion().getArea2());
            appendIfPresent(sb, preferred.getRegion().getArea3());
        }
        if (preferred.getLand() != null && preferred.getLand().getName() != null) {
            sb.append(' ').append(preferred.getLand().getName());
            if (preferred.getLand().getNumber1() != null && !preferred.getLand().getNumber1().isBlank()) {
                sb.append(' ').append(preferred.getLand().getNumber1());
            }
        }

        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    private void appendIfPresent(StringBuilder sb, NcpReverseGeocodeResponse.Area area) {
        if (area != null && area.getName() != null && !area.getName().isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(area.getName());
        }
    }
}
