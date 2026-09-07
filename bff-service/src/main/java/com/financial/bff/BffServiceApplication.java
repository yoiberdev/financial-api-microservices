package com.financial.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * El escaneo de com.financial.common se declara con scanBasePackages y NO con un
 * {@code @ComponentScan} aparte: un {@code @ComponentScan} propio pierde los excludeFilters de
 * {@code @SpringBootApplication} (TypeExcludeFilter), asi que los tests de slice como
 * {@code @WebFluxTest} acababan levantando todos los @Service del proyecto.
 */
@SpringBootApplication(scanBasePackages = {
		"com.financial.bff",
		"com.financial.common"
})
public class BffServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffServiceApplication.class, args);
	}
}
