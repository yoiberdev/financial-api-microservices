package com.financial.bff.client;

import com.financial.bff.dto.FinancialProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

@Service
@Slf4j
public class FinancialProductsServiceClient {

    private final WebClient webClient;

    public FinancialProductsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.financial-products.base-url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<FinancialProductDTO>> getFinancialProducts(String codigoUnico) {
        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.getOrDefault("correlationId", "unknown");

            log.info("Calling Financial Products Service for codigo: {} with correlation-id: {}",
                    codigoUnico, correlationId);

            return webClient.get()
                    .uri("/api/financial-products/customer/{codigoUnico}", codigoUnico)
                    .header("Correlation-ID", correlationId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Error calling Financial Products Service: {} - {}",
                                        response.statusCode(), correlationId);
                                return Mono.error(new RuntimeException(
                                        "Financial products not found or service unavailable"));
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<FinancialProductDTO>>() {})
                    .doOnSuccess(products -> log.info("Financial products retrieved successfully: {} items - {}",
                            products.size(), correlationId))
                    .doOnError(error -> log.error("Error retrieving financial products: {} - {}",
                            error.getMessage(), correlationId));
        });
    }
}