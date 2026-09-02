package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Test unitaire pur : UserDtoMapperImpl est généré par MapStruct (mapping de
 * champs uniquement), sans dépendance externe. Aucun mock nécessaire : on
 * instancie directement l'implémentation générée.
 */
class UserDtoMapperTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "jdoe";
    private static final String PASSWORD = "secret";

    private UserDtoMapper userDtoMapper;

    @BeforeEach
    void setUp() {
        userDtoMapper = new UserDtoMapperImpl();
    }

    // TU-01 : UserDtoMapper.toEntity - cas nominal
    @Test
    void toEntity_shouldMapAllFieldsFromRegisterDTO() {
        // GIVEN
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName(FIRST_NAME);
        registerDTO.setLastName(LAST_NAME);
        registerDTO.setLogin(LOGIN);
        registerDTO.setPassword(PASSWORD);

        // WHEN
        User user = userDtoMapper.toEntity(registerDTO);

        // THEN
        assertThat(user.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(user.getLastName()).isEqualTo(LAST_NAME);
        assertThat(user.getLogin()).isEqualTo(LOGIN);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getId()).isNull();
    }
}