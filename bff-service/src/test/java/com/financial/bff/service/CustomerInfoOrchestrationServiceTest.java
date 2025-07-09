package com.financial.bff.service;

import com.financial.bff.client.CustomerServiceClient;
import com.financial.bff.client.FinancialProductsServiceClient;
import com.financial.bff.dto.CustomerDTO;
import com.financial.bff.dto.CustomerInfoResponse;
import com.financial.bff.dto.FinancialProductDTO;
import com.financial.common.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CustomerInfoOrchestrationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Info Orchestration Service Tests")
class CustomerInfoOrchestrationServiceTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Mock
    private FinancialProductsServiceClient financialProductsServiceClient;

    @InjectMocks
    private CustomerInfoOrchestrationService orchestrationService;

    private CustomerDTO testCustomer;
    private List<FinancialProductDTO> testProducts;
    private String encryptedCode;
    private String decryptedCode;
    private String correlationId;

    @BeforeEach
    void setUp() {
        testCustomer = CustomerDTO.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .build();

        testProducts = Arrays.asList(
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
        );

        encryptedCode = "encrypted123";
        decryptedCode = "CUST001";
        correlationId = "test-correlation-123";
    }

    @Test
    @DisplayName("Should orchestrate successfully when all services return data")
    void shouldOrchestrateSuccessfullyWhenAllServicesReturnData() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(testProducts));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assert response.getCorrelationId().equals(correlationId);
                    assert response.getNombres().equals("Juan Carlos");
                    assert response.getApellidos().equals("Pérez García");
                    assert response.getTipoDocumento().equals("DNI");
                    assert response.getNumeroDocumento().equals("12345678");
                    assert response.getProductos().size() == 2;
                    assert response.getProductos().get(0).getTipoProducto().equals("Cuenta de Ahorro");
                    assert response.getProductos().get(1).getTipoProducto().equals("Tarjeta de Crédito");
                    return true;
                })
                .verifyComplete();

        verify(encryptionService, times(1)).decrypt(encryptedCode);
        verify(customerServiceClient, times(1)).getCustomer(decryptedCode);
        verify(financialProductsServiceClient, times(1)).getFinancialProducts(decryptedCode);
    }

    @Test
    @DisplayName("Should fail when decryption fails")
    void shouldFailWhenDecryptionFails() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenThrow(new RuntimeException("Decryption failed"));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Invalid encrypted codigo unico")
                )
                .verify();

        verify(encryptionService, times(1)).decrypt(encryptedCode);
        verify(customerServiceClient, never()).getCustomer(anyString());
        verify(financialProductsServiceClient, never()).getFinancialProducts(anyString());
    }

    @Test
    @DisplayName("Should fail when customer service fails")
    void shouldFailWhenCustomerServiceFails() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.error(new RuntimeException("Customer not found")));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(testProducts));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Failed to retrieve complete customer information")
                )
                .verify();

        verify(encryptionService, times(1)).decrypt(encryptedCode);
        verify(customerServiceClient, times(1)).getCustomer(decryptedCode);
        verify(financialProductsServiceClient, times(1)).getFinancialProducts(decryptedCode);
    }

    @Test
    @DisplayName("Should fail when financial products service fails")
    void shouldFailWhenFinancialProductsServiceFails() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.error(new RuntimeException("Products not found")));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Failed to retrieve complete customer information")
                )
                .verify();

        verify(encryptionService, times(1)).decrypt(encryptedCode);
        verify(customerServiceClient, times(1)).getCustomer(decryptedCode);
        verify(financialProductsServiceClient, times(1)).getFinancialProducts(decryptedCode);
    }

    @Test
    @DisplayName("Should handle empty products list")
    void shouldHandleEmptyProductsList() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(List.of())); // Lista vacía

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assert response.getCorrelationId().equals(correlationId);
                    assert response.getNombres().equals("Juan Carlos");
                    assert response.getProductos().isEmpty();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle timeout gracefully")
    void shouldHandleTimeoutGracefully() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer).delayElement(java.time.Duration.ofSeconds(15))); // Simular timeout
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(testProducts));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectTimeout(java.time.Duration.ofSeconds(11)) // El timeout está configurado en 10s
                .verify();
    }

    @Test
    @DisplayName("Should handle null correlation ID")
    void shouldHandleNullCorrelationId() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(testProducts));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, null);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assert response.getNombres().equals("Juan Carlos");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent calls properly")
    void shouldHandleConcurrentCallsProperly() {
        // Given
        when(encryptionService.decrypt(encryptedCode))
                .thenReturn(decryptedCode);
        when(customerServiceClient.getCustomer(decryptedCode))
                .thenReturn(Mono.just(testCustomer).delayElement(java.time.Duration.ofMillis(100)));
        when(financialProductsServiceClient.getFinancialProducts(decryptedCode))
                .thenReturn(Mono.just(testProducts).delayElement(java.time.Duration.ofMillis(200)));

        // When
        Mono<CustomerInfoResponse> result = orchestrationService
                .getCustomerCompleteInfo(encryptedCode, correlationId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        "Juan Carlos".equals(response.getNombres()) &&
                                response.getProductos().size() == 2
                )
                .verifyComplete();

        // Verificar que ambas llamadas se hicieron
        verify(customerServiceClient, times(1)).getCustomer(decryptedCode);
        verify(financialProductsServiceClient, times(1)).getFinancialProducts(decryptedCode);
    }
}