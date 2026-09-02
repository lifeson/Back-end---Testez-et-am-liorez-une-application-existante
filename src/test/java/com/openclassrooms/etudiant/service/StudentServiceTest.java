package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/*
 * Tests unitaires avec mocks : StudentRepository est mocké (Mockito), et
 * injecté dans une vraie instance de StudentService. Cas nominaux uniquement,
 * sorties vérifiées sur la valeur de retour (pas de verify() sur le mock).
 */
@ExtendWith(SpringExtension.class)
class StudentServiceTest {

    private static final String FIRST_NAME = "Alice";
    private static final String LAST_NAME = "Martin";
    private static final String EMAIL = "alice@mail.com";

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;

    // TU-09 : StudentService.create - cas nominal
    @Test
    void create_shouldReturnStudentSavedByRepository() {
        // GIVEN
        Student studentToCreate = Student.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();
        Student savedStudent = Student.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();
        when(studentRepository.save(studentToCreate)).thenReturn(savedStudent);

        // WHEN
        Student result = studentService.create(studentToCreate);

        // THEN
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
    }

    // TU-10 : StudentService.findAll - cas nominal
    @Test
    void findAll_shouldReturnAllStudentsFromRepository() {
        // GIVEN
        Student student1 = Student.builder().id(1L).firstName("Alice").build();
        Student student2 = Student.builder().id(2L).firstName("Bob").build();
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));

        // WHEN
        List<Student> result = studentService.findAll();

        // THEN
        assertThat(result).containsExactly(student1, student2);
    }

    // TU-11 : StudentService.findById - cas nominal
    @Test
    void findById_shouldReturnMatchingStudent() {
        // GIVEN
        Student student = Student.builder().id(1L).firstName(FIRST_NAME).build();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // WHEN
        Student result = studentService.findById(1L);

        // THEN
        assertThat(result).isEqualTo(student);
    }

    // TU-12 : StudentService.update - cas nominal
    @Test
    void update_shouldApplyChangesAndReturnUpdatedStudent() {
        // GIVEN
        Student existingStudent = Student.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();
        Student updatedFields = Student.builder()
                .firstName("Alicia")
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(existingStudent)).thenReturn(existingStudent);

        // WHEN
        Student result = studentService.update(1L, updatedFields);

        // THEN
        assertThat(result.getFirstName()).isEqualTo("Alicia");
    }
}