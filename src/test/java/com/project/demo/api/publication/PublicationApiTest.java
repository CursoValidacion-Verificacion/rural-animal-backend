package com.project.demo.api.publication;

import com.project.demo.api.config.SecurityMockBeans;
import com.project.demo.api.data.PublicationTestData;
import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.photo.TblPhotoRepository;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.publication.PublicationRestController;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas funcionales de API para el endpoint /publications.
 * <p>
 * Valida la capa HTTP del controlador de publicaciones mediante REST Assured + MockMvc.
 * Cubre listado de ventas, subastas, publicaciones filtradas, creación y actualización parcial,
 * así como el comportamiento cuando no se encuentra un recurso.
 */
@WebMvcTest(PublicationRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API Publications - Pruebas funcionales REST Assured")
class PublicationApiTest extends SecurityMockBeans {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TblPublicationRepository tblPublicationRepository;

    @MockBean
    private TblDirectionRepository tblDirectionRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TblPhotoRepository tblPhotoRepository;

    private TblPublication testPublication;
    private TblUser testUser;
    private TblDirection testDirection;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        TblRole sellerRole = new TblRole();
        sellerRole.setTitle(RoleEnum.SELLER);

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("seller@ruraltest.com");
        testUser.setRole(sellerRole);
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));

        testDirection = new TblDirection();
        testDirection.setId(1L);
        testDirection.setProvince("Alajuela");
        testDirection.setCanton("Grecia");
        testDirection.setDistrict("San Isidro");

        testPublication = new TblPublication();
        testPublication.setId(1L);
        testPublication.setTitle("Venta de novillo Holstein");
        testPublication.setSpecie("bovino");
        testPublication.setPrice(350000L);
        testPublication.setType("SALE");
        testPublication.setState("ACTIVE");
        testPublication.setDirection(testDirection);
        testPublication.setUser(testUser);
        testPublication.setPhotos(Collections.emptyList());

        // Mockear el contexto de seguridad para endpoints que requieren autenticación
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
     * TC-PUB-01: Listar todas las ventas debe retornar HTTP 200.
     */
    @Test
    @Story("Publicaciones - Ventas")
    @Description("TC-PUB-01: GET /publications/sales debe retornar 200 con lista de ventas")
    @DisplayName("TC-PUB-01: GET /publications/sales retorna 200")
    void getAllSales_returns200WithList() {
        Page<TblPublication> page = new PageImpl<>(Collections.singletonList(testPublication));
        when(tblPublicationRepository.findAllSales(any(Pageable.class))).thenReturn(page);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("page", 1)
                .param("size", 6)
        .when()
                .get("/publications/sales")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-PUB-02: Listar todas las subastas debe retornar HTTP 200.
     */
    @Test
    @Story("Publicaciones - Subastas")
    @Description("TC-PUB-02: GET /publications/auctions debe retornar 200 con lista de subastas")
    @DisplayName("TC-PUB-02: GET /publications/auctions retorna 200")
    void getAllAuctions_returns200WithList() {
        Page<TblPublication> page = new PageImpl<>(Collections.singletonList(testPublication));
        when(tblPublicationRepository.findAllAuctions(any(Pageable.class))).thenReturn(page);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("page", 1)
                .param("size", 6)
        .when()
                .get("/publications/auctions")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-PUB-03: Obtener publicaciones filtradas debe retornar HTTP 200.
     */
    @Test
    @Story("Publicaciones - Filtrado")
    @Description("TC-PUB-03: GET /publications/filtered con filtros válidos debe retornar 200")
    @DisplayName("TC-PUB-03: GET /publications/filtered retorna 200")
    void getFilteredPublications_withValidFilters_returns200() {
        Page<TblPublication> page = new PageImpl<>(Collections.singletonList(testPublication));
        when(tblPublicationRepository.findFilteredPublications(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("type", "SALE")
                .param("search", "bovino")
                .param("sort", "price")
        .when()
                .get("/publications/filtered")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    /**
     * TC-PUB-04: Actualización parcial de publicación existente debe retornar HTTP 200.
     */
    @Test
    @Story("Publicaciones - Actualización")
    @Description("TC-PUB-04: PATCH /publications/{id} con publicación existente debe retornar 200")
    @DisplayName("TC-PUB-04: PATCH /publications/{id} con ID válido retorna 200")
    void patchPublication_withExistingId_returns200() {
        when(tblPublicationRepository.findById(1L)).thenReturn(Optional.of(testPublication));
        when(tblPublicationRepository.save(any(TblPublication.class))).thenReturn(testPublication);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(PublicationTestData.validSalePublicationPayload())
        .when()
                .patch("/publications/1")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    /**
     * TC-PUB-05: Obtener publicaciones por userId existente debe retornar HTTP 200.
     */
    @Test
    @Story("Publicaciones - Por Usuario")
    @Description("TC-PUB-05: GET /publications/user/{userId}/publications con usuario válido retorna 200")
    @DisplayName("TC-PUB-05: GET publicaciones por usuario retorna 200")
    void getAllByUserId_withExistingUser_returns200() {
        Page<TblPublication> page = new PageImpl<>(Collections.singletonList(testPublication));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tblPublicationRepository.findTblPublicationsByUserId(anyLong(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/publications/user/1/publications")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    // =========================================================================
    // Escenarios NEGATIVOS
    // =========================================================================

    /**
     * TC-PUB-06: Obtener publicaciones de usuario inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Publicaciones - Por Usuario")
    @Description("TC-PUB-06: GET /publications/user/{userId}/publications con usuario inexistente retorna 404")
    @DisplayName("TC-PUB-06: GET publicaciones con userId inexistente retorna 404")
    void getAllByUserId_withNonExistentUser_returns404() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
        .when()
                .get("/publications/user/9999/publications")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    /**
     * TC-PUB-07: Actualización parcial de publicación inexistente debe retornar HTTP 404.
     */
    @Test
    @Story("Publicaciones - Actualización")
    @Description("TC-PUB-07: PATCH /publications/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-PUB-07: PATCH /publications/{id} con ID inexistente retorna 404")
    void patchPublication_withNonExistentId_returns404() {
        when(tblPublicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(PublicationTestData.validSalePublicationPayload())
        .when()
                .patch("/publications/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
