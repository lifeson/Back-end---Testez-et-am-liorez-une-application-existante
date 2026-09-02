package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.JwtService;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/*
 * Tests d'intégration : StudentController est protégé par JWT (voir
 * SpringSecurityConfig). Un utilisateur est enregistré et un token généré
 * dans le @BeforeEach, puis réutilisé sur chaque requête. Cas nominaux
 * uniquement (TI-02 à TI-06).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class StudentControllerTest {

    private static final String URL = "/api/students";
    private static final String LOGIN = "jdoe";
    private static final String PASSWORD = "password";

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.0");

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private String bearerToken;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    void authenticate() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        userService.register(user);

        UserDetails userDetails = userRepository.findByLogin(LOGIN).orElseThrow();
        bearerToken = "Bearer " + jwtService.generateToken(userDetails);
    }

    @AfterEach
    void cleanUp() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private StudentDTO buildStudentDTO() {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Alice");
        studentDTO.setLastName("Martin");
        studentDTO.setEmail("alice@mail.com");
        studentDTO.setDateOfBirth(LocalDate.of(2000, 1, 15));
        studentDTO.setPhoneNumber("0600000000");
        return studentDTO;
    }

    // TI-02 : POST /api/students - cas nominal
    @Test
    void createStudent_shouldReturn201WithCreatedStudent() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .header("Authorization", bearerToken)
                        .content(objectMapper.writeValueAsString(buildStudentDTO()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Alice"));
    }

    // TI-03 : GET /api/students - cas nominal
    @Test
    void findAllStudents_shouldReturn200WithAllStudents() throws Exception {
        // GIVEN
        studentRepository.save(Student.builder().firstName("Alice").lastName("Martin").email("alice@mail.com").build());
        studentRepository.save(Student.builder().firstName("Bob").lastName("Durand").email("bob@mail.com").build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                        .header("Authorization", bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    // TI-04 : GET /api/students/{id} - cas nominal
    @Test
    void findStudentById_shouldReturn200WithMatchingStudent() throws Exception {
        // GIVEN
        Student student = studentRepository.save(
                Student.builder().firstName("Alice").lastName("Martin").email("alice@mail.com").build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/" + student.getId())
                        .header("Authorization", bearerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("alice@mail.com"));
    }

    // TI-05 : PUT /api/students/{id} - cas nominal
    @Test
    void updateStudent_shouldReturn200WithUpdatedStudent() throws Exception {
        // GIVEN
        Student student = studentRepository.save(
                Student.builder().firstName("Alice").lastName("Martin").email("alice@mail.com").build());
        StudentDTO updatedDTO = buildStudentDTO();
        updatedDTO.setFirstName("Alicia");

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/" + student.getId())
                        .header("Authorization", bearerToken)
                        .content(objectMapper.writeValueAsString(updatedDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Alicia"));
    }

    // TI-06 : DELETE /api/students/{id} - cas nominal
    @Test
    void deleteStudent_shouldReturn204() throws Exception {
        // GIVEN
        Student student = studentRepository.save(
                Student.builder().firstName("Alice").lastName("Martin").email("alice@mail.com").build());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/" + student.getId())
                        .header("Authorization", bearerToken))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}