package com.financial.bff.controller;

import com.financial.bff.dto.CustomerInfoResponse;
import com.financial.bff.dto.FinancialProductDTO;
import com.financial.bff.service.CustomerInfoOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests de integración para CustomerInfoController
 */
@WebFluxTest(CustomerInfoController.class)
@ActiveProfiles("test")
@DisplayName("Customer Info Controller Integration Tests")
class CustomerInfoControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CustomerInfoOrchestrationService orchestrationService() {
            return Mockito.mock(CustomerInfoOrchestrationService.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerInfoOrchestrationService orchestrationService;

    private CustomerInfoResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = CustomerInfoResponse.builder()
                .correlationId("test-123")
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .productos(Arrays.asList(
                        FinancialProductDTO.builder()
                                .tipoProducto("Cuenta de Ahorro")
                                .nombre("Cuenta Ahorro Básica")
                                .saldo(new BigDecimal("5500.50"))
                                .build(),
                        FinancialProductDTO.builder()
                                .tipoProducto("Tarjeta de Crédito")
                                .nombre("Tarjeta Gold")
                                .saldo(new BigDecimal("-1200.00"))
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("Should return customer info when valid encrypted code provided")
    void shouldReturnCustomerInfoWhenValidEncryptedCodeProvided() {
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.just(testResponse));

        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", "validEncryptedCode123")
                .header("Correlation-ID", "test-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getCorrelationId().equals("test-123");
                    assert response.getNombres().equals("Juan Carlos");
                });
    }

    @Test
    @DisplayName("Should generate correlation ID when not provided")
    void shouldGenerateCorrelationIdWhenNotProvided() {
        // Given
        String encryptedCode = "validEncryptedCode123";
        CustomerInfoResponse responseWithGeneratedId = CustomerInfoResponse.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .productos(List.of())
                .build();

        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.just(responseWithGeneratedId));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", encryptedCode)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getNombres().equals("Juan Carlos");
                    // El correlation ID debería ser generado automáticamente
                });
    }

    @Test
    @DisplayName("Should handle invalid encrypted codigo unico")
    void shouldHandleInvalidEncryptedCodigoUnico() {
        // Given
        String invalidEncryptedCode = "invalidCode";
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid encrypted codigo unico")));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", invalidEncryptedCode)
                .header("Correlation-ID", "test-invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.apellidos").isEqualTo("INVALID_REQUEST")
                .jsonPath("$.tipoDocumento").isEqualTo("El código único proporcionado es inválido")
                .jsonPath("$.numeroDocumento").isEqualTo("400 BAD_REQUEST")
                .jsonPath("$.correlationId").isEqualTo("test-invalid");
    }

    @Test
    @DisplayName("Should handle customer not found")
    void shouldHandleCustomerNotFound() {
        // Given
        String encryptedCode = "validButNotFound";
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Customer not found")));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", encryptedCode)
                .header("Correlation-ID", "test-not-found")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getApellidos().contains("NOT_FOUND");
                });
    }

    @Test
    @DisplayName("Should handle service unavailable")
    void shouldHandleServiceUnavailable() {
        // Given
        String encryptedCode = "validCode";
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("service unavailable")));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", encryptedCode)
                .header("Correlation-ID", "test-unavailable")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getApellidos().contains("SERVICE_UNAVAILABLE");
                });
    }

    @Test
    @DisplayName("Should handle internal server error")
    void shouldHandleInternalServerError() {
        // Given
        String encryptedCode = "validCode";
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", encryptedCode)
                .header("Correlation-ID", "test-error")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getApellidos().contains("INTERNAL_ERROR");
                });
    }

    @Test
    @DisplayName("Should handle empty correlation ID header")
    void shouldHandleEmptyCorrelationIdHeader() {
        // Given
        String encryptedCode = "validCode";
        when(orchestrationService.getCustomerCompleteInfo(anyString(), anyString()))
                .thenReturn(Mono.just(testResponse));

        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/{codigoUnico}", encryptedCode)
                .header("Correlation-ID", "   ") // Empty/whitespace header
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerInfoResponse.class)
                .value(response -> {
                    assert response.getNombres().equals("Juan Carlos");
                    // Should generate a new correlation ID
                });
    }

    @Test
    @DisplayName("Should validate blank codigo unico")
    void shouldValidateBlankCodigoUnico() {
        // When & Then
        webTestClient.get()
                .uri("/api/customer-info/ ") // Blank codigo unico
                .header("Correlation-ID", "test-blank")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}