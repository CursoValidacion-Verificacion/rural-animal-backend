package com.project.demo.api.user;

import com.project.demo.api.data.UserTestData;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.EmailService;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Pruebas funcionales de API para el endpoint /users.
 *
 * Usa @SpringBootTest con servidor real en puerto aleatorio y MariaDB/Redis via Docker.
 * EmailService se mockea para evitar envios reales de correo.
 * Cubre creacion, actualizacion parcial, filtrado y escenarios de error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API Users - Pruebas funcionales REST Assured")
@Tag("api")
class UserApiTest {

    @LocalServerPort
    private int port;

    @MockBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private TblDirectionRepository directionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TblUser testUser;
    private TblRole buyerRole;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        buyerRole = roleRepository.findByTitle(RoleEnum.BUYER)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.BUYER);
                    return roleRepository.save(r);
                });

        testUser = new TblUser();
        testUser.setName("Carlos");
        testUser.setLastName1("Rodriguez");
        testUser.setEmail("carlos.user.test"+ System.nanoTime() + "@ruraltest.com");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
        testUser.setIdentification("112345678" + System.nanoTime());
        testUser.setPhoneNumber("88001122");
        testUser.setBirthDate(LocalDate.of(1995, 3, 20));
        testUser.setRole(buyerRole);
        testUser = userRepository.save(testUser);
    }



    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-USER-01: Crear usuario con datos validos debe retornar HTTP 200.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-01: Crear usuario con datos validos debe retornar 200 y el usuario creado")
    @DisplayName("TC-USER-01: POST /users con datos validos retorna 200")
    void addUser_withValidData_returns200() {
        given()
            .contentType("application/json")
            .body(UserTestData.validUserPayload())
        .when()
            .post("/users")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("data", notNullValue());
    }

    /**
     * TC-USER-02: Actualizacion parcial de usuario existente debe retornar HTTP 200.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-02: PATCH /users/{id} con cambios validos debe retornar 200")
    @DisplayName("TC-USER-02: PATCH /users/{id} con datos validos retorna 200")
    void patchUser_withValidChanges_returns200() {
        given()
            .contentType("application/json")
            .body(UserTestData.partialUpdatePayload())
        .when()
            .patch("/users/" + testUser.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-USER-03: Filtrar usuarios por keyword debe retornar HTTP 200 con lista.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-03: GET /users/filter con keyword valida debe retornar 200")
    @DisplayName("TC-USER-03: GET /users/filter retorna 200 con resultados")
    void filterUsers_withKeyword_returns200() {
        given()
            .contentType("application/json")
            .param("keyword", "Carlos")
            .param("page", 1)
            .param("size", 10)
        .when()
            .get("/users/filter")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("data", notNullValue());
    }

    // =========================================================================
    // Escenarios NEGATIVOS
    // =========================================================================

    /**
     * TC-USER-04: Crear usuario con nombre vacio debe retornar HTTP 400.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-04: POST /users con nombre vacio debe retornar 400")
    @DisplayName("TC-USER-04: POST /users con nombre vacio retorna 400")
    void addUser_withEmptyName_returns400() {
        given()
            .contentType("application/json")
            .body(UserTestData.userWithEmptyNamePayload())
        .when()
            .post("/users")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-USER-05: Crear usuario con email invalido debe retornar HTTP 400.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-05: POST /users con email invalido debe retornar 400")
    @DisplayName("TC-USER-05: POST /users con email invalido retorna 400")
    void addUser_withInvalidEmail_returns400() {
        given()
            .contentType("application/json")
            .body(UserTestData.userWithInvalidEmailPayload())
        .when()
            .post("/users")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-USER-06: Crear usuario con identificacion invalida debe retornar HTTP 400.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-06: POST /users con identificacion invalida debe retornar 400")
    @DisplayName("TC-USER-06: POST /users con identificacion invalida retorna 400")
    void addUser_withInvalidIdentification_returns400() {
        given()
            .contentType("application/json")
            .body(UserTestData.userWithInvalidIdentificationPayload())
        .when()
            .post("/users")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-USER-07: Actualizar usuario con ID inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-07: PATCH /users/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-USER-07: PATCH /users/{id} con ID inexistente retorna 404")
    void patchUser_withNonExistentId_returns404() {
        given()
            .contentType("application/json")
            .body(UserTestData.partialUpdatePayload())
        .when()
            .patch("/users/99999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-USER-08: Crear usuario con email duplicado debe retornar HTTP 409.
     */
    @Test
    @Story("Gestion de Usuarios")
    @Description("TC-USER-08: POST /users con email ya registrado debe retornar 409")
    @DisplayName("TC-USER-08: POST /users con email duplicado retorna 409")
    void addUser_withDuplicateEmail_returns409() {
        String payload = """
    {
      "name": "Nuevo",
      "lastName1": "Usuario",
      "lastName2": "Prueba",
      "email": "%s",
      "password": "Test123!",
      "identification": "%s",
      "phoneNumber": "88001122",
      "birthDate": "2000-05-10"
    }
    """.formatted(
                testUser.getEmail(),
                "112345" + System.nanoTime()
        );

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }
}
