package com.financial.products.service;

import com.financial.products.dto.FinancialProductResponseDTO;
import com.financial.products.entity.FinancialProduct;
import com.financial.products.repository.FinancialProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio para gestión de productos financieros
 */
@Service
@Slf4j
public class FinancialProductService {

    private final FinancialProductRepository productRepository;

    public FinancialProductService(FinancialProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<List<FinancialProductResponseDTO>> getProductsByCustomer(String codigoUnico) {
        log.info("Retrieving financial products for customer: {}", codigoUnico);

        return productRepository.findByCodigoUnico(codigoUnico)
                .map(this::mapToDTO)
                .collectList()
                .doOnNext(products -> log.info("Found {} products for customer: {}",
                        products.size(), codigoUnico))
                .filter(products -> !products.isEmpty())
                .switchIfEmpty(Mono.error(new RuntimeException("No financial products found for customer")));
    }

    private FinancialProductResponseDTO mapToDTO(FinancialProduct product) {
        return FinancialProductResponseDTO.builder()
                .tipoProducto(product.getTipoProducto().getDescripcion())
                .nombre(product.getNombre())
                .saldo(product.getSaldo())
                .build();
    }
}