package com.financial.bff;

import com.financial.bff.config.TestMockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMockConfig.class)
@TestPropertySource(properties = {
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/auth/realms/test",
		"services.customer.base-url=http://localhost:8081",
		"services.financial-products.base-url=http://localhost:8082",
		"encryption.aes.secret-key=TestSecretKey123"
})
class  BffServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void applicationStartsWithTestProfile() {
	}
}
