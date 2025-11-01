package com.example.wagemanager.api.common;

import com.example.wagemanager.common.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/health")
    public ApiResponse<HealthCheckResponse> healthCheck() {
        return ApiResponse.success(
                HealthCheckResponse.builder()
                        .status("OK")
                        .message("WageManager API is running")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class HealthCheckResponse {
        private String status;
        private String message;
        private LocalDateTime timestamp;
    }
}
