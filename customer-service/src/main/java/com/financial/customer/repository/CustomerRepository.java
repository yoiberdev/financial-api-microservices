package com.financial.customer.repository;

import com.financial.customer.entity.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends R2dbcRepository<Customer, Long> {

    Mono<Customer> findByCodigoUnico(String codigoUnico);

    // 🔥 AGREGAR estos métodos que usa tu test:
    Mono<Boolean> existsByCodigoUnico(String codigoUnico);

    Mono<Customer> findByNumeroDocumento(String numeroDocumento);

    Mono<Boolean> existsByNumeroDocumento(String numeroDocumento);
}