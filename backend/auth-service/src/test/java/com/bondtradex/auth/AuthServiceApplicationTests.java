package com.bondtradex.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceApplicationTests {

	private final PasswordEncoder passwordEncoder =
			new BCryptPasswordEncoder();

	@Test
	void generatePassword() {
		String encodedPassword = passwordEncoder.encode("admin123");

		System.out.println(encodedPassword);
	}
}