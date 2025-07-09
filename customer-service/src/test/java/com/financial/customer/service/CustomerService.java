package com.financial.customer.service;

import com.financial.customer.dto.CustomerResponseDTO;
import com.financial.customer.entity.Customer;
import com.financial.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CustomerService
 * Usando Mockito para mocks y StepVerifier para testing reactivo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .codigoUnico("CUST001")
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        expectedResponse = CustomerResponseDTO.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .build();
    }

    @Test
    @DisplayName("Should return customer when found by codigo unico")
    void shouldReturnCustomerWhenFoundByCodigoUnico() {
        // Given
        when(customerRepository.findByCodigoUnico("CUST001"))
                .thenReturn(Mono.just(testCustomer));

        // When
        Mono<CustomerResponseDTO> result = customerService.getCustomerByCodigoUnico("CUST001");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getNombres().equals("Juan Carlos") &&
                                response.getApellidos().equals("Pérez García") &&
                                response.getTipoDocumento().equals("DNI") &&
                                response.getNumeroDocumento().equals("12345678")
                )
                .verifyComplete();

        verify(customerRepository, times(1)).findByCodigoUnico("CUST001");
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerRepository.findByCodigoUnico("NONEXISTENT"))
                .thenReturn(Mono.empty());

        // When
        Mono<CustomerResponseDTO> result = customerService.getCustomerByCodigoUnico("NONEXISTENT");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Customer not found")
                )
                .verify();

        verify(customerRepository, times(1)).findByCodigoUnico("NONEXISTENT");
    }

    @Test
    @DisplayName("Should handle repository error gracefully")
    void shouldHandleRepositoryErrorGracefully() {
        // Given
        when(customerRepository.findByCodigoUnico(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Database connection error")));

        // When
        Mono<CustomerResponseDTO> result = customerService.getCustomerByCodigoUnico("CUST001");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database connection error")
                )
                .verify();

        verify(customerRepository, times(1)).findByCodigoUnico("CUST001");
    }

    @Test
    @DisplayName("Should map customer entity to DTO correctly")
    void shouldMapCustomerEntityToDTOCorrectly() {
        // Given
        Customer customerWithSpecialCharacters = Customer.builder()
                .codigoUnico("CUST002")
                .nombres("María José")
                .apellidos("López-Hernández")
                .tipoDocumento("CE")
                .numeroDocumento("87654321")
                .build();

        when(customerRepository.findByCodigoUnico("CUST002"))
                .thenReturn(Mono.just(customerWithSpecialCharacters));

        // When
        Mono<CustomerResponseDTO> result = customerService.getCustomerByCodigoUnico("CUST002");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getNombres().equals("María José") &&
                                response.getApellidos().equals("López-Hernández") &&
                                response.getTipoDocumento().equals("CE") &&
                                response.getNumeroDocumento().equals("87654321")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null customer fields gracefully")
    void shouldHandleNullCustomerFieldsGracefully() {
        // Given - Customer with some null fields
        Customer incompleteCustomer = Customer.builder()
                .codigoUnico("CUST003")
                .nombres("Pedro")
                .apellidos("Sánchez")
                .tipoDocumento(null) // null field
                .numeroDocumento("11111111")
                .build();

        when(customerRepository.findByCodigoUnico("CUST003"))
                .thenReturn(Mono.just(incompleteCustomer));

        // When
        Mono<CustomerResponseDTO> result = customerService.getCustomerByCodigoUnico("CUST003");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getNombres().equals("Pedro") &&
                                response.getApellidos().equals("Sánchez") &&
                                response.getTipoDocumento() == null &&
                                response.getNumeroDocumento().equals("11111111")
                )
                .verifyComplete();
    }
}