package com.project.demo.rest.publication;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.photo.TblPhoto;
import com.project.demo.logic.entity.photo.TblPhotoRepository;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicationRestControllerTest {

    @Mock
    private TblPublicationRepository publicationRepository;

    @Mock
    private TblDirectionRepository directionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TblPhotoRepository photoRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PublicationRestController publicationController;

    private TblPublication testPublication;
    private TblUser testUser;
    private TblDirection testDirection;
    private List<TblPhoto> testPhotos;

    /**
     * Inicializa los objetos de prueba necesarios para los métodos de prueba en la clase.
     * Configura instancias de TblDirection, TblUser, TblPhoto y TblPublication con valores predeterminados
     * para ser utilizados en diferentes pruebas, asegurando que se inicie un estado consistente antes de cada ensayo.
     */
    @BeforeEach
    void setUp() {

        testDirection = new TblDirection();
        testDirection.setId(1L);
        testDirection.setProvince("San José");
        testDirection.setCanton("Central");
        testDirection.setDistrict("Catedral");

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testPhotos = new ArrayList<>();
        TblPhoto photo = new TblPhoto();
        photo.setId(1L);
        photo.setUrl("test-url");
        testPhotos.add(photo);

        testPublication = new TblPublication();
        testPublication.setId(1L);
        testPublication.setTitle("Venta de ganado");
        testPublication.setSpecie("Bovino");
        testPublication.setPrice(1000L);
        testPublication.setType("SALE");
        testPublication.setState("ACTIVE");
        testPublication.setDirection(testDirection);
        testPublication.setUser(testUser);
        testPublication.setPhotos(testPhotos);
    }

    @Test
    @Story("Listar Publicaciones")
    @Description("Debe retornar una lista paginada de publicaciones")
    void getAll_ShouldReturnPaginatedPublications() {

        List<TblPublication> publications = Collections.singletonList(testPublication);
        Page<TblPublication> publicationPage = new PageImpl<>(publications);
        when(publicationRepository.findAll(any(Pageable.class))).thenReturn(publicationPage);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "Página: 1\n" +
                "Tamaño: 10\n" +
                "Publicaciones de prueba: " + publications);

        ResponseEntity<?> response = publicationController.getAll(1, 10, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Cuerpo de respuesta: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publicationRepository).findAll(any(Pageable.class));
    }

    @Test
    @Story("Listar Publicaciones por Usuario")
    @Description("Debe retornar una lista paginada de publicaciones filtradas por usuario")
    void getAllByUserId_WithValidUser_ShouldReturnUserPublications() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        Page<TblPublication> publicationPage = new PageImpl<>(Collections.singletonList(testPublication));
        when(publicationRepository.findTblPublicationsByUserId(anyLong(), anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(publicationPage);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "ID de Usuario: 1\n" +
                "Página: 1\n" +
                "Tamaño: 10\n" +
                "Tipo: SALE\n" +
                "Búsqueda: search\n" +
                "Ordenamiento: date\n" +
                "Publicaciones de prueba: " + publicationPage.getContent());

        ResponseEntity<?> response = publicationController.getAllByUserId(1L, 1, 10, "SALE", "search", "date", httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Publicaciones encontradas: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publicationRepository).findTblPublicationsByUserId(anyLong(), anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @Story("Guardar Publicación")
    @Description("Debe crear una nueva publicación")
    void save_ShouldCreateNewPublication() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        when(directionRepository.save(any(TblDirection.class))).thenReturn(testDirection);
        when(publicationRepository.save(any(TblPublication.class))).thenReturn(testPublication);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "Publicación: " + testPublication + "\n" +
                "Dirección: " + testDirection + "\n" +
                "Usuario: " + testUser);

        ResponseEntity<?> response = publicationController.save(testPublication, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Publicación guardada: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(directionRepository).save(any(TblDirection.class));
        verify(publicationRepository).save(any(TblPublication.class));
    }

    @Test
    @Story("Listar Ventas")
    @Description("Debe retornar una lista paginada de ventas")
    void getAllSales_ShouldReturnPaginatedSales() {

        Page<TblPublication> salesPage = new PageImpl<>(Collections.singletonList(testPublication));
        when(publicationRepository.findAllSales(any(Pageable.class))).thenReturn(salesPage);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "Página: 1\n" +
                "Tamaño: 6\n" +
                "Ventas de prueba: " + salesPage.getContent());

        ResponseEntity<?> response = publicationController.getAllSales(1, 6, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Ventas encontradas: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publicationRepository).findAllSales(any(Pageable.class));
    }

    @Test
    @Story("Listar Subastas")
    @Description("Debe retornar una lista paginada de subastas")
    void getAllAuctions_ShouldReturnPaginatedAuctions() {
        Page<TblPublication> auctionsPage = new PageImpl<>(Collections.singletonList(testPublication));
        when(publicationRepository.findAllAuctions(any(Pageable.class))).thenReturn(auctionsPage);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "Página: 1\n" +
                "Tamaño: 6\n" +
                "Subastas de prueba: " + auctionsPage.getContent());

        ResponseEntity<?> response = publicationController.getAllAuctions(1, 6, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Subastas encontradas: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publicationRepository).findAllAuctions(any(Pageable.class));
    }

    @Test
    @Story("Filtrar Publicaciones")
    @Description("Debe retornar una lista paginada de publicaciones según los filtros aplicados")
    void getFilteredPublications_ShouldReturnFilteredResults() {
        Page<TblPublication> filteredPage = new PageImpl<>(Collections.singletonList(testPublication));
        when(publicationRepository.findFilteredPublications(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(filteredPage);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

        Allure.addAttachment("Datos de Entrada",
                "Página: 1\n" +
                "Tamaño: 10\n" +
                "Tipo: SALE\n" +
                "Especie: bovino\n" +
                "Ordenamiento: price\n" +
                "Publicaciones filtradas: " + filteredPage.getContent());

        ResponseEntity<?> response = publicationController.getFilteredPublications(1, 10, "SALE", "bovino", "price", httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Publicaciones filtradas encontradas: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(publicationRepository).findFilteredPublications(anyString(), anyString(), anyString(), any(Pageable.class));
    }
}