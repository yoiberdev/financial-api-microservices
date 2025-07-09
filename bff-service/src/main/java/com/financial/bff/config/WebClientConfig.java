package com.financial.bff.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuración de WebClient para llamadas a microservicios
 * Incluye configuración de timeouts, logging y manejo de errores
 */
@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configurar HttpClient con timeouts
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .followRedirect(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter());
    }

    /**
     * Filtro para logging de requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Outgoing Request: {} {}",
                    clientRequest.method(), clientRequest.url());

            clientRequest.headers().forEach((name, values) -> {
                if (!name.toLowerCase().contains("authorization")) {
                    log.debug("Request Header: {}={}", name, values);
                }
            });

            return Mono.just(clientRequest);
        });
    }

    /**
     * Filtro para logging de responses
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response Status: {}", clientResponse.statusCode());

            clientResponse.headers().asHttpHeaders().forEach((name, values) -> {
                log.debug("Response Header: {}={}", name, values);
            });

            return Mono.just(clientResponse);
        });
    }

    /**
     * Filtro para manejo centralizado de errores
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                log.warn("Client error received: {}", clientResponse.statusCode());
            } else if (clientResponse.statusCode().is5xxServerError()) {
                log.error("Server error received: {}", clientResponse.statusCode());
            }

            return Mono.just(clientResponse);
        });
    }
}