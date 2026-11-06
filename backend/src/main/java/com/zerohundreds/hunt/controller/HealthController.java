package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.ok("Zero to Hundred's Hunt Core API is operational", Map.of(
                "status", "UP",
                "platform", "Zero to Hundred's Hunt",
                "version", "1.0.0",
                "timestamp", LocalDateTime.now()
        )));
    }
}
