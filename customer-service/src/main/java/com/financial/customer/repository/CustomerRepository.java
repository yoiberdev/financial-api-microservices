package com.financial.customer.repository;

import com.financial.customer.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    /**
     * Busca un cliente por su código único
     * @param codigoUnico código único del cliente
     * @return Mono con el cliente encontrado o vacío si no existe
     */
    Mono<Customer> findByCodigoUnico(String codigoUnico);

    /**
     * Verifica si existe un cliente con el código único dado
     * @param codigoUnico código único del cliente
     * @return Mono con true si existe, false si no
     */
    Mono<Boolean> existsByCodigoUnico(String codigoUnico);

    /**
     * Busca un cliente por número de documento
     * @param numeroDocumento número de documento del cliente
     * @return Mono con el cliente encontrado o vacío si no existe
     */
    Mono<Customer> findByNumeroDocumento(String numeroDocumento);
}
