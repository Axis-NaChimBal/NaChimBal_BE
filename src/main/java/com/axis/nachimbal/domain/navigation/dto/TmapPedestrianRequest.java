package com.axis.nachimbal.domain.navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TmapPedestrianRequest {
    private String startName;
    private String startX;
    private String startY;

    private String endName;
    private String endX;
    private String endY;

    private String searchOption;
    private String reqCoordType;
    private String resCoordType;
}