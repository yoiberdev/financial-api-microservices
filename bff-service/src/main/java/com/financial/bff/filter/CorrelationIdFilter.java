package com.financial.bff.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * Filtro para manejo de Correlation ID en todas las requests
 * Asegura trazabilidad distribuida entre microservicios
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "Correlation-ID";
    private static final String CORRELATION_ID_CONTEXT_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = getOrGenerateCorrelationId(exchange);

        log.debug("Processing request with correlation-id: {}", correlationId);

        // Agregar correlation ID al response header
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        // El correlation ID generado aqui tambien se inyecta en la peticion: sin esto el
        // @RequestHeader("Correlation-ID") del controlador llegaba vacio y generaba OTRO UUID,
        // de modo que la cabecera de respuesta y el correlationId del cuerpo no coincidian.
        ServerWebExchange mutated = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId)))
                .build();

        // Propagar correlation ID en el contexto de Reactor
        return chain.filter(mutated)
                .contextWrite(Context.of(CORRELATION_ID_CONTEXT_KEY, correlationId));
    }

    private String getOrGenerateCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation-id: {}", correlationId);
        } else {
            log.debug("Using existing correlation-id: {}", correlationId);
        }

        return correlationId;
    }
}