package com.financial.products.repository;

import com.financial.products.entity.FinancialProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface FinancialProductRepository extends ReactiveCrudRepository<FinancialProduct, Long> {

    /**
     * Busca todos los productos financieros de un cliente por su código único
     */
    Flux<FinancialProduct> findByCodigoUnico(String codigoUnico);
}
