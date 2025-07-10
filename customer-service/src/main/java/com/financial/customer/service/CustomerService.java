package com.financial.customer.service;

import com.financial.common.annotation.Loggable;
import com.financial.customer.dto.CustomerResponseDTO;
import com.financial.customer.entity.Customer;
import com.financial.customer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio para gestión de clientes
 */
@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Loggable(value = "Buscar cliente por código", includeArgs = true, includeResult = true)
    public Mono<CustomerResponseDTO> getCustomerByCodigoUnico(String codigoUnico) {
        log.info("Retrieving customer by codigo unico: {}", codigoUnico);

        return customerRepository.findByCodigoUnico(codigoUnico)
                .map(this::mapToDTO)
                .doOnNext(customer -> log.info("Customer found: {} {}",
                        customer.getNombres(), customer.getApellidos()))
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")));
    }

    private CustomerResponseDTO mapToDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .nombres(customer.getNombres())
                .apellidos(customer.getApellidos())
                .tipoDocumento(customer.getTipoDocumento())
                .numeroDocumento(customer.getNumeroDocumento())
                .build();
    }
}