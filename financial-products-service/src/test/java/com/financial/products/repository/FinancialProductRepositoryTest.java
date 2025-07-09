package com.financial.products.repository;

import com.financial.products.entity.FinancialProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@DataR2dbcTest
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
class FinancialProductRepositoryTest {

    @Autowired
    private FinancialProductRepository financialProductRepository;

    private FinancialProduct cuentaAhorro;
    private FinancialProduct tarjetaCredito;

    @BeforeEach
    void setUp() {
        cuentaAhorro = FinancialProduct.builder()
                .codigoUnico("CUST001")
                .tipoProducto(FinancialProduct.TipoProducto.CUENTA_AHORRO)
                .nombre("Cuenta Ahorro Test")
                .saldo(new BigDecimal("5000.00"))
                .numeroCuenta("001-001-000000001")
                .estado(FinancialProduct.EstadoProducto.ACTIVO)
                .fechaApertura(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tarjetaCredito = FinancialProduct.builder()
                .codigoUnico("TEST001")
                .tipoProducto(FinancialProduct.TipoProducto.TARJETA_CREDITO)
                .nombre("Tarjeta Crédito Test")
                .saldo(new BigDecimal("-1500.00"))
                .numeroCuenta("5555-1111-2222-3333")
                .estado(FinancialProduct.EstadoProducto.ACTIVO)
                .fechaApertura(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should find products by codigo unico")
    void shouldFindProductsByCodigoUnico() {
        // Given
        Flux<FinancialProduct> saveProducts = financialProductRepository
                .saveAll(Flux.just(cuentaAhorro, tarjetaCredito));

        // When - Then
        saveProducts
                .then()
                .thenMany(financialProductRepository.findByCodigoUnico("TEST001"))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when codigo unico not found")
    void shouldReturnEmptyWhenCodigoUnicoNotFound() {
        financialProductRepository.findByCodigoUnico("NONEXISTENT")
                .as(StepVerifier::create)
                .verifyComplete();
    }
}
