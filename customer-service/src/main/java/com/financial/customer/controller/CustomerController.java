package com.financial.customer.controller;

import com.financial.common.annotation.Loggable;
import com.financial.customer.dto.CustomerResponseDTO;
import com.financial.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para Customer Service
 */
@RestController
@RequestMapping("/api/customers")
@Slf4j
@Tag(name = "Customer API", description = "API para gestión de clientes")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{codigoUnico}")
    @Operation(
            summary = "Obtener cliente por código único",
            description = "Busca y retorna la información básica de un cliente usando su código único"
    )
    @Loggable(value = "Endpoint obtener cliente", includeArgs = true)
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomer(
            @Parameter(description = "Código único del cliente", required = true)
            @PathVariable String codigoUnico,

            @Parameter(description = "ID de correlación para tracking")
            @RequestHeader(value = "Correlation-ID", required = false) String correlationId) {

        log.info("Received request for customer: {} with correlation-id: {}",
                codigoUnico, correlationId);

        return customerService.getCustomerByCodigoUnico(codigoUnico)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Customer request completed successfully - {}", correlationId))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que el servicio esté funcionando")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Customer Service is UP"));
    }
}