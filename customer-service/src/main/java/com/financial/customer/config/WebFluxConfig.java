package com.financial.customer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import jakarta.annotation.PostConstruct;

/**
 * Sin @EnableWebFlux a proposito: en una aplicacion Spring Boot esa anotacion desactiva
 * WebFluxAutoConfiguration, con lo que se pierden las propiedades spring.webflux.*, los codecs
 * configurados por Boot y el servicio de recursos estaticos.
 */
@Configuration
@Slf4j
public class WebFluxConfig implements WebFluxConfigurer {

    @PostConstruct
    public void init() {
        log.info("WebFluxConfig initialized for Customer Service");
    }
}
