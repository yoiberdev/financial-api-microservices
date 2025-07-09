package com.financial.bff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controlador para health checks y monitoreo
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Endpoints para verificación de salud del servicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check básico", description = "Verifica que el BFF esté funcionando")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of(
                "status", "UP",
                "service", "bff-service",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}