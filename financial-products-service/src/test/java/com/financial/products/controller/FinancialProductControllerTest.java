package com.financial.products.controller;

import com.financial.products.dto.FinancialProductResponseDTO;
import com.financial.products.service.FinancialProductService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests de integración para FinancialProductController
 */
@WebFluxTest(FinancialProductController.class)
@DisplayName("Financial Product Controller Integration Tests")
class FinancialProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FinancialProductService productService;

    private List<FinancialProductResponseDTO> mockProducts;

    private List<FinancialProductResponseDTO> testProducts;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public FinancialProductService financialProductService() {
            return Mockito.mock(FinancialProductService.class);
        }
    }

    @BeforeEach
    void setUp() {
        testProducts = Arrays.asList(
                FinancialProductResponseDTO.builder()
                        .tipoProducto("Cuenta de Ahorro")
                        .nombre("Cuenta Ahorro Básica")
                        .saldo(new BigDecimal("5500.50"))
                        .build(),
                FinancialProductResponseDTO.builder()
                        .tipoProducto("Tarjeta de Crédito")
                        .nombre("Tarjeta Gold")
                        .saldo(new BigDecimal("-1200.00"))
                        .build()
        );
    }

    @Test
    @DisplayName("Should return products when valid codigo unico provided")
    void shouldReturnProductsWhenValidCodigoUnicoProvided() {
        // Given
        when(productService.getProductsByCustomer("CUST001"))
                .thenReturn(Mono.just(testProducts));

        // When & Then
        webTestClient.get()
                .uri("/api/financial-products/customer/CUST001")
                .header("Correlation-ID", "test-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(FinancialProductResponseDTO.class)
                .value(products -> {
                    assert products.size() == 2;
                    assert products.get(0).getTipoProducto().equals("Cuenta de Ahorro");
                    assert products.get(0).getNombre().equals("Cuenta Ahorro Básica");
                    assert products.get(0).getSaldo().equals(new BigDecimal("5500.50"));

                    assert products.get(1).getTipoProducto().equals("Tarjeta de Crédito");
                    assert products.get(1).getNombre().equals("Tarjeta Gold");
                    assert products.get(1).getSaldo().equals(new BigDecimal("-1200.00"));
                });
    }

    @Test
    @DisplayName("Should return 404 when no products found")
    void shouldReturn404WhenNoProductsFound() {
        // Given
        when(productService.getProductsByCustomer("CUST_NO_PRODUCTS"))
                .thenReturn(Mono.error(new RuntimeException("No financial products found for customer")));

        // When & Then
        webTestClient.get()
                .uri("/api/financial-products/customer/CUST_NO_PRODUCTS")
                .header("Correlation-ID", "test-404")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return products without correlation ID header")
    void shouldReturnProductsWithoutCorrelationIdHeader() {
        // Given
        when(productService.getProductsByCustomer("CUST001"))
                .thenReturn(Mono.just(testProducts));

        // When & Then
        webTestClient.get()
                .uri("/api/financial-products/customer/CUST001")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FinancialProductResponseDTO.class)
                .value(products -> {
                    assert products.size() == 2;
                });
    }

    @Test
    @DisplayName("Should handle service errors gracefully")
    void shouldHandleServiceErrorsGracefully() {
        // Given
        when(productService.getProductsByCustomer(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Database connection error")));

        // When & Then
        webTestClient.get()
                .uri("/api/financial-products/customer/ERROR_CASE")
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
                .uri("/api/financial-products/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert response.equals("Financial Products Service is UP");
                });
    }

    @Test
    @DisplayName("Should handle special characters in codigo unico")
    void shouldHandleSpecialCharactersInCodigoUnico() {
        // Given
        String codigoWithSpecialChars = "CUST-001_TEST";
        when(productService.getProductsByCustomer(codigoWithSpecialChars))
                .thenReturn(Mono.just(testProducts));

        // When & Then
        webTestClient.get()
                .uri("/api/financial-products/customer/{codigo}", codigoWithSpecialChars)
                .header("Correlation-ID", "test-special-chars")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FinancialProductResponseDTO.class)
                .value(products -> {
                    assert products.size() == 2;
                });
    }
}