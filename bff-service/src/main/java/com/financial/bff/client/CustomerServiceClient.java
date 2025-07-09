package com.financial.bff.client;

import com.financial.bff.dto.CustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CustomerServiceClient {

    private final WebClient webClient;

    public CustomerServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.customer.base-url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<CustomerDTO> getCustomer(String codigoUnico) {
        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.getOrDefault("correlationId", "unknown");

            log.info("Calling Customer Service for codigo: {} with correlation-id: {}",
                    codigoUnico, correlationId);

            return webClient.get()
                    .uri("/api/customers/{codigoUnico}", codigoUnico)
                    .header("Correlation-ID", correlationId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Error calling Customer Service: {} - {}",
                                        response.statusCode(), correlationId);
                                return Mono.error(new RuntimeException(
                                        "Customer not found or service unavailable"));
                            })
                    .bodyToMono(CustomerDTO.class)
                    .doOnSuccess(customer -> log.info("Customer retrieved successfully: {} - {}",
                            customer.getNombres(), correlationId))
                    .doOnError(error -> log.error("Error retrieving customer: {} - {}",
                            error.getMessage(), correlationId));
        });
    }
}