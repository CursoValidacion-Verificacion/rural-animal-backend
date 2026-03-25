package com.project.demo.api.config;

import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.user.TblUser;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

/**
 * Clase base de configuración para todas las pruebas REST Assured.
 * <p>
 * Centraliza la configuración de MockMvc, la generación de tokens JWT
 * y la construcción de especificaciones de petición reutilizables.
 * Implementa el patrón "Capa de Configuración" para mantener separada
 * la infraestructura de las pruebas de la lógica de negocio.
 */
public abstract class RestAssuredConfig {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Inicializa RestAssuredMockMvc con el contexto de la aplicación Spring.
     * Debe ser llamado en el @BeforeEach de cada clase de prueba.
     */
    protected void setUp() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    /**
     * Construye un usuario de prueba con rol BUYER y genera un token JWT válido.
     *
     * @return especificación de petición con el header Authorization ya configurado
     */
    protected MockMvcRequestSpecification buildBuyerRequestSpec() {
        TblUser buyer = buildTestUser(RoleEnum.BUYER);
        String token = jwtService.generateToken(buyer);
        return RestAssuredMockMvc.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }

    /**
     * Construye un usuario de prueba con rol ADMIN y genera un token JWT válido.
     *
     * @return especificación de petición con el header Authorization ya configurado
     */
    protected MockMvcRequestSpecification buildAdminRequestSpec() {
        TblUser admin = buildTestUser(RoleEnum.ADMIN);
        String token = jwtService.generateToken(admin);
        return RestAssuredMockMvc.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }

    /**
     * Construye un usuario de prueba sin autenticación (request anónimo).
     *
     * @return especificación de petición sin header Authorization
     */
    protected MockMvcRequestSpecification buildAnonymousRequestSpec() {
        return RestAssuredMockMvc.given()
                .contentType("application/json");
    }

    // ---------------------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------------------

    private TblUser buildTestUser(RoleEnum roleEnum) {
        TblRole role = new TblRole();
        role.setTitle(roleEnum);

        TblUser user = new TblUser();
        user.setId(999L);
        user.setEmail("test-config@ruraltest.com");
        user.setPassword(passwordEncoder.encode("Test123!"));
        user.setName("Test");
        user.setLastName1("Config");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setRole(role);
        return user;
    }
}
