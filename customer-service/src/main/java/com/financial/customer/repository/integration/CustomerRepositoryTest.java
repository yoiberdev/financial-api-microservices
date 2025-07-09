package com.financial.customer.repository.integration;

import com.financial.customer.entity.Customer;
import com.financial.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataR2dbcTest
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll().block();
        testCustomer = Customer.builder()
                .codigoUnico("TEST001")
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save and find customer by codigo unico")
    void shouldSaveAndFindCustomerByCodigoUnico() {
        // Given - When - Then
        customerRepository.save(testCustomer)
                .then(customerRepository.findByCodigoUnico("TEST001"))
                .as(StepVerifier::create)
                .expectNextMatches(customer ->
                        customer.getCodigoUnico().equals("TEST001") &&
                                customer.getNombres().equals("Juan Carlos") &&
                                customer.getApellidos().equals("Pérez García")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when customer not found")
    void shouldReturnEmptyWhenCustomerNotFound() {
        customerRepository.findByCodigoUnico("NONEXISTENT")
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if customer exists by codigo unico")
    void shouldCheckIfCustomerExistsByCodigoUnico() {
        // Save customer first
        customerRepository.save(testCustomer)
                .then(customerRepository.existsByCodigoUnico("TEST001"))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find customer by numero documento")
    void shouldFindCustomerByNumeroDocumento() {
        customerRepository.save(testCustomer)
                .then(customerRepository.findByNumeroDocumento("12345678"))
                .as(StepVerifier::create)
                .expectNextMatches(customer ->
                        customer.getNumeroDocumento().equals("12345678") &&
                                customer.getCodigoUnico().equals("TEST001")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when customer does not exist")
    void shouldReturnFalseWhenCustomerDoesNotExist() {
        customerRepository.existsByCodigoUnico("NONEXISTENT")
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }
}