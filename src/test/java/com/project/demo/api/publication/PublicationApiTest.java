package com.project.demo.api.publication;

import com.project.demo.api.data.PublicationTestData;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
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

import java.time.Instant;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API Publications - Pruebas funcionales REST Assured")
@Tag("api")
class PublicationApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TblPublicationRepository publicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private TblDirectionRepository directionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private TblUser sellerUser;
    private TblUser adminUser;
    private TblPublication testPublication;
    private TblDirection testDirection;
    private String sellerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        TblRole sellerRole = roleRepository.findByTitle(RoleEnum.SELLER)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.SELLER);
                    return roleRepository.save(r);
                });

        TblRole adminRole = roleRepository.findByTitle(RoleEnum.ADMIN)
                .orElseGet(() -> {
                    TblRole r = new TblRole();
                    r.setTitle(RoleEnum.ADMIN);
                    return roleRepository.save(r);
                });

        sellerUser = new TblUser();
        sellerUser.setName("Seller");
        sellerUser.setLastName1("Publication");
        sellerUser.setEmail("seller.pub.test"+System.nanoTime()+"@ruraltest.com");
        sellerUser.setPassword(passwordEncoder.encode("Test123!"));
        sellerUser.setIdentification("112345678"+System.nanoTime());
        sellerUser.setPhoneNumber("88001122");
        sellerUser.setBirthDate(LocalDate.of(1990, 1, 1));
        sellerUser.setRole(sellerRole);
        sellerUser = userRepository.save(sellerUser);
        sellerToken = jwtService.generateToken(sellerUser);

        adminUser = new TblUser();
        adminUser.setName("Admin");
        adminUser.setLastName1("Publication");
        adminUser.setEmail("admin.pub.test"+ System.nanoTime()+"@ruraltest.com");
        adminUser.setPassword(passwordEncoder.encode("Test123!"));
        adminUser.setIdentification("212345678" + System.nanoTime());
        adminUser.setPhoneNumber("88003344");
        adminUser.setBirthDate(LocalDate.of(1988, 7, 20));
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        testDirection = new TblDirection();
        testDirection.setProvince("Alajuela");
        testDirection.setCanton("Grecia");
        testDirection.setDistrict("San Isidro");
        testDirection.setOtherDetails("Frente al estadio");
        testDirection = directionRepository.save(testDirection);

        testPublication = new TblPublication();
        testPublication.setTitle("Venta de novillo Holstein");
        testPublication.setSpecie("bovino");
        testPublication.setRace("Holstein");
        testPublication.setGender("Macho");
        testPublication.setWeight(450L);
        testPublication.setBirthDate(LocalDate.of(2022, 3, 10));
        testPublication.setSenasaCertificate("SEN-001");
        testPublication.setPrice(350000L);
        testPublication.setType("SALE");
        testPublication.setState("ACTIVE");
        testPublication.setCreationDate(Instant.now());
        testPublication.setDirection(testDirection);
        testPublication.setUser(sellerUser);
        testPublication = publicationRepository.save(testPublication);
    }



    @Test
    @Story("Publicaciones - Ventas")
    @Description("TC-PUB-01: GET /publications/sales debe retornar 200 con lista de ventas")
    @DisplayName("TC-PUB-01: GET /publications/sales retorna 200")
    void getAllSales_returns200WithList() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .param("page", 1)
            .param("size", 6)
        .when()
            .get("/publications/sales")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("data", notNullValue());
    }

    @Test
    @Story("Publicaciones - Subastas")
    @Description("TC-PUB-02: GET /publications/auctions debe retornar 200 con lista de subastas")
    @DisplayName("TC-PUB-02: GET /publications/auctions retorna 200")
    void getAllAuctions_returns200WithList() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .param("page", 1)
            .param("size", 6)
        .when()
            .get("/publications/auctions")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("data", notNullValue());
    }

    @Test
    @Story("Publicaciones - Filtrado")
    @Description("TC-PUB-03: GET /publications/filtered con filtros validos debe retornar 200")
    @DisplayName("TC-PUB-03: GET /publications/filtered retorna 200")
    void getFilteredPublications_withValidFilters_returns200() {
        given()
            .header("Authorization", "Bearer " + adminToken)
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

    @Test
    @Story("Publicaciones - Actualizacion")
    @Description("TC-PUB-04: PATCH /publications/{id} con publicacion existente debe retornar 200")
    @DisplayName("TC-PUB-04: PATCH /publications/{id} con ID valido retorna 200")
    void patchPublication_withExistingId_returns200() {
        given()
            .header("Authorization", "Bearer " + sellerToken)
            .contentType("application/json")
            .body(PublicationTestData.partialUpdatePayload())
        .when()
            .patch("/publications/" + testPublication.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @Story("Publicaciones - Por Usuario")
    @Description("TC-PUB-05: GET /publications/user/{userId}/publications con usuario valido retorna 200")
    @DisplayName("TC-PUB-05: GET publicaciones por usuario retorna 200")
    void getAllByUserId_withExistingUser_returns200() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
        .when()
            .get("/publications/user/" + sellerUser.getId() + "/publications")
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @Story("Publicaciones - Por Usuario")
    @Description("TC-PUB-06: GET /publications/user/{userId}/publications con usuario inexistente retorna 404")
    @DisplayName("TC-PUB-06: GET publicaciones con userId inexistente retorna 404")
    void getAllByUserId_withNonExistentUser_returns404() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
        .when()
            .get("/publications/user/99999/publications")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Story("Publicaciones - Actualizacion")
    @Description("TC-PUB-07: PATCH /publications/{id} con ID inexistente debe retornar 404")
    @DisplayName("TC-PUB-07: PATCH /publications/{id} con ID inexistente retorna 404")
    void patchPublication_withNonExistentId_returns404() {
        given()
            .header("Authorization", "Bearer " + sellerToken)
            .contentType("application/json")
            .body(PublicationTestData.partialUpdatePayload())
        .when()
            .patch("/publications/99999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
