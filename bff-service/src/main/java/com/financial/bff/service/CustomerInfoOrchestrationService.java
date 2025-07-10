package com.financial.bff.service;

import com.financial.bff.client.CustomerServiceClient;
import com.financial.bff.client.FinancialProductsServiceClient;
import com.financial.bff.dto.CustomerDTO;
import com.financial.bff.dto.CustomerInfoResponse;
import com.financial.bff.dto.FinancialProductDTO;
import com.financial.common.annotation.Loggable;
import com.financial.common.service.impl.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Servicio principal de orquestación que coordina las llamadas a los microservicios
 * Aplica el patrón BFF (Backend for Frontend) para agregar datos de múltiples fuentes
 */
@Service
@Slf4j
public class CustomerInfoOrchestrationService {

    private final EncryptionService encryptionService;
    private final CustomerServiceClient customerServiceClient;
    private final FinancialProductsServiceClient financialProductsServiceClient;

    public CustomerInfoOrchestrationService(
            EncryptionService encryptionService,
            CustomerServiceClient customerServiceClient,
            FinancialProductsServiceClient financialProductsServiceClient) {
        this.encryptionService = encryptionService;
        this.customerServiceClient = customerServiceClient;
        this.financialProductsServiceClient = financialProductsServiceClient;
    }

    /**
     * Orquesta la obtención completa de información del cliente
     * Desencripta el código único y realiza llamadas paralelas a ambos microservicios
     */
    @Loggable(value = "Orquestación de info cliente BFF", includeArgs = true, includeResult = false)
    public Mono<CustomerInfoResponse> getCustomerCompleteInfo(String encryptedCodigoUnico, String correlationId) {
        return Mono.deferContextual(ctx -> {
            log.info("Starting customer info orchestration with correlation-id: {}", correlationId);

            return decryptCodigoUnico(encryptedCodigoUnico, correlationId)
                    .flatMap(codigoUnico -> orchestrateServiceCalls(codigoUnico, correlationId))
                    .contextWrite(context -> {
                        if (correlationId != null) {
                            return context.put("correlationId", correlationId);
                        }
                        return context;
                    })
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Customer info orchestration completed successfully - {}", correlationId))
                    .doOnError(error -> log.error("Customer info orchestration failed: {} - {}", error.getMessage(), correlationId));
        });
    }

    /**
     * Desencripta el código único del cliente
     */
    private Mono<String> decryptCodigoUnico(String encryptedCodigoUnico, String correlationId) {
        return Mono.fromCallable(() -> {
                    log.debug("Decrypting codigo unico - {}", correlationId);
                    return encryptionService.decrypt(encryptedCodigoUnico);
                })
                .doOnSuccess(decrypted -> log.debug("Codigo unico decrypted successfully - {}", correlationId))
                .onErrorMap(error -> {
                    log.error("Failed to decrypt codigo unico: {} - {}", error.getMessage(), correlationId);
                    return new RuntimeException("Invalid encrypted codigo unico");
                });
    }

    /**
     * Orquesta las llamadas paralelas a ambos microservicios
     * Utiliza Mono.zip para ejecutar ambas llamadas de forma concurrente
     */
    private Mono<CustomerInfoResponse> orchestrateServiceCalls(String codigoUnico, String correlationId) {
        log.info("Orchestrating parallel calls for codigo: {} - {}", codigoUnico, correlationId);

        // Llamadas paralelas a ambos microservicios
        Mono<CustomerDTO> customerMono = customerServiceClient.getCustomer(codigoUnico)
                .doOnSubscribe(sub -> log.debug("Starting customer service call - {}", correlationId));

        Mono<List<FinancialProductDTO>> productsMono = financialProductsServiceClient.getFinancialProducts(codigoUnico)
                .doOnSubscribe(sub -> log.debug("Starting financial products service call - {}", correlationId));

        // Combinar resultados de ambos servicios
        return Mono.zip(customerMono, productsMono)
                .map(tuple -> {
                    CustomerDTO customer = tuple.getT1();
                    List<FinancialProductDTO> products = tuple.getT2();

                    log.info("Successfully retrieved data from both services - {}", correlationId);
                    log.debug("Customer: {} {}, Products count: {} - {}",
                            customer.getNombres(), customer.getApellidos(), products.size(), correlationId);

                    return CustomerInfoResponse.builder()
                            .correlationId(correlationId)
                            .nombres(customer.getNombres())
                            .apellidos(customer.getApellidos())
                            .tipoDocumento(customer.getTipoDocumento())
                            .numeroDocumento(customer.getNumeroDocumento())
                            .productos(products)
                            .build();
                })
                .onErrorMap(error -> {
                    log.error("Error during service orchestration: {} - {}", error.getMessage(), correlationId);
                    return new RuntimeException("Failed to retrieve complete customer information");
                });
    }
}