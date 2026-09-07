package com.financial.bff.handler;

import com.financial.bff.dto.CustomerInfoResponse;
import com.financial.bff.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

/**
 * Manejador global para excepciones controladas en CustomerInfoController
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Respeta el codigo de estado original.
     *
     * ResponseStatusException hereda de RuntimeException, asi que el handler generico de mas
     * abajo la capturaba y convertia CUALQUIER 404 / 405 / 415 del BFF en un 500. Una ruta de
     * API inexistente respondia "500 INTERNAL_SERVER_ERROR" en lugar de 404.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                              ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.debug("Peticion no resuelta: {} {} -> {}",
                exchange.getRequest().getMethod(), exchange.getRequest().getPath(), status);

        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .error(status.name())
                .message(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .correlationId(exchange.getResponse().getHeaders().getFirst("Correlation-ID"))
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomerInfoResponse handleConstraintViolation(ConstraintViolationException ex) {
        return CustomerInfoResponse.builder()
                .nombres("ERROR")
                .apellidos("INVALID_REQUEST")
                .tipoDocumento(ex.getMessage())
                .numeroDocumento("400 BAD_REQUEST")
                .productos(null)
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomerInfoResponse handleIllegalArgument(IllegalArgumentException ex) {
        return CustomerInfoResponse.builder()
                .nombres("ERROR")
                .apellidos("INVALID_REQUEST")
                .tipoDocumento(ex.getMessage())
                .numeroDocumento("400 BAD_REQUEST")
                .productos(null)
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomerInfoResponse handleRuntimeException(RuntimeException ex) {
        log.error("Error no controlado en el BFF: {}", ex.getMessage(), ex);
        return CustomerInfoResponse.builder()
                .nombres("ERROR")
                .apellidos("INTERNAL_ERROR")
                .tipoDocumento(ex.getMessage())
                .numeroDocumento("500 INTERNAL_SERVER_ERROR")
                .productos(null)
                .build();
    }
}
