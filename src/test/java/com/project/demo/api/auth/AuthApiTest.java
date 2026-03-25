package com.project.demo.api.auth;

import com.project.demo.api.config.SecurityMockBeans;
import com.project.demo.api.data.AuthTestData;
import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.TokenStorage;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.EmailService;
import com.project.demo.rest.auth.AuthRestController;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /auth.
 * <p>
 * Utiliza REST Assured con MockMvc para validar los contratos HTTP
 * del controlador de autenticación sin necesitar una base de datos real.
 * Cubre escenarios positivos (login exitoso, registro válido) y negativos
 * (credenciales incorrectas, email inválido, contraseña débil, menor de edad).
 */
@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API Auth - Pruebas funcionales REST Assured")
class AuthApiTest extends SecurityMockBeans {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @MockBean
    private TblRoleRepository roleRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private TokenStorage tokenStorage;

    private TblUser testUser;
    private TblRole buyerRole;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        buyerRole = new TblRole();
        buyerRole.setTitle(RoleEnum.BUYER);

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("andres@gmail.com");
        testUser.setPassword("encoded-password");
        testUser.setName("Andres");
        testUser.setLastName1("Torres");
        testUser.setRole(buyerRole);
        testUser.setBirthDate(LocalDate.of(2000, 5, 10));
    }

    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-AUTH-01: Login con credenciales válidas debe retornar HTTP 200 y token JWT.
     */
    @Test
    @Story("Autenticación")
    @Description("TC-AUTH-01: Login con credenciales válidas debe retornar 200 y un token JWT")
    @DisplayName("TC-AUTH-01: Login válido retorna 200 y token")
    void login_withValidCredentials_returns200AndToken() {
        when(userRepository.findByEmail("andres@gmail.com")).thenReturn(Optional.of(testUser));
        when(authenticationService.authenticate(any(TblUser.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(TblUser.class))).thenReturn("jwt-token-mock");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.validLoginPayload())
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("token", equalTo("jwt-token-mock"));
    }

    /**
     * TC-AUTH-02: Registro con datos válidos debe retornar HTTP 200.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-02: Registro con datos válidos debe retornar 200")
    @DisplayName("TC-AUTH-02: Registro válido retorna 200")
    void signup_withValidData_returns200() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByTitle(any())).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(TblUser.class))).thenReturn(testUser);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.validSignupPayload())
        .when()
                .post("/auth/signup")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-AUTH-03: Confirmación de email con ID válido debe retornar HTTP 200.
     */
    @Test
    @Story("Confirmación de Email")
    @Description("TC-AUTH-03: Confirmar email con ID válido retorna 200")
    @DisplayName("TC-AUTH-03: EmailConfirm con ID válido retorna 200")
    void emailConfirm_withValidId_returns200() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(TblUser.class))).thenReturn(testUser);

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/auth/emailConfirm/1")
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
    @Story("Autenticación")
    @Description("TC-AUTH-04: Login con usuario inexistente debe retornar 401")
    @DisplayName("TC-AUTH-04: Login con usuario inexistente retorna 401")
    void login_withNonExistentUser_returns401() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.nonExistentUserPayload())
        .when()
                .post("/auth/login")
        .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * TC-AUTH-05: Registro con email inválido debe retornar HTTP 400.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-05: Registro con email inválido debe retornar 400")
    @DisplayName("TC-AUTH-05: Registro con email inválido retorna 400")
    void signup_withInvalidEmail_returns400() {
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.signupWithInvalidEmailPayload())
        .when()
                .post("/auth/signup")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-AUTH-06: Registro con contraseña débil debe retornar HTTP 400.
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-06: Registro con contraseña débil debe retornar 400")
    @DisplayName("TC-AUTH-06: Registro con contraseña débil retorna 400")
    void signup_withWeakPassword_returns400() {
        RestAssuredMockMvc.given()
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
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.signupWithMinorAgePayload())
        .when()
                .post("/auth/signup")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-AUTH-08: Registro con email ya existente debe retornar HTTP 409 (CONFLICT).
     */
    @Test
    @Story("Registro")
    @Description("TC-AUTH-08: Registro con email existente debe retornar 409")
    @DisplayName("TC-AUTH-08: Registro con email duplicado retorna 409")
    void signup_withExistingEmail_returns409() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(AuthTestData.validSignupPayload())
        .when()
                .post("/auth/signup")
        .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    /**
     * TC-AUTH-09: Confirmación de email con ID inexistente debe retornar HTTP 400.
     */
    @Test
    @Story("Confirmación de Email")
    @Description("TC-AUTH-09: EmailConfirm con ID inexistente debe retornar 400")
    @DisplayName("TC-AUTH-09: EmailConfirm con ID inexistente retorna 400")
    void emailConfirm_withInvalidId_returns400() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/auth/emailConfirm/999")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
