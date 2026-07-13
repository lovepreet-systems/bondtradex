package com.bondtradex.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthServiceApplicationTests {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void generatePassword() {
		System.out.println(passwordEncoder.encode("admin123"));
	}


}
