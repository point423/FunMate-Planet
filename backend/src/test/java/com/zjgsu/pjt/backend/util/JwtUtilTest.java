package com.zjgsu.pjt.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void generateToken_CanBeParsedAndValidated() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY_STR", "FunMatePlanetSecretKeyForJwtTokenGeneration2024");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", 60);
        jwtUtil.init();

        String token = jwtUtil.generateToken(42L);

        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_ReturnsFalseForInvalidToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY_STR", "FunMatePlanetSecretKeyForJwtTokenGeneration2024");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", 60);
        jwtUtil.init();

        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void validateToken_ReturnsFalseForExpiredToken() throws InterruptedException {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY_STR", "FunMatePlanetSecretKeyForJwtTokenGeneration2024");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", -1);
        jwtUtil.init();

        String token = jwtUtil.generateToken(42L);

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }
}
