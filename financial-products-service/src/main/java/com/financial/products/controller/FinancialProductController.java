package com.financial.products.controller;

import com.financial.common.annotation.Loggable;
import com.financial.products.dto.FinancialProductResponseDTO;
import com.financial.products.service.FinancialProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador REST para Financial Products Service
 */
@RestController
@RequestMapping("/api/financial-products")
@Slf4j
@Tag(name = "Financial Products API", description = "API para gestión de productos financieros")
public class FinancialProductController {

    private final FinancialProductService productService;

    public FinancialProductController(FinancialProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/customer/{codigoUnico}")
    @Operation(
            summary = "Obtener productos financieros por cliente",
            description = "Busca y retorna todos los productos financieros asociados a un cliente"
    )
    @Loggable(value = "Endpoint productos financieros", includeArgs = true)
    public Mono<ResponseEntity<List<FinancialProductResponseDTO>>> getProductsByCustomer(
            @Parameter(description = "Código único del cliente", required = true)
            @PathVariable String codigoUnico,

            @Parameter(description = "ID de correlación para tracking")
            @RequestHeader(value = "Correlation-ID", required = false) String correlationId) {

        log.info("Received request for products of customer: {} with correlation-id: {}",
                codigoUnico, correlationId);

        return productService.getProductsByCustomer(codigoUnico)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Products request completed successfully - {}", correlationId))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que el servicio esté funcionando")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Financial Products Service is UP"));
    }
}