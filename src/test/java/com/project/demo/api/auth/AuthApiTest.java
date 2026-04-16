package com.project.demo.api.auth;

import com.project.demo.api.data.AuthTestData;
import com.project.demo.logic.entity.direction.TblDirection;
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
 * Pruebas funcionales de API para el endpoint /auth.
 *
 * Usa @SpringBootTest con servidor real en puerto aleatorio y MariaDB/Redis via Docker.
 * EmailService se mockea para evitar envios reales de correo.
 * Cubre escenarios positivos (login exitoso, registro valido) y negativos
 * (usuario no existe, email invalido, contrasena debil, menor de edad, email duplicado).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API Auth - Pruebas funcionales REST Assured")
@Tag("api")
class AuthApiTest {

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

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Insertar rol BUYER si no existe
        TblRole buyerRole = roleRepository.findByTitle(RoleEnum.BUYER)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.BUYER);
                    return roleRepository.save(r);
                });

        TblDirection direction = new TblDirection();
        direction.setProvince("San Jose");
        direction.setCanton("Central");
        direction.setDistrict("Carmen");
        direction.setOtherDetails("Direccion de prueba");
        direction = directionRepository.save(direction);

        // Insertar usuario de prueba para escenarios de loginS
        testUser = new TblUser();
        testUser.setName("Andres");
        testUser.setLastName1("Torres");
        testUser.setEmail("andres.test.auth" + System.nanoTime() + "@ruraltest.com");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
        testUser.setPhoneNumber("88001123");
        testUser.setIdentification("112345" + System.nanoTime());
        testUser.setBirthDate(LocalDate.of(2000, 5, 10));
        testUser.setRole(buyerRole);
        testUser.setDirection(direction);
        testUser = userRepository.save(testUser);
    }



    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-AUTH-01: Login con credenciales validas debe retornar HTTP 200 y token JWT.
     */
    @Test
    @Story("Autenticacion")
    @Description("TC-AUTH-01: Login con credenciales validas debe retornar 200 y un token JWT")
    @DisplayName("TC-AUTH-01: Login valido retorna 200 y token")
    void login_withValidCredentials_returns200AndToken() {
        String loginPayload = """
        {
          "email": "%s",
          "password": "Test123!"
        }
        """.formatted(testUser.getEmail());

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("token", notNullValue());
    }

    /**
     * TC-AUTH-02: Registro con datos validos debe retornar HTTP 200.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-02: Registro con datos validos debe retornar 200")
    @DisplayName("TC-AUTH-02: Registro valido retorna 200")
    void signup_withValidData_returns200() {
        given()
            .contentType("application/json")
            .body(AuthTestData.validSignupPayload())
        .when()
            .post("/auth/signup")
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-AUTH-03: Confirmacion de email con ID valido debe retornar HTTP 200.
     */
    @Test
    @Story("Confirmacion de Email")
    @Description("TC-AUTH-03: Confirmar email con ID valido retorna 200")
    @DisplayName("TC-AUTH-03: EmailConfirm con ID valido retorna 200")
    void emailConfirm_withValidId_returns200() {
        given()
            .contentType("application/json")
        .when()
            .get("/auth/emailConfirm/" + testUser.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    // =========================================================================
    // Escenarios NEGATIVOS
    // =========================================================================

    /**
     * TC-AUTH-04: Login con usuario inexistente debe retornar HTTP 401.
     */
    @Test
    @Story("Autenticacion")
    @Description("TC-AUTH-04: Login con usuario inexistente debe retornar 401")
    @DisplayName("TC-AUTH-04: Login con usuario inexistente retorna 401")
    void login_withNonExistentUser_returns401() {
        given()
            .contentType("application/json")
            .body(AuthTestData.nonExistentUserPayload())
        .when()
            .post("/auth/login")
        .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * TC-AUTH-05: Registro con email invalido debe retornar HTTP 400.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-05: Registro con email invalido debe retornar 400")
    @DisplayName("TC-AUTH-05: Registro con email invalido retorna 400")
    void signup_withInvalidEmail_returns400() {
        given()
            .contentType("application/json")
            .body(AuthTestData.signupWithInvalidEmailPayload())
        .when()
            .post("/auth/signup")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-AUTH-06: Registro con contrasena debil debe retornar HTTP 400.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-06: Registro con contrasena debil debe retornar 400")
    @DisplayName("TC-AUTH-06: Registro con contrasena debil retorna 400")
    void signup_withWeakPassword_returns400() {
        given()
            .contentType("application/json")
            .body(AuthTestData.signupWithWeakPasswordPayload())
        .when()
            .post("/auth/signup")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-AUTH-07: Registro con usuario menor de edad debe retornar HTTP 400.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-07: Registro con menor de edad debe retornar 400")
    @DisplayName("TC-AUTH-07: Registro con menor de edad retorna 400")
    void signup_withMinorAge_returns400() {
        given()
            .contentType("application/json")
            .body(AuthTestData.signupWithMinorAgePayload())
        .when()
            .post("/auth/signup")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-AUTH-08: Registro con email ya existente debe retornar HTTP 409.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-08: Registro con email existente debe retornar 409")
    @DisplayName("TC-AUTH-08: Registro con email duplicado retorna 409")
    void signup_withExistingEmail_returns409() {
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
                .post("/auth/signup")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    /**
     * TC-AUTH-09: Confirmacion de email con ID inexistente debe retornar HTTP 400.
     */
    @Test
    @Story("Confirmacion de Email")
    @Description("TC-AUTH-09: EmailConfirm con ID inexistente debe retornar 400")
    @DisplayName("TC-AUTH-09: EmailConfirm con ID inexistente retorna 400")
    void emailConfirm_withInvalidId_returns400() {
        given()
            .contentType("application/json")
        .when()
            .get("/auth/emailConfirm/99999")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
