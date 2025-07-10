package com.financial.bff;

import com.financial.common.config.EncryptionConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.financial.bff",
		"com.financial.common"
})
@Import(EncryptionConfiguration.class)
public class BffServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffServiceApplication.class, args);
	}
}