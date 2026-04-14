package com.project.demo.api.veterinary;

import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.veterinaryAppointment.AvailabilityDto;
import com.project.demo.logic.entity.veterinaryAppointment.CreateAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentDto;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentService;
import com.project.demo.logic.utils.EmailService;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /veterinary_appointments.
 *
 * Usa @SpringBootTest con servidor real y JWT real.
 * VeterinaryAppointmentService y EmailService se mockean porque dependen de
 * Google Calendar y SMTP que no estan disponibles en el entorno de pruebas.
 * El resto del contexto (Spring Security, JwtService, MariaDB, Redis) es real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API VeterinaryAppointments - Pruebas funcionales REST Assured")
@Tag("api")
class VeterinaryAppointmentApiTest {

    @LocalServerPort
    private int port;

    @MockBean
    private VeterinaryAppointmentService veterinaryAppointmentService;

    @MockBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private TblUser testUser;
    private TblUser adminUser;
    private String buyerToken;
    private String adminToken;
    private VeterinaryAppointmentDto appointmentDto;

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

        TblRole adminRole = roleRepository.findByTitle(RoleEnum.ADMIN)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.ADMIN);
                    return roleRepository.save(r);
                });

        testUser = new TblUser();
        testUser.setName("Ana");
        testUser.setLastName1("Mora");
        testUser.setEmail("ana.vet.test"+System.nanoTime()+"@ruraltest.com");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
        testUser.setIdentification("112345678"+System.nanoTime());
        testUser.setPhoneNumber("88001122");
        testUser.setBirthDate(LocalDate.of(1995, 6, 15));
        testUser.setRole(buyerRole);
        testUser = userRepository.save(testUser);
        buyerToken = jwtService.generateToken(testUser);

        adminUser = new TblUser();
        adminUser.setName("Admin");
        adminUser.setLastName1("Vet");
        adminUser.setEmail("admin.vet.test"+System.nanoTime()+"@ruraltest.com");
        adminUser.setPassword(passwordEncoder.encode("Test123!"));
        adminUser.setIdentification("212345678"+System.nanoTime());
        adminUser.setPhoneNumber("88002233");
        adminUser.setBirthDate(LocalDate.of(1985, 1, 1));
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        appointmentDto = new VeterinaryAppointmentDto();
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

        given()
            .header("Authorization", "Bearer " + buyerToken)
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
     * TC-VET-02: Consultar disponibilidad con rango de fechas valido debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-02: GET /veterinary_appointments/availability con fechas validas debe retornar 200")
    @DisplayName("TC-VET-02: GET /veterinary_appointments/availability retorna 200")
    void getAvailableDates_withValidRange_returns200() {
        List<AvailabilityDto> availabilities = Collections.singletonList(new AvailabilityDto());
        when(veterinaryAppointmentService.getAvailableDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(availabilities);

        given()
            .header("Authorization", "Bearer " + buyerToken)
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
     * TC-VET-03: Crear cita veterinaria con datos validos debe retornar HTTP 200.
     */
    @Test
    @Story("Citas Veterinarias")
    @Description("TC-VET-03: POST /veterinary_appointments con datos validos debe retornar 200")
    @DisplayName("TC-VET-03: POST /veterinary_appointments retorna 200")
    void createAppointment_withValidData_returns200() throws Exception {
        when(veterinaryAppointmentService.createAppointment(any(CreateAppointmentDto.class), anyLong()))
                .thenReturn(appointmentDto);

        Map<String, Object> payload = new HashMap<>();
        payload.put("appointmentDate", "2026-04-05T10:00:00");
        payload.put("reason", "Revision general del ganado");
        payload.put("veterinaryId", 1);

        given()
            .header("Authorization", "Bearer " + buyerToken)
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

        given()
            .header("Authorization", "Bearer " + adminToken)
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

        given()
            .header("Authorization", "Bearer " + buyerToken)
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
        payload.put("reason", "Revision");
        payload.put("veterinaryId", 1);

        given()
            .header("Authorization", "Bearer " + buyerToken)
            .contentType("application/json")
            .body(payload)
        .when()
            .post("/veterinary_appointments")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
