package com.financial.products.repository;

import com.financial.products.entity.FinancialProduct;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FinancialProductRepository extends R2dbcRepository<FinancialProduct, Long> {

    Flux<FinancialProduct> findByCodigoUnico(String codigoUnico);
}