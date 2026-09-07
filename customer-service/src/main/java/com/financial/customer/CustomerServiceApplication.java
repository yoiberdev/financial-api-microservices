package com.financial.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Se escanea tambien com.financial.common para que los aspectos de AOP (@Loggable,
 * PerformanceAspect) se registren de verdad en este servicio: sin ello las anotaciones
 * @Loggable del controlador y del servicio no hacian nada.
 */
@SpringBootApplication(scanBasePackages = {
		"com.financial.customer",
		"com.financial.common"
})
public class CustomerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

}
