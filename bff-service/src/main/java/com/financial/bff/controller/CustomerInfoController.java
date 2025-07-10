package com.financial.bff.controller;

import com.financial.bff.dto.CustomerInfoResponse;
import com.financial.bff.dto.ErrorResponse;
import com.financial.bff.service.CustomerInfoOrchestrationService;
import com.financial.common.annotation.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controlador principal del BFF que expone la API unificada
 * Orquesta llamadas a microservicios y maneja la autenticación OAuth2
 */
@RestController
@RequestMapping("/api/customer-info")
@Validated
@Slf4j
@Tag(name = "Customer Info API", description = "API del BFF para información completa del cliente")
@SecurityRequirement(name = "oauth2")
public class CustomerInfoController {

    private final CustomerInfoOrchestrationService orchestrationService;

    public CustomerInfoController(CustomerInfoOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping("/{codigoUnico}")
    @Operation(
            summary = "Obtener información completa del cliente",
            description = "Endpoint principal que orquesta la obtención de datos del cliente y sus productos financieros desde múltiples microservicios",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Información del cliente obtenida exitosamente",
                            content = @Content(schema = @Schema(implementation = CustomerInfoResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Código único inválido o mal formateado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado - Token JWT inválido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cliente no encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )

    @Loggable(value = "Endpoint principal BFF", includeArgs = true)
    public Mono<ResponseEntity<CustomerInfoResponse>> getCustomerInfo(
            @Parameter(description = "Código único del cliente (encriptado)", required = true)
            @PathVariable @NotBlank String codigoUnico,

            @Parameter(description = "ID de correlación para tracking distribuido")
            @RequestHeader(value = "Correlation-ID", required = false) String correlationId) {

        // Generar correlation ID si no se proporciona
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        log.info("Received request for customer info - codigo: {}, correlation-id: {}",
                codigoUnico, finalCorrelationId);

        return orchestrationService.getCustomerCompleteInfo(codigoUnico, finalCorrelationId)
                .map(customerInfo -> {
                    log.info("Successfully processed customer info request - {}", finalCorrelationId);
                    return ResponseEntity.ok(customerInfo);
                })
                .onErrorResume(error -> {
                    log.error("Error processing customer info request: {} - {}",
                            error.getMessage(), finalCorrelationId);
                    return handleError(error, finalCorrelationId, "/api/customer-info/" + codigoUnico);
                });
    }

    /**
     * Maneja errores y devuelve respuestas apropiadas
     */
    @Loggable(value = "Manejo de error en BFF", includeArgs = true, includeResult = true)
    private Mono<ResponseEntity<CustomerInfoResponse>> handleError(Throwable error, String correlationId, String path) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        HttpStatus status;
        String errorMessage = error.getMessage();

        if (errorMessage.contains("Invalid encrypted codigo unico") ||
                errorMessage.contains("must be alphanumeric")) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse.setError("INVALID_REQUEST");
            errorResponse.setMessage("El código único proporcionado es inválido");
        } else if (errorMessage.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            errorResponse.setError("NOT_FOUND");
            errorResponse.setMessage("Cliente no encontrado");
        } else if (errorMessage.contains("service unavailable")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorResponse.setError("SERVICE_UNAVAILABLE");
            errorResponse.setMessage("Uno o más servicios no están disponibles");
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse.setError("INTERNAL_ERROR");
            errorResponse.setMessage("Error interno del servidor");
        }

        // Convertir ErrorResponse a CustomerInfoResponse para mantener la firma del método
        CustomerInfoResponse errorAsCustomerResponse = CustomerInfoResponse.builder()
                .correlationId(correlationId)
                .nombres("ERROR")
                .apellidos(errorResponse.getError())
                .tipoDocumento(errorResponse.getMessage())
                .numeroDocumento(status.toString())
                .build();

        return Mono.just(ResponseEntity.status(status).body(errorAsCustomerResponse));
    }
}
