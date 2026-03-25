package com.project.demo.api.veterinary;

import com.project.demo.api.config.SecurityMockBeans;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.veterinaryAppointment.AvailabilityDto;
import com.project.demo.logic.entity.veterinaryAppointment.CreateAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentService;
import com.project.demo.logic.utils.EmailService;
import com.project.demo.rest.veterinaryAppointment.VeterinaryAppointmentRestController;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /veterinary_appointments.
 * <p>
 * Valida la capa HTTP del controlador de citas veterinarias mediante REST Assured + MockMvc.
 * Cubre listado de citas del usuario autenticado, consulta de disponibilidad,
 * creación de cita exitosa, y manejo de errores (no encontrado, servicio fallido).
 */
@WebMvcTest(VeterinaryAppointmentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API VeterinaryAppointments - Pruebas funcionales REST Assured")
class VeterinaryAppointmentApiTest extends SecurityMockBeans {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VeterinaryAppointmentService veterinaryAppointmentService;

    @MockBean
    private EmailService emailService;

    private TblUser testUser;
    private VeterinaryAppointmentDto appointmentDto;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        TblRole buyerRole = new TblRole();
        buyerRole.setTitle(RoleEnum.BUYER);

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("buyer@ruraltest.com");
        testUser.setName("Ana");
        testUser.setLastName1("Mora");
        testUser.setRole(buyerRole);
        testUser.setBirthDate(LocalDate.of(1995, 6, 15));

        appointmentDto = new VeterinaryAppointmentDto();

        // Configurar SecurityContext con el usuario autenticado
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
    }

    // =========================================================================
    // Escenarios POSITIVOS
    // =========================================================================

    /**
     * TC-VET-01: Obtener citas del usuario autenticado debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-01: GET /veterinary_appointments debe retornar 200 con lista de citas del usuario")
    @DisplayName("TC-VET-01: GET /veterinary_appointments retorna 200")
    void getUserAppointments_authenticated_returns200() {
        Page<VeterinaryAppointmentDto> page = new PageImpl<>(Collections.singletonList(appointmentDto));
        when(veterinaryAppointmentService.getUserAppointments(anyLong(), any(Pageable.class))).thenReturn(page);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("page", 1)
                .param("size", 10)
        .when()
                .get("/veterinary_appointments")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-VET-02: Consultar disponibilidad con rango de fechas válido debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-02: GET /veterinary_appointments/availability con fechas válidas debe retornar 200")
    @DisplayName("TC-VET-02: GET /veterinary_appointments/availability retorna 200")
    void getAvailableDates_withValidRange_returns200() {
        List<AvailabilityDto> availabilities = Collections.singletonList(new AvailabilityDto());
        when(veterinaryAppointmentService.getAvailableDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(availabilities);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("startDate", "2026-04-01T08:00:00")
                .param("endDate", "2026-04-07T18:00:00")
        .when()
                .get("/veterinary_appointments/availability")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-VET-03: Crear cita veterinaria con datos válidos debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-03: POST /veterinary_appointments con datos válidos debe retornar 200")
    @DisplayName("TC-VET-03: POST /veterinary_appointments retorna 200")
    void createAppointment_withValidData_returns200() throws Exception {
        when(veterinaryAppointmentService.createAppointment(any(CreateAppointmentDto.class), anyLong()))
                .thenReturn(appointmentDto);

        Map<String, Object> payload = new HashMap<>();
        payload.put("appointmentDate", "2026-04-05T10:00:00");
        payload.put("reason", "Revisión general del ganado");
        payload.put("veterinaryId", 1);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(payload)
        .when()
                .post("/veterinary_appointments")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-VET-04: Listar todas las citas (admin) debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias - Admin")
    @Description("TC-VET-04: GET /veterinary_appointments/all debe retornar 200 con todas las citas")
    @DisplayName("TC-VET-04: GET /veterinary_appointments/all retorna 200")
    void getAllAppointments_asAdmin_returns200() {
        when(veterinaryAppointmentService.getAll())
                .thenReturn(Collections.singletonList(appointmentDto));

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/veterinary_appointments/all")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    // =========================================================================
    // Escenarios NEGATIVOS
    // =========================================================================

    /**
     * TC-VET-05: Error al obtener citas del usuario debe retornar HTTP 500.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-05: GET /veterinary_appointments cuando el servicio falla debe retornar 500")
    @DisplayName("TC-VET-05: GET /veterinary_appointments con error de servicio retorna 500")
    void getUserAppointments_whenServiceFails_returns500() {
        when(veterinaryAppointmentService.getUserAppointments(anyLong(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/veterinary_appointments")
        .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * TC-VET-06: Crear cita cuando el servicio lanza ResponseStatusException debe retornar HTTP 400.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-06: POST /veterinary_appointments cuando el servicio lanza BAD_REQUEST debe retornar 400")
    @DisplayName("TC-VET-06: POST /veterinary_appointments con horario no disponible retorna 400")
    void createAppointment_whenSlotUnavailable_returns400() throws Exception {
        when(veterinaryAppointmentService.createAppointment(any(CreateAppointmentDto.class), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario no disponible"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("appointmentDate", "2026-04-05T10:00:00");
        payload.put("reason", "Revisión");
        payload.put("veterinaryId", 1);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(payload)
        .when()
                .post("/veterinary_appointments")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
