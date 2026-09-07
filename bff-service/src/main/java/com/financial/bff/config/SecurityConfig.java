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
     * Configuracion de seguridad para desarrollo y para la demo publica - Sin autenticacion.
     *
     * El perfil "demo" se anade aqui a proposito: si un perfil activo no tiene ninguna
     * SecurityWebFilterChain declarada, Spring Security aplica su cadena por defecto y toda la
     * aplicacion queda detras de un basic auth con contrasena generada en el arranque.
     */
    @Bean
    @Profile({"dev", "default", "docker", "demo"})
    public SecurityWebFilterChain devSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .build();
    }

    /**
     * Configuracion de seguridad para produccion - Con OAuth2.
     * Requiere un emisor OIDC accesible; las propiedades viven en application-prod.yml.
     */
    @Bean
    @Profile("prod")
    public SecurityWebFilterChain prodSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        .pathMatchers("/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .pathMatchers("/api/customer-info/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))
                .build();
    }

}
