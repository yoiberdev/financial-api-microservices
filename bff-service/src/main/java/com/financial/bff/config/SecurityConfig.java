package com.financial.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configuración de seguridad para desarrollo - Sin autenticación
     */
    @Bean
    @Profile({"dev", "default"})
    public SecurityWebFilterChain devSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // 🔥 PERMITIR TODOS LOS ENDPOINTS PARA DESARROLLO
                        .anyExchange().permitAll())
                .build();
    }

    /**
     * Configuración de seguridad para producción - Con OAuth2
     */
    @Bean
    @Profile("prod")
    public SecurityWebFilterChain prodSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/webjars/**").permitAll()
                        .pathMatchers("/api/customer-info/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))
                .build();
    }

}