package com.financial.bff.handler;

import com.financial.bff.dto.CustomerInfoResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Manejador global para excepciones controladas en CustomerInfoController
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return CustomerInfoResponse.builder()
                .nombres("ERROR")
                .apellidos("INTERNAL_ERROR")
                .tipoDocumento(ex.getMessage())
                .numeroDocumento("500 INTERNAL_SERVER_ERROR")
                .productos(null)
                .build();
    }
}
