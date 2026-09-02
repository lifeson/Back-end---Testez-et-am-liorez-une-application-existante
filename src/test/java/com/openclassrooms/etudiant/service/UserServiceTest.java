package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/* [P2.4.E1] Analyse du code de test
 * Test unitaires.
 */

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String RAW_PASSWORD = "secret";
    private static final String ENCODED_PASSWORD = "encoded_secret";
    private static final String JWT_TOKEN = "un.jwt.token";

    /* [P2.4.E1] Analyse du code de test
     * Isolation des dépendances:
     * UserRepository et PasswordEncoder sont mockées (Mockito),
     * et injectées dans une vraie instance de UserService.
    * */
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @Test
    public void test_create_null_user_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        /* [P2.4.E1] Analyse du code de test
         * Test des cas d'erreur par assertion d'exception.
         */
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(null));
    }

    /* [P2.4.E1] Analyse du code de test
     * Stubbing du comportement des mocks** avec when(...).thenReturn(...)
     */
    @Test
    public void test_create_already_exist_user_throws_IllegalArgumentException() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(user));
    }

    @Test
    public void test_create_user() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());

        // WHEN
        userService.register(user);

        // THEN
        /* [P2.4.E1] Analyse du code de test
         * Vérification du comportement via ArgumentCaptor + verify() dans le cas nominal.
         * Le test capture l'objet réellement passé à userRepository.save(...) et vérifie
         * qu'il correspond à l'utilisateur attendu.
         */
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(user);
    }

    // TU-13 : UserService.register - cas nominal (sans vérification d'effet de bord)
    @Test
    void register_shouldEncodeRawPasswordBeforeSaving() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(RAW_PASSWORD);
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // WHEN
        userService.register(user);

        // THEN
        // Sortie observée sur l'objet lui-même (pas de verify() sur le mock) :
        // le mot de passe a bien été remplacé par sa version encodée.
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    // TU-14 : UserService.login - cas nominal
    @Test
    void login_shouldReturnJwtTokenForValidCredentials() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(ENCODED_PASSWORD);
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn(JWT_TOKEN);

        // WHEN
        String token = userService.login(LOGIN, RAW_PASSWORD);

        // THEN
        assertThat(token).isEqualTo(JWT_TOKEN);
    }
}