package com.financial.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Se escanea tambien com.financial.common para que los aspectos de AOP (@Loggable,
 * PerformanceAspect) se registren de verdad en este servicio.
 */
@SpringBootApplication(scanBasePackages = {
		"com.financial.products",
		"com.financial.common"
})
public class FinancialProductsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialProductsServiceApplication.class, args);
	}

}
