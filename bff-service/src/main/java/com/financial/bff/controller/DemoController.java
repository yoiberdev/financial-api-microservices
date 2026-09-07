package com.financial.bff.controller;

import com.financial.common.service.impl.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de apoyo para la demo publica.
 *
 * Solo se registra con demo.enabled=true (perfil "demo"). Devuelve el codigo cifrado de un
 * conjunto CERRADO de clientes ficticios para que la pagina de la demo pueda llamar al endpoint
 * real del BFF sin tener que cifrar en el navegador. No cifra texto arbitrario a proposito.
 */
@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(prefix = "demo", name = "enabled", havingValue = "true")
@Tag(name = "Demo", description = "Datos de apoyo para la demo publica")
@Slf4j
public class DemoController {

    private final EncryptionService encryptionService;
    private final List<String> codigos;

    public DemoController(EncryptionService encryptionService,
                          @Value("${demo.codigos:CUST001,CUST002,CUST003,CUST004,CUST005}") List<String> codigos) {
        this.encryptionService = encryptionService;
        this.codigos = codigos;
    }

    @GetMapping("/customers")
    @Operation(summary = "Codigos de demo",
            description = "Lista los codigos unicos ficticios y su equivalente cifrado en AES (Base64 URL-safe)")
    public Mono<List<Map<String, String>>> demoCustomers() {
        return Mono.fromCallable(() -> codigos.stream()
                .map(codigo -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("codigo", codigo);
                    item.put("codigoCifrado", encryptionService.encrypt(codigo));
                    return item;
                })
                .toList());
    }
}
