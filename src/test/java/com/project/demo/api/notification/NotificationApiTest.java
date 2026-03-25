package com.project.demo.api.notification;

import com.project.demo.api.config.SecurityMockBeans;
import com.project.demo.api.data.NotificationTestData;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification.TblNotification;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.notificate.NotificationRestController;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /notifications.
 * <p>
 * Valida la capa HTTP del controlador de notificaciones mediante REST Assured + MockMvc.
 * Cubre obtención, creación, actualización completa, parcial y eliminación de notificaciones,
 * así como el comportamiento 404 para recursos inexistentes.
 */
@WebMvcTest(NotificationRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API Notifications - Pruebas funcionales REST Assured")
class NotificationApiTest extends SecurityMockBeans {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private UserRepository userRepository;

    private TblUser testUser;
    private TblNotification testNotification;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        TblRole buyerRole = new TblRole();
        buyerRole.setTitle(RoleEnum.BUYER);

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("buyer@ruraltest.com");
        testUser.setRole(buyerRole);

        testNotification = new TblNotification();
        testNotification.setId(1L);
        testNotification.setTitle("Nueva puja en tu subasta");
        testNotification.setDescription("Se realizó una nueva oferta en tu publicación de ganado bovino.");
        testNotification.setState("UNREAD");
        testNotification.setUser(testUser);
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findActiveByUserId(1L))
                .thenReturn(Collections.singletonList(testNotification));

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/notifications/1")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-NOT-02: Crear notificación para usuario existente debe retornar HTTP 201.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-02: POST /notifications/{userId} con usuario existente debe retornar 201")
    @DisplayName("TC-NOT-02: POST /notifications/{userId} retorna 201")
    void addNotification_withExistingUser_returns201() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(TblNotification.class))).thenReturn(testNotification);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(NotificationTestData.validNotificationPayload())
        .when()
                .post("/notifications/1")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("data", notNullValue());
    }

    /**
     * TC-NOT-03: Actualización completa de notificación existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-03: PUT /notifications/{id} con notificación existente debe retornar 200")
    @DisplayName("TC-NOT-03: PUT /notifications/{id} retorna 200")
    void updateNotification_withExistingId_returns200() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(TblNotification.class))).thenReturn(testNotification);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(NotificationTestData.fullUpdateNotificationPayload())
        .when()
                .put("/notifications/1")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-NOT-04: Actualización parcial (PATCH) de notificación existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-04: PATCH /notifications/{id} con estado válido debe retornar 200")
    @DisplayName("TC-NOT-04: PATCH /notifications/{id} retorna 200")
    void patchNotification_withExistingId_returns200() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(TblNotification.class))).thenReturn(testNotification);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(NotificationTestData.updateNotificationStatePayload())
        .when()
                .patch("/notifications/1")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-NOT-05: Eliminar notificación existente debe retornar HTTP 200.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-05: DELETE /notifications/{id} con notificación existente debe retornar 200")
    @DisplayName("TC-NOT-05: DELETE /notifications/{id} retorna 200")
    void deleteNotification_withExistingId_returns200() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        doNothing().when(notificationRepository).deleteById(anyLong());

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .delete("/notifications/1")
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
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/notifications/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-NOT-07: Crear notificación para usuario inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-07: POST /notifications/{userId} con usuario inexistente debe retornar 404")
    @DisplayName("TC-NOT-07: POST /notifications/{userId} con usuario inexistente retorna 404")
    void addNotification_withNonExistentUser_returns404() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(NotificationTestData.validNotificationPayload())
        .when()
                .post("/notifications/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-NOT-08: Eliminar notificación inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Notificaciones")
    @Description("TC-NOT-08: DELETE /notifications/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-NOT-08: DELETE /notifications/{id} con ID inexistente retorna 404")
    void deleteNotification_withNonExistentId_returns404() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .delete("/notifications/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
