package com.openclassrooms.etudiant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Tests unitaires purs : JwtService n'a aucune dépendance vers un repository ou
 * une base de données (uniquement de la crypto via JJWT). On instancie
 * directement le service et on injecte les propriétés @Value via
 * ReflectionTestUtils, pour rester sans contexte Spring.
 */
class JwtServiceTest {

    private static final String SECRET = "maSuperCleSecreteDe32CaracteresMinimum123";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String USERNAME = "jdoe";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    private UserDetails buildUserDetails() {
        return User.withUsername(USERNAME)
                .password("irrelevant")
                .authorities(List.of())
                .build();
    }

    // TU-06 : JwtService.generateToken - cas nominal
    @Test
    void generateToken_shouldReturnNonEmptyJwtString() {
        // GIVEN
        UserDetails userDetails = buildUserDetails();

        // WHEN
        String token = jwtService.generateToken(userDetails);

        // THEN
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    // TU-07 : JwtService.extractUsername - cas nominal
    @Test
    void extractUsername_shouldReturnUsernameEncodedInToken() {
        // GIVEN
        String token = jwtService.generateToken(buildUserDetails());

        // WHEN
        String username = jwtService.extractUsername(token);

        // THEN
        assertThat(username).isEqualTo(USERNAME);
    }

    // TU-08 : JwtService.isTokenValid - cas nominal
    @Test
    void isTokenValid_shouldReturnTrueForMatchingNonExpiredToken() {
        // GIVEN
        UserDetails userDetails = buildUserDetails();
        String token = jwtService.generateToken(userDetails);

        // WHEN
        boolean valid = jwtService.isTokenValid(token, userDetails);

        // THEN
        assertThat(valid).isTrue();
    }
}