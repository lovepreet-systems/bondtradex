package com.bondtradex.ioi;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceApplicationTests {

	@Test
	void generatePassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		String encodedPassword = passwordEncoder.encode("admin123");

		System.out.println(encodedPassword);

		assertThat(encodedPassword).isNotBlank();
		assertThat(passwordEncoder.matches("admin123", encodedPassword))
				.isTrue();
	}
}