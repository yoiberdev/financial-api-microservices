package com.financial.products.service;

import com.financial.products.dto.FinancialProductResponseDTO;
import com.financial.products.entity.FinancialProduct;
import com.financial.products.repository.FinancialProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FinancialProductService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Financial Product Service Tests")
class FinancialProductServiceTest {

    @Mock
    private FinancialProductRepository productRepository;

    @InjectMocks
    private FinancialProductService productService;

    private FinancialProduct cuentaAhorro;
    private FinancialProduct tarjetaCredito;
    private FinancialProduct depositoPlazoFijo;

    @BeforeEach
    void setUp() {
        cuentaAhorro = FinancialProduct.builder()
                .id(1L)
                .codigoUnico("CUST001")
                .tipoProducto(FinancialProduct.TipoProducto.CUENTA_AHORRO)
                .nombre("Cuenta Ahorro Básica")
                .saldo(new BigDecimal("5500.50"))
                .numeroCuenta("001-001-000000001")
                .estado(FinancialProduct.EstadoProducto.ACTIVO)
                .fechaApertura(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tarjetaCredito = FinancialProduct.builder()
                .id(2L)
                .codigoUnico("CUST001")
                .tipoProducto(FinancialProduct.TipoProducto.TARJETA_CREDITO)
                .nombre("Tarjeta Gold")
                .saldo(new BigDecimal("-1200.00"))
                .numeroCuenta("5555-1111-2222-3333")
                .estado(FinancialProduct.EstadoProducto.ACTIVO)
                .fechaApertura(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        depositoPlazoFijo = FinancialProduct.builder()
                .id(3L)
                .codigoUnico("CUST001")
                .tipoProducto(FinancialProduct.TipoProducto.DEPOSITO_PLAZO_FIJO)
                .nombre("Depósito 12 meses")
                .saldo(new BigDecimal("10000.00"))
                .numeroCuenta("001-002-000000001")
                .estado(FinancialProduct.EstadoProducto.ACTIVO)
                .fechaApertura(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return products when customer has multiple products")
    void shouldReturnProductsWhenCustomerHasMultipleProducts() {
        // Given
        when(productRepository.findByCodigoUnico("CUST001"))
                .thenReturn(Flux.just(cuentaAhorro, tarjetaCredito, depositoPlazoFijo));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST001");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 3;
                    assert products.get(0).getTipoProducto().equals("Cuenta de Ahorro");
                    assert products.get(0).getNombre().equals("Cuenta Ahorro Básica");
                    assert products.get(0).getSaldo().equals(new BigDecimal("5500.50"));

                    assert products.get(1).getTipoProducto().equals("Tarjeta de Crédito");
                    assert products.get(1).getNombre().equals("Tarjeta Gold");
                    assert products.get(1).getSaldo().equals(new BigDecimal("-1200.00"));

                    assert products.get(2).getTipoProducto().equals("Depósito a Plazo Fijo");
                    assert products.get(2).getNombre().equals("Depósito 12 meses");
                    assert products.get(2).getSaldo().equals(new BigDecimal("10000.00"));

                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findByCodigoUnico("CUST001");
    }

    @Test
    @DisplayName("Should return single product when customer has one product")
    void shouldReturnSingleProductWhenCustomerHasOneProduct() {
        // Given
        when(productRepository.findByCodigoUnico("CUST002"))
                .thenReturn(Flux.just(cuentaAhorro));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST002");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 1;
                    assert products.get(0).getTipoProducto().equals("Cuenta de Ahorro");
                    assert products.get(0).getNombre().equals("Cuenta Ahorro Básica");
                    assert products.get(0).getSaldo().equals(new BigDecimal("5500.50"));
                    return true;
                })
                .verifyComplete();

        verify(productRepository, times(1)).findByCodigoUnico("CUST002");
    }

    @Test
    @DisplayName("Should throw exception when no products found")
    void shouldThrowExceptionWhenNoProductsFound() {
        // Given
        when(productRepository.findByCodigoUnico("CUST_NO_PRODUCTS"))
                .thenReturn(Flux.empty());

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST_NO_PRODUCTS");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("No financial products found for customer")
                )
                .verify();

        verify(productRepository, times(1)).findByCodigoUnico("CUST_NO_PRODUCTS");
    }

    @Test
    @DisplayName("Should handle repository error gracefully")
    void shouldHandleRepositoryErrorGracefully() {
        // Given
        when(productRepository.findByCodigoUnico(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Database connection error")));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("ERROR_CASE");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database connection error")
                )
                .verify();

        verify(productRepository, times(1)).findByCodigoUnico("ERROR_CASE");
    }

    @Test
    @DisplayName("Should correctly map all product types")
    void shouldCorrectlyMapAllProductTypes() {
        // Given - Crear productos de diferentes tipos
        FinancialProduct cuentaCorriente = FinancialProduct.builder()
                .tipoProducto(FinancialProduct.TipoProducto.CUENTA_CORRIENTE)
                .nombre("Cuenta Corriente Empresarial")
                .saldo(new BigDecimal("2500.00"))
                .build();

        FinancialProduct prestamo = FinancialProduct.builder()
                .tipoProducto(FinancialProduct.TipoProducto.PRESTAMO)
                .nombre("Préstamo Personal")
                .saldo(new BigDecimal("-5000.00"))
                .build();

        FinancialProduct creditoHipotecario = FinancialProduct.builder()
                .tipoProducto(FinancialProduct.TipoProducto.CREDITO_HIPOTECARIO)
                .nombre("Crédito Hipotecario")
                .saldo(new BigDecimal("-120000.00"))
                .build();

        when(productRepository.findByCodigoUnico("CUST_ALL_TYPES"))
                .thenReturn(Flux.just(cuentaCorriente, prestamo, creditoHipotecario));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST_ALL_TYPES");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 3;

                    // Verificar mapeo correcto de tipos
                    assert products.get(0).getTipoProducto().equals("Cuenta Corriente");
                    assert products.get(1).getTipoProducto().equals("Préstamo");
                    assert products.get(2).getTipoProducto().equals("Crédito Hipotecario");

                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle large numbers correctly")
    void shouldHandleLargeNumbersCorrectly() {
        // Given
        FinancialProduct largeBalance = FinancialProduct.builder()
                .tipoProducto(FinancialProduct.TipoProducto.DEPOSITO_PLAZO_FIJO)
                .nombre("Depósito Grande")
                .saldo(new BigDecimal("999999999.99"))
                .build();

        when(productRepository.findByCodigoUnico("CUST_LARGE"))
                .thenReturn(Flux.just(largeBalance));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST_LARGE");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 1;
                    assert products.get(0).getSaldo().equals(new BigDecimal("999999999.99"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle zero balance correctly")
    void shouldHandleZeroBalanceCorrectly() {
        // Given
        FinancialProduct zeroBalance = FinancialProduct.builder()
                .tipoProducto(FinancialProduct.TipoProducto.TARJETA_DEBITO)
                .nombre("Tarjeta Débito Nueva")
                .saldo(BigDecimal.ZERO)
                .build();

        when(productRepository.findByCodigoUnico("CUST_ZERO"))
                .thenReturn(Flux.just(zeroBalance));

        // When
        Mono<List<FinancialProductResponseDTO>> result =
                productService.getProductsByCustomer("CUST_ZERO");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(products -> {
                    assert products.size() == 1;
                    assert products.get(0).getSaldo().equals(BigDecimal.ZERO);
                    assert products.get(0).getTipoProducto().equals("Tarjeta de Débito");
                    return true;
                })
                .verifyComplete();
    }
}