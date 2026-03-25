package com.project.demo.api.user;

import com.project.demo.api.config.SecurityMockBeans;
import com.project.demo.api.data.UserTestData;
import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.user.UserService;
import com.project.demo.logic.utils.EmailService;
import com.project.demo.rest.user.UserRestController;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /users.
 * <p>
 * Valida la capa HTTP del controlador de usuarios mediante REST Assured + MockMvc:
 * creación, consulta paginada, actualización parcial y eliminación.
 * Incluye escenarios positivos y negativos con validaciones de status code y body.
 */
@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API Users - Pruebas funcionales REST Assured")
class UserApiTest extends SecurityMockBeans {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TblDirectionRepository tblDirectionRepository;

    @MockBean
    private TblRoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userService;

    private TblUser testUser;
    private TblRole buyerRole;
    private TblDirection testDirection;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        buyerRole = new TblRole();
        buyerRole.setTitle(RoleEnum.BUYER);

        testDirection = new TblDirection();
        testDirection.setId(1L);
        testDirection.setProvince("San José");
        testDirection.setCanton("Goicoechea");
        testDirection.setDistrict("San Francisco");

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setName("Carlos");
        testUser.setLastName1("Rodríguez");
        testUser.setEmail("carlos.test@ruraltest.com");
        testUser.setPassword("encoded-password");
        testUser.setIdentification("1-1234-5678");
        testUser.setPhoneNumber("8800-1122");
        testUser.setRole(buyerRole);
        testUser.setDirection(testDirection);
        testUser.setBirthDate(LocalDate.of(1995, 3, 20));
    }

    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-USER-01: Crear usuario con datos válidos debe retornar HTTP 200.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-01: Crear usuario con datos válidos debe retornar 200 y el usuario creado")
    @DisplayName("TC-USER-01: POST /users con datos válidos retorna 200")
    void addUser_withValidData_returns200() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByTitle(any())).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(tblDirectionRepository.save(any(TblDirection.class))).thenReturn(testDirection);
        when(userRepository.save(any(TblUser.class))).thenReturn(testUser);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.validUserPayload())
        .when()
                .post("/users")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-USER-02: Actualización parcial de usuario existente debe retornar HTTP 200.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-02: PATCH /users/{id} con cambios válidos debe retornar 200")
    @DisplayName("TC-USER-02: PATCH /users/{id} con datos válidos retorna 200")
    void patchUser_withValidChanges_returns200() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(TblUser.class))).thenReturn(testUser);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.partialUpdatePayload())
        .when()
                .patch("/users/1")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-USER-03: Filtrar usuarios por keyword debe retornar HTTP 200 con lista.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-03: GET /users/filter con keyword válida debe retornar 200")
    @DisplayName("TC-USER-03: GET /users/filter retorna 200 con resultados")
    void filterUsers_withKeyword_returns200() {
        Page<TblUser> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findUsersByKeyword(anyString(), any(Pageable.class))).thenReturn(page);

        RestAssuredMockMvc.given()
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
     * TC-USER-04: Crear usuario con nombre vacío debe retornar HTTP 400.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-04: POST /users con nombre vacío debe retornar 400")
    @DisplayName("TC-USER-04: POST /users con nombre vacío retorna 400")
    void addUser_withEmptyName_returns400() {
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.userWithEmptyNamePayload())
        .when()
                .post("/users")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-USER-05: Crear usuario con email inválido debe retornar HTTP 400.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-05: POST /users con email inválido debe retornar 400")
    @DisplayName("TC-USER-05: POST /users con email inválido retorna 400")
    void addUser_withInvalidEmail_returns400() {
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.userWithInvalidEmailPayload())
        .when()
                .post("/users")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * TC-USER-06: Crear usuario con identificación inválida debe retornar HTTP 400.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-06: POST /users con identificación inválida debe retornar 400")
    @DisplayName("TC-USER-06: POST /users con identificación inválida retorna 400")
    void addUser_withInvalidIdentification_returns400() {
        RestAssuredMockMvc.given()
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
    @Story("Gestión de Usuarios")
    @Description("TC-USER-07: PATCH /users/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-USER-07: PATCH /users/{id} con ID inexistente retorna 404")
    void patchUser_withNonExistentId_returns404() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.partialUpdatePayload())
        .when()
                .patch("/users/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-USER-08: Crear usuario con email duplicado debe retornar HTTP 409.
     */
    @Test
    @Story("Gestión de Usuarios")
    @Description("TC-USER-08: POST /users con email ya registrado debe retornar 409")
    @DisplayName("TC-USER-08: POST /users con email duplicado retorna 409")
    void addUser_withDuplicateEmail_returns409() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(UserTestData.validUserPayload())
        .when()
                .post("/users")
        .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }
}
