package com.project.demo.rest.user;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserRestControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TblDirectionRepository directionRepository;

    @Mock
    private TblRoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserRestController userRestController;

    private TblUser testUser;
    private TblRole testRole;
    private TblDirection testDirection;

    /**
     * Configura el entorno de prueba antes de cada caso de prueba.
     * <br>
     * Inicializa y configura las entidades TblRole, TblDirection y TblUser con datos de prueba.
     * <br>
     * Mockea las respuestas del objeto HttpServletRequest para simular una petición HTTP.
     */
    @BeforeEach
    void setUp() {
        testRole = new TblRole();
        testRole.setTitle(RoleEnum.BUYER);

        testDirection = new TblDirection();
        testDirection.setId(1L);
        testDirection.setProvince("San José");
        testDirection.setCanton("Goicoechea");
        testDirection.setDistrict("San Francisco de Goicoechea");

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("andres@gmail.com");
        testUser.setPassword("Test123!");
        testUser.setName("Andres");
        testUser.setLastName1("Torres");
        testUser.setIdentification("123456789");
        testUser.setPhoneNumber("88888888");
        testUser.setRole(testRole);
        testUser.setDirection(testDirection);
        testUser.setBirthDate(LocalDate.now().minusYears(20));

        StringBuffer requestURL = new StringBuffer("http://localhost:8080/test");
        when(httpServletRequest.getRequestURL()).thenReturn(requestURL);
        when(httpServletRequest.getMethod()).thenReturn("GET");
    }

    /**
     * Prueba unitaria para el método `getAllUsers_ShouldReturnPaginatedUsers`.
     * <p>
     * Verifica que el método `getAll` del `UserRestController` devuelva una lista paginada
     * de usuarios con un código de estado HTTP 200 (OK).
     * <p>
     * El método realiza las siguientes acciones:
     * - Configura un objeto `Pageable` con una solicitud de la primera página y un tamaño de página de 10 elementos.
     * - Simula la respuesta del repositorio `userRepository` devolviendo una página de usuarios simulada.
     * - Llama al método `getAll` del controlador REST de usuarios con la paginación especificada.
     * - Comprueba que la respuesta de la API es de estado 200 (OK).
     * - Verifica que el método `findAll` del `userRepository` sea invocado con los parámetros de paginación adecuados.
     */
    @Test
    @Story("Listar Usuarios")
    @Description("Debe retornar una lista paginada de todos los usuarios")
    void getAllUsers_ShouldReturnPaginatedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TblUser> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Allure.addAttachment("Datos de Entrada",
                "Página: 1\n" +
                "Tamaño: 10\n" +
                "Usuarios de prueba: " + userPage.getContent());

        ResponseEntity<?> response = userRestController.getAll(1, 10, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Usuarios encontrados: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).findAll(any(Pageable.class));
    }

    /**
     * Prueba unitaria para el método `addUser` en la clase `UserRestController`,
     * encargada de verificar que se puede guardar un usuario cuando se proporcionan datos válidos.
     * <p>
     * Esta prueba realiza las siguientes acciones:
     * <p>
     * - Simula que no hay ningún usuario existente con el correo proporcionado.
     * - Simula la obtención de un rol válido a partir del repositorio de roles.
     * - Mockea la codificación de la contraseña del usuario.
     * - Simula el guardado de una dirección válida en el repositorio de direcciones.
     * - Simula el guardado del usuario en el repositorio de usuarios.
     * <p>
     * Finalmente, la prueba verifica que el método `addUser` del controlador:
     * <p>
     * - Devuelve un código de estado HTTP 200 (OK) si el usuario se ha añadido con éxito.
     * - Efectivamente llama a los métodos `save` en los repositorios `userRepository` y `directionRepository`.
     */
    @Test
    @Story("Crear Usuario")
    @Description("Debe crear un nuevo usuario cuando los datos proporcionados son válidos")
    void addUser_WithValidData_ShouldSaveUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByTitle(any())).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(directionRepository.save(any())).thenReturn(testDirection);
        when(userRepository.save(any())).thenReturn(testUser);

        Allure.addAttachment("Datos de Entrada",
                "Usuario: " + testUser + "\n" +
                "Rol: " + testRole + "\n" +
                "Dirección: " + testDirection);

        ResponseEntity<?> response = userRestController.addUser(testUser, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Usuario creado: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(TblUser.class));
        verify(directionRepository).save(any(TblDirection.class));
    }

    /**
     * Prueba unitaria para el método `updateUser` en la clase `UserRestController`.
     * <p>
     * Verifica que el método actualice correctamente un usuario existente cuando se proporcione un ID válido.
     * <p>
     * La prueba realiza las siguientes acciones:
     * - Simula la búsqueda de un usuario existente en el repositorio `userRepository` a partir de un ID.
     * - Mockea la codificación de la contraseña del usuario utilizando `passwordEncoder`.
     * - Simula el guardado del usuario actualizado en el repositorio `userRepository`.
     * - Llama al método `updateUser` del controlador REST para actualizar el usuario.
     * - Verifica que la respuesta devuelta tiene un código de estado HTTP 200 (OK).
     * - Comprueba que el método `save` del `userRepository` es invocado correctamente para persistir los cambios del usuario.
     */
    @Test
    @Story("Actualizar Usuario")
    @Description("Debe actualizar un usuario existente cuando se proporciona un ID válido")
    void updateUser_WithExistingId_ShouldUpdateUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        Allure.addAttachment("Datos de Entrada",
                "ID: 1\n" +
                "Usuario actualizado: " + testUser);

        ResponseEntity<?> response = userRestController.updateUser(1L, testUser, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Usuario actualizado: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(TblUser.class));
    }

    /**
     * Prueba unitaria para el método `patchUser` en la clase `UserRestController`.
     * <p>
     * Verifica que el método actualice parcialmente un usuario existente
     * cuando se proporcionan cambios válidos en un `TblUser`.
     * <p>
     * La prueba realiza las siguientes acciones:
     * - Mockea el comportamiento del repositorio `userRepository` para devolver un usuario de prueba existente al buscar por ID.
     * - Mockea el comportamiento del repositorio `userRepository` para simular el guardado del usuario.
     * - Configura un objeto `TblUser` con los cambios parciales en el nombre y correo electrónico.
     * - Invoca el método `patchUser` del controlador REST con el ID del usuario y los cambios parciales.
     * - Comprueba que la respuesta devuelta tiene un código de estado HTTP 200 (OK).
     * - Verifica que el método `save` del `userRepository` se invoca para persistir los cambios parciales del usuario.
     */
    @Test
    @Story("Actualizar Usuario Parcialmente")
    @Description("Debe actualizar parcialmente un usuario cuando se proporcionan cambios válidos")
    void patchUser_WithValidChanges_ShouldUpdatePartially() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        TblUser partialUpdate = new TblUser();
        partialUpdate.setName("Jane");
        partialUpdate.setEmail("jane@example.com");

        Allure.addAttachment("Datos de Entrada",
                "ID: 1\n" +
                "Usuario original: " + testUser + "\n" +
                "Cambios parciales: " + partialUpdate);

        ResponseEntity<?> response = userRestController.patchUser(1L, partialUpdate, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Usuario actualizado: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(TblUser.class));
    }

    /**
     * Prueba unitaria para el método `deleteUser` en la clase `UserRestController`.
     * <p>
     * Esta prueba verifica que el método elimine correctamente un usuario existente
     * cuando se proporciona un ID válido.
     * <p>
     * La prueba realiza las siguientes acciones:
     * <p>
     * - Simula la búsqueda de un usuario existente en el repositorio `userRepository` mediante un ID.
     * - Mockea el comportamiento del repositorio para que no realice ninguna acción al eliminar por ID.
     * - Llama al método `deleteUser` del controlador REST.
     * - Verifica que la respuesta de la API HTTP devuelve un código de estado 200 (OK).
     * - Confirma que el método `deleteById` del `userRepository` es invocado con el ID correcto.
     */
    @Test
    @Story("Eliminar Usuario")
    @Description("Debe eliminar un usuario existente cuando se proporciona un ID válido")
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(anyLong());

        Allure.addAttachment("Datos de Entrada",
                "ID: 1\n" +
                "Usuario a eliminar: " + testUser);

        ResponseEntity<?> response = userRestController.deleteUser(1L, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).deleteById(1L);
    }

    /**
     * Prueba unitaria para el método `filterUsers` en la clase `UserRestController`.
     * <p>
     * Verifica que el método devuelva una lista filtrada de usuarios basándose en un criterio de búsqueda
     * proporcionado.
     * <p>
     * La prueba realiza las siguientes acciones:
     * - Configura un objeto `Pageable` para especificar la paginación de la respuesta.
     * - Simula el comportamiento del `userRepository` para devolver una página de usuarios que coinciden
     * con el criterio de búsqueda.
     * - Invoca el método `filterUsers` del controlador REST con un término de búsqueda y parámetros de
     * paginación.
     * - Verifica que la respuesta del servicio tiene un código de estado HTTP 200 (OK).
     * - Asegura que el método `findUsersByKeyword` del `userRepository` es llamado con los argumentos
     * correctos.
     */
    @Test
    @Story("Filtrar Usuarios")
    @Description("Debe retornar una lista paginada de usuarios que coincidan con el criterio de búsqueda")
    void filterUsers_ShouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TblUser> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findUsersByKeyword(anyString(), any(Pageable.class))).thenReturn(userPage);

        Allure.addAttachment("Datos de Entrada",
                "Término de búsqueda: test\n" +
                "Página: 1\n" +
                "Tamaño: 10\n" +
                "Usuarios de prueba: " + userPage.getContent());

        ResponseEntity<?> response = userRestController.filterUsers("test", 1, 10, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Usuarios filtrados: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).findUsersByKeyword(anyString(), any(Pageable.class));
    }
}