package com.axis.nachimbal.domain.route.controller;

import com.axis.nachimbal.domain.route.dto.RouteSummaryResponse;
import com.axis.nachimbal.domain.route.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/routes")
@RequiredArgsConstructor
public class GpsArtAdminController {

    private final ArchiveService archiveService;

    /**
     * GPS 아트 경로 등록.
     * Postman에서 multipart/form-data로 호출:
     *   - name:    경로 이름 (예: 광화문 댕댕런)
     *   - address: 대표 주소 (예: 서울특별시 종로구)
     *   - gpxFile: GPX 파일
     */
    @PostMapping("/gps-art")
    public ResponseEntity<RouteSummaryResponse> createGpsArt(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestPart("gpxFile") MultipartFile gpxFile
    ) {
        RouteSummaryResponse response = archiveService.createGpsArt(name, address, gpxFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
