package com.financial.bff.client;

import com.financial.bff.dto.CustomerDTO;
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
import java.util.Objects;

/**
 * Tests unitarios para CustomerServiceClient usando MockWebServer
 */
@DisplayName("Customer Service Client Tests")
class CustomerServiceClientTest {

    private MockWebServer mockWebServer;
    private CustomerServiceClient customerServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        customerServiceClient = new CustomerServiceClient(
                WebClient.builder(),
                baseUrl
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return customer when service responds successfully")
    void shouldReturnCustomerWhenServiceRespondsSuccessfully() {
        // Given
        String responseBody = """
                {
                    "nombres": "Juan Carlos",
                    "apellidos": "Pérez García",
                    "tipoDocumento": "DNI",
                    "numeroDocumento": "12345678"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When
        Mono<CustomerDTO> result = customerServiceClient.getCustomer("CUST001")
                .contextWrite(Context.of("correlationId", "test-123"));

        // Then
        StepVerifier.create(result)
                .expectNextMatches(customer ->
                        customer.getNombres().equals("Juan Carlos") &&
                                customer.getApellidos().equals("Pérez García") &&
                                customer.getTipoDocumento().equals("DNI") &&
                                customer.getNumeroDocumento().equals("12345678")
                )
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
                        .setBody("{\"error\": \"Customer not found\"}")
        );

        // When
        Mono<CustomerDTO> result = customerServiceClient.getCustomer("NONEXISTENT")
                .contextWrite(Context.of("correlationId", "test-404"));

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Customer not found or service unavailable")
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
        Mono<CustomerDTO> result = customerServiceClient.getCustomer("ERROR_CASE")
                .contextWrite(Context.of("correlationId", "test-500"));

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Customer not found or service unavailable")
                )
                .verify();
    }

    @Test
    @DisplayName("Should handle correlation ID from context")
    void shouldHandleCorrelationIdFromContext() throws InterruptedException {
        // Given
        String responseBody = """
                {
                    "nombres": "Test",
                    "apellidos": "User",
                    "tipoDocumento": "DNI",
                    "numeroDocumento": "00000000"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseBody)
        );

        // When
        Mono<CustomerDTO> result = customerServiceClient.getCustomer("CUST001")
                .contextWrite(Context.of("correlationId", "specific-test-id"));

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        // Verificar que el header Correlation-ID fue enviado
        var request = mockWebServer.takeRequest();
        assert Objects.equals(request.getHeader("Correlation-ID"), "specific-test-id");
    }
}