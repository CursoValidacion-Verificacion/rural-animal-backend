package com.project.demo.api.notification;

import com.project.demo.api.data.NotificationTestData;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification.TblNotification;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Pruebas funcionales de API para el endpoint /notifications.
 *
 * Usa @SpringBootTest con servidor real en puerto aleatorio y MariaDB/Redis via Docker.
 * NotificationRestController no usa EmailService, por lo que no requiere @MockBean.
 * Cubre obtencion, creacion, actualizacion completa, parcial y eliminacion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API Notifications - Pruebas funcionales REST Assured")
@Tag("api")
class NotificationApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TblUser testUser;
    private TblNotification testNotification;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        TblRole buyerRole = roleRepository.findByTitle(RoleEnum.BUYER)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.BUYER);
                    return roleRepository.save(r);
                });

        testUser = new TblUser();
        testUser.setName("Buyer");
        testUser.setLastName1("Notif");
        testUser.setEmail("buyer.notif.test@ruraltest.com");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
        testUser.setIdentification("112345678");
        testUser.setPhoneNumber("88001122");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setRole(buyerRole);
        testUser = userRepository.save(testUser);

        testNotification = new TblNotification();
        testNotification.setTitle("Nueva puja en tu subasta");
        testNotification.setDescription("Se realizo una nueva oferta en tu publicacion.");
        testNotification.setType("BID");
        testNotification.setState("Active");
        testNotification.setUser(testUser);
        testNotification = notificationRepository.save(testNotification);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-NOT-01: Obtener notificaciones de usuario existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-01: GET /notifications/{userId} con usuario existente debe retornar 200")
    @DisplayName("TC-NOT-01: GET /notifications/{userId} con usuario existente retorna 200")
    void getNotificationsByUserId_withExistingUser_returns200() {
        given()
            .contentType("application/json")
        .when()
            .get("/notifications/" + testUser.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("data", notNullValue());
    }

    /**
     * TC-NOT-02: Crear notificacion para usuario existente debe retornar HTTP 201.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-02: POST /notifications/{userId} con usuario existente debe retornar 201")
    @DisplayName("TC-NOT-02: POST /notifications/{userId} retorna 201")
    void addNotification_withExistingUser_returns201() {
        given()
            .contentType("application/json")
            .body(NotificationTestData.validNotificationPayload())
        .when()
            .post("/notifications/" + testUser.getId())
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("data", notNullValue());
    }

    /**
     * TC-NOT-03: Actualizacion completa de notificacion existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-03: PUT /notifications/{id} con notificacion existente debe retornar 200")
    @DisplayName("TC-NOT-03: PUT /notifications/{id} retorna 200")
    void updateNotification_withExistingId_returns200() {
        given()
            .contentType("application/json")
            .body(NotificationTestData.fullUpdateNotificationPayload())
        .when()
            .put("/notifications/" + testNotification.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-NOT-04: Actualizacion parcial (PATCH) de notificacion existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-04: PATCH /notifications/{id} con estado valido debe retornar 200")
    @DisplayName("TC-NOT-04: PATCH /notifications/{id} retorna 200")
    void patchNotification_withExistingId_returns200() {
        given()
            .contentType("application/json")
            .body(NotificationTestData.updateNotificationStatePayload())
        .when()
            .patch("/notifications/" + testNotification.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-NOT-05: Eliminar notificacion existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-05: DELETE /notifications/{id} con notificacion existente debe retornar 200")
    @DisplayName("TC-NOT-05: DELETE /notifications/{id} retorna 200")
    void deleteNotification_withExistingId_returns200() {
        given()
            .contentType("application/json")
        .when()
            .delete("/notifications/" + testNotification.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    // =========================================================================
    // Escenarios NEGATIVOS
    // =========================================================================

    /**
     * TC-NOT-06: Obtener notificaciones de usuario inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-06: GET /notifications/{userId} con usuario inexistente debe retornar 404")
    @DisplayName("TC-NOT-06: GET /notifications/{userId} con usuario inexistente retorna 404")
    void getNotificationsByUserId_withNonExistentUser_returns404() {
        given()
            .contentType("application/json")
        .when()
            .get("/notifications/99999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-NOT-07: Crear notificacion para usuario inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-07: POST /notifications/{userId} con usuario inexistente debe retornar 404")
    @DisplayName("TC-NOT-07: POST /notifications/{userId} con usuario inexistente retorna 404")
    void addNotification_withNonExistentUser_returns404() {
        given()
            .contentType("application/json")
            .body(NotificationTestData.validNotificationPayload())
        .when()
            .post("/notifications/99999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-NOT-08: Eliminar notificacion inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-08: DELETE /notifications/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-NOT-08: DELETE /notifications/{id} con ID inexistente retorna 404")
    void deleteNotification_withNonExistentId_returns404() {
        given()
            .contentType("application/json")
        .when()
            .delete("/notifications/99999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
