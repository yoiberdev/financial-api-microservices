package com.financial.customer.controller;

import com.financial.customer.dto.CustomerResponseDTO;
import com.financial.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests de integración para CustomerController
 */
@WebFluxTest(CustomerController.class)
@DisplayName("Customer Controller Integration Tests")
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerService customerService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CustomerService customerService() {
            return Mockito.mock(CustomerService.class);
        }
    }

    private CustomerResponseDTO testCustomerResponse;

    @BeforeEach
    void setUp() {
        testCustomerResponse = CustomerResponseDTO.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .build();
    }

    @Test
    @DisplayName("Should return customer when valid codigo unico provided")
    void shouldReturnCustomerWhenValidCodigoUnicoProvided() {
        // Given
        when(customerService.getCustomerByCodigoUnico("CUST001"))
                .thenReturn(Mono.just(testCustomerResponse));

        // When & Then
        webTestClient.get()
                .uri("/api/customers/CUST001")
                .header("Correlation-ID", "test-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .value(response -> {
                    assert response.getNombres().equals("Juan Carlos");
                    assert response.getApellidos().equals("Pérez García");
                    assert response.getTipoDocumento().equals("DNI");
                    assert response.getNumeroDocumento().equals("12345678");
                });
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() {
        // Given
        when(customerService.getCustomerByCodigoUnico("NONEXISTENT"))
                .thenReturn(Mono.error(new RuntimeException("Customer not found")));

        // When & Then
        webTestClient.get()
                .uri("/api/customers/NONEXISTENT")
                .header("Correlation-ID", "test-404")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return customer without correlation ID header")
    void shouldReturnCustomerWithoutCorrelationIdHeader() {
        // Given
        when(customerService.getCustomerByCodigoUnico("CUST001"))
                .thenReturn(Mono.just(testCustomerResponse));

        // When & Then
        webTestClient.get()
                .uri("/api/customers/CUST001")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(response -> {
                    assert response.getNombres().equals("Juan Carlos");
                });
    }

    @Test
    @DisplayName("Should handle service errors gracefully")
    void shouldHandleServiceErrorsGracefully() {
        // Given
        when(customerService.getCustomerByCodigoUnico(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Internal service error")));

        // When & Then
        webTestClient.get()
                .uri("/api/customers/ERROR_CASE")
                .header("Correlation-ID", "test-error")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Debido al onErrorReturn en el controller
    }

    @Test
    @DisplayName("Should return health check successfully")
    void shouldReturnHealthCheckSuccessfully() {
        // When & Then
        webTestClient.get()
                .uri("/api/customers/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert response.equals("Customer Service is UP");
                });
    }
}