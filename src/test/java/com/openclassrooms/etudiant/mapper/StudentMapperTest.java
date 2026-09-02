package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.StudentDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Tests unitaires purs : StudentMapperImpl est généré par MapStruct (mapping de
 * champs uniquement), sans dépendance externe. Aucun mock nécessaire.
 */
class StudentMapperTest {

    private static final String FIRST_NAME = "Alice";
    private static final String LAST_NAME = "Martin";
    private static final String EMAIL = "alice@mail.com";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(2000, 1, 15);
    private static final String PHONE_NUMBER = "0600000000";

    private StudentMapper studentMapper;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapperImpl();
    }

    private StudentDTO buildStudentDTO() {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName(FIRST_NAME);
        studentDTO.setLastName(LAST_NAME);
        studentDTO.setEmail(EMAIL);
        studentDTO.setDateOfBirth(DATE_OF_BIRTH);
        studentDTO.setPhoneNumber(PHONE_NUMBER);
        return studentDTO;
    }

    // TU-02 : StudentMapper.toEntity - cas nominal
    @Test
    void toEntity_shouldMapAllFieldsFromStudentDTO() {
        // GIVEN
        StudentDTO studentDTO = buildStudentDTO();

        // WHEN
        Student student = studentMapper.toEntity(studentDTO);

        // THEN
        assertThat(student.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(student.getLastName()).isEqualTo(LAST_NAME);
        assertThat(student.getEmail()).isEqualTo(EMAIL);
        assertThat(student.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
        assertThat(student.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(student.getId()).isNull();
        assertThat(student.getCreatedAt()).isNull();
        assertThat(student.getUpdatedAt()).isNull();
    }

    // TU-03 : StudentMapper.toResponseDTO - cas nominal
    @Test
    void toResponseDTO_shouldMapAllFieldsFromStudent() {
        // GIVEN
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 11, 0);
        Student student = Student.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .dateOfBirth(DATE_OF_BIRTH)
                .phoneNumber(PHONE_NUMBER)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        StudentResponseDTO responseDTO = studentMapper.toResponseDTO(student);

        // THEN
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(responseDTO.getLastName()).isEqualTo(LAST_NAME);
        assertThat(responseDTO.getEmail()).isEqualTo(EMAIL);
        assertThat(responseDTO.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
        assertThat(responseDTO.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(responseDTO.getCreatedAt()).isEqualTo(createdAt);
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(updatedAt);
    }

    // TU-04 : StudentMapper.toResponseDTOList - cas nominal
    @Test
    void toResponseDTOList_shouldMapEachStudentInOrder() {
        // GIVEN
        Student student1 = Student.builder().id(1L).firstName("Alice").lastName("Martin").email("alice@mail.com").build();
        Student student2 = Student.builder().id(2L).firstName("Bob").lastName("Durand").email("bob@mail.com").build();

        // WHEN
        List<StudentResponseDTO> responseDTOs = studentMapper.toResponseDTOList(List.of(student1, student2));

        // THEN
        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(0).getFirstName()).isEqualTo("Alice");
        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getFirstName()).isEqualTo("Bob");
    }

    // TU-05 : StudentMapper.updateEntityFromDto - cas nominal
    @Test
    void updateEntityFromDto_shouldUpdateEditableFieldsAndPreserveTheRest() {
        // GIVEN
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Student existingStudent = Student.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .dateOfBirth(DATE_OF_BIRTH)
                .phoneNumber(PHONE_NUMBER)
                .createdAt(createdAt)
                .build();

        StudentDTO updatedDTO = buildStudentDTO();
        updatedDTO.setFirstName("Alicia");

        // WHEN
        studentMapper.updateEntityFromDto(updatedDTO, existingStudent);

        // THEN
        assertThat(existingStudent.getFirstName()).isEqualTo("Alicia");
        assertThat(existingStudent.getLastName()).isEqualTo(LAST_NAME);
        assertThat(existingStudent.getId()).isEqualTo(1L);
        assertThat(existingStudent.getCreatedAt()).isEqualTo(createdAt);
    }
}