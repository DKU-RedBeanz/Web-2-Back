package com.redbeanz.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.redbeanz.backend.config.SecurityConfig;

@WebMvcTest(SecurityTestController.class)
@Import(SecurityConfig.class)
class PasswordEncoderTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void passwordCanBeEncodedAndMatched() {
        String rawPassword = "testPassword123!";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
}
