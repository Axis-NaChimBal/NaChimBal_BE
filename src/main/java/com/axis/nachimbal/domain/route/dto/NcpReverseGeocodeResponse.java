package com.axis.nachimbal.domain.route.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NcpReverseGeocodeResponse {

    private Status status;
    private List<Result> results;

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private int code;
        private String name;
        private String message;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String name;
        private Region region;
        private Land land;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Region {
        private Area area1;
        private Area area2;
        private Area area3;
        private Area area4;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Area {
        private String name;
    }

    @Getter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Land {
        private String name;
        private String number1;
        private String number2;
    }
}
