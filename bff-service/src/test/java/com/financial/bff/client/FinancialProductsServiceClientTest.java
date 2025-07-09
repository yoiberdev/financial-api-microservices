package com.financial.bff.client;

import com.financial.bff.dto.FinancialProductDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Tests unitarios para FinancialProductsServiceClient usando MockWebServer
 */
@DisplayName("Financial Products Service Client Tests")
class FinancialProductsServiceClientTest {

    private MockWebServer mockWebServer;
    private FinancialProductsServiceClient financialProductsServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        financialProductsServiceClient = new FinancialProductsServiceClient(
                WebClient.builder(),
                baseUrl
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return products when service responds successfully")
    void shouldReturnProductsWhenServiceRespondsSuccessfully() {
        // Given
        String responseBody = """
                [
                    {
                        "tipoProducto": "Cuenta de Ahorro",
                        "nombre": "Cuenta Ahorro Básica",
                        "saldo": 5500.50
                    },
                    {
                        "tipoProducto": "Tarjeta de Crédito",
                        "nombre": "Tarjeta Gold",
                        "saldo": -1200.00
                    }
                ]
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("CUST001")
                .contextWrite(Context.of("correlationId", "test-123"));

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 2;
                    assert products.get(0).getTipoProducto().equals("Cuenta de Ahorro");
                    assert products.get(0).getNombre().equals("Cuenta Ahorro Básica");
                    assert products.get(0).getSaldo().equals(new BigDecimal("5500.50"));
                    assert products.get(1).getTipoProducto().equals("Tarjeta de Crédito");
                    assert products.get(1).getNombre().equals("Tarjeta Gold");
                    assert products.get(1).getSaldo().equals(new BigDecimal("-1200.00"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void shouldReturnEmptyListWhenNoProductsFound() {
        // Given
        String responseBody = "[]";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("CUST_NO_PRODUCTS")
                .contextWrite(Context.of("correlationId", "test-empty"));

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> products.isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle 404 error from service")
    void shouldHandle404ErrorFromService() {
        // Given
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"error\": \"Products not found\"}")
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("NONEXISTENT")
                .contextWrite(Context.of("correlationId", "test-404"));

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Financial products not found or service unavailable")
                )
                .verify();
    }

    @Test
    @DisplayName("Should handle 500 error from service")
    void shouldHandle500ErrorFromService() {
        // Given
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"error\": \"Internal server error\"}")
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("ERROR_CASE")
                .contextWrite(Context.of("correlationId", "test-500"));

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Financial products not found or service unavailable")
                )
                .verify();
    }

    @Test
    @DisplayName("Should handle correlation ID from context")
    void shouldHandleCorrelationIdFromContext() throws InterruptedException {
        // Given
        String responseBody = "[]";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("CUST001")
                .contextWrite(Context.of("correlationId", "specific-test-id"));

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        // Verificar que el header Correlation-ID fue enviado
        var request = mockWebServer.takeRequest();
        assert request.getHeader("Correlation-ID").equals("specific-test-id");
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void shouldHandleMalformedJsonResponse() {
        // Given
        String malformedJson = "{ invalid json response }";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(malformedJson)
        );

        // When
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("CUST001")
                .contextWrite(Context.of("correlationId", "test-malformed"));

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    @DisplayName("Should handle unknown correlation ID gracefully")
    void shouldHandleUnknownCorrelationIdGracefully() {
        // Given
        String responseBody = "[]";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When - Sin contexto de correlationId
        Mono<List<FinancialProductDTO>> result = financialProductsServiceClient
                .getFinancialProducts("CUST001");

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }
}
