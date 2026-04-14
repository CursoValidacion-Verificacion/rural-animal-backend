package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.EmailService;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AuthRestControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TblRoleRepository roleRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Spy
    @InjectMocks
    private AuthRestController authRestController;

    private TblUser testUser;
    private TblRole testRole;

    /**
     * Método de configuración que se ejecuta antes de cada test en la clase de prueba AuthRestControllerTest.
     * <p>
     * Este método inicializa los objetos necesarios para las pruebas, incluyendo instancias de TblRole y TblUser,
     * y configura sus propiedades relevantes. Además, inyecta dependencias simuladas en el controlador
     * authRestController mediante ReflectionTestUtils.
     * <p>
     * Implementa las siguientes acciones:
     * - Crea una instancia de TblRole con el título establecido a BUYER.
     * - Crea una instancia de TblUser, configurando identificador, correo, contraseña, rol y fecha de nacimiento.
     * - Inyecta dependencias simuladas para userRepository, passwordEncoder, emailService y roleRepository
     * en el controlador authRestController.
     */
    @BeforeEach
    void setUp() {
        testRole = new TblRole();
        testRole.setTitle(RoleEnum.BUYER);

        testUser = new TblUser();
        testUser.setId(1L);
        testUser.setEmail("andres@gmail.com");
        testUser.setPassword("Test123!");
        testUser.setRole(testRole);
        testUser.setBirthDate(LocalDate.now().minusYears(20));

        ReflectionTestUtils.setField(authRestController, "userRepository", userRepository);
        ReflectionTestUtils.setField(authRestController, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authRestController, "emailService", emailService);
        ReflectionTestUtils.setField(authRestController, "roleRepository", roleRepository);
    }

    /**
     * Prueba unitaria para el método de autenticación en el controlador AuthRestController.
     * Este test verifica que cuando se intenta iniciar sesión con credenciales válidas,
     * el sistema retorna un token de autenticación válido.
     * <p>
     * Procedimiento:
     * - Se simula la existencia de un usuario en el repositorio de usuarios a través del método findByEmail.
     * - Se mockea el proceso de autenticación exitoso mediante el servicio de autenticación.
     * - Se simula la generación de un token de JWT mediante el servicio de JWT.
     * - Se invoca el método de autenticación en el controlador utilizando el usuario de prueba.
     * <p>
     * Verificaciones:
     * - Se asegura que la respuesta tiene un código de estado HTTP OK.
     * - Se verifica que el cuerpo de la respuesta no es nulo e incluye un token.
     * - Se confirma que el token en el cuerpo de la respuesta coincide con el token generado.
     */
    @Test
    @Story("Autenticación de Usuario")
    @Description("Debe retornar un token cuando se proporcionan credenciales válidas")
    void whenLoginWithValidCredentials_thenReturnToken() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(authenticationService.authenticate(any(TblUser.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(TblUser.class))).thenReturn("test-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        Allure.addAttachment("Datos de Entrada",
                "Usuario: " + testUser + "\n" +
                "Email: " + testUser.getEmail() + "\n" +
                "Password: " + testUser.getPassword());

        ResponseEntity<LoginResponse> response = authRestController.authenticate(testUser);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode() + "\n" +
                "Token generado: " + response.getBody().getToken() + "\n" +
                "Tiempo de expiración: " + 3600L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
    }

    /**
     * Prueba unitaria para el método de registro de usuarios en el controlador AuthRestController.
     * Este test verifica que cuando se intenta registrar un usuario con un correo electrónico
     * ya existente en el sistema, la aplicación responde con un código de estado HTTP de conflicto (409).
     * <p>
     * Procedimiento:
     * - Se simula la existencia de un usuario en la base de datos con el mismo correo utilizando el repositorio de usuarios.
     * - Se invoca el método registerUser del controlador con el usuario de prueba.
     * - Se verifica que el estado de la respuesta HTTP es CONFLICT, indicando que el registro no fue exitoso debido a que
     * el correo ya está registrado.
     * - También se verifica que el método save del repositorio no es llamado, asegurando que no se intenta guardar
     * un usuario nuevo con un correo duplicado.
     */
    @Test
    @Story("Registro de Usuario")
    @Description("Debe retornar conflicto cuando se intenta registrar un email ya existente")
    void whenSignupWithExistingEmail_thenConflict() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Allure.addAttachment("Datos de Entrada",
                "Usuario: " + testUser + "\n" +
                "Email existente: " + testUser.getEmail());

        ResponseEntity<?> response = authRestController.registerUser(testUser, httpServletRequest);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userRepository, never()).save(any(TblUser.class));
    }

    /**
     * Prueba unitaria para el método EmailConfirm del controlador AuthRestController.
     * Este test verifica que cuando se confirma un correo electrónico utilizando un ID válido,
     * se obtiene una respuesta exitosa.
     * <p>
     * El método simula la búsqueda de un usuario existente en el repositorio y asegura
     * que se llama al método save del repositorio de usuarios para guardar los cambios
     * relacionados con la confirmación del correo.
     * <p>
     * Se comprueba que el código de estado de la respuesta HTTP es OK, lo cual
     * indica que la operación se ha completado con éxito.
     */
    @Test
    @Story("Confirmación de Email")
    @Description("Debe confirmar exitosamente el email cuando se proporciona un ID válido")
    void whenEmailConfirmWithValidId_thenSuccess() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(TblUser.class))).thenReturn(testUser);

        Allure.addAttachment("Datos de Entrada",
                "ID de usuario: 1\n" +
                "Usuario: " + testUser);

        ResponseEntity<?> response = authRestController.EmailConfirm(1L);

        Allure.addAttachment("Datos de Salida",
                "Código de estado: " + response.getStatusCode());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(TblUser.class));
    }

    /**
     * Prueba unitaria para el método isValidEmail del controlador AuthRestController.
     * Este método de prueba evalúa la validez de direcciones de correo electrónico
     * utilizadas en el sistema.
     * <p>
     * Se realizan dos aserciones para verificar el comportamiento esperado:
     * - Una dirección de correo electrónico correctamente formada "test@example.com"
     * debe ser considerada válida y devolver true.
     * - Una dirección de correo electrónico incorrectamente formada "invalid-email"
     * debe ser considerada inválida y devolver false.
     */
    @Test
    @Story("Validación de Email")
    @Description("Debe validar correctamente el formato de las direcciones de email")
    void testIsValidEmail() {
        Allure.addAttachment("Datos de Entrada",
                "Email válido: test@example.com\n" +
                "Email inválido: invalid-email");

        boolean validResult = authRestController.isValidEmail("test@example.com");
        boolean invalidResult = authRestController.isValidEmail("invalid-email");

        Allure.addAttachment("Datos de Salida",
                "Resultado email válido: " + validResult + "\n" +
                "Resultado email inválido: " + invalidResult);

        assertTrue(validResult);
        assertFalse(invalidResult);
    }

    /**
     * Prueba unitaria para el método isValidPassword del controlador AuthRestController.
     * Este método asegura que isValidPassword evalúa correctamente la robustez de una contraseña.
     * <p>
     * Se verifican dos casos:
     * - Una contraseña fuerte "Test123!" debe ser considerada válida y devolver true.
     * - Una contraseña débil "weak" debe ser considerada inválida y devolver false.
     */
    @Test
    @Story("Validación de Contraseña")
    @Description("Debe validar correctamente la fortaleza de las contraseñas")
    void testIsValidPassword() {
        Allure.addAttachment("Datos de Entrada",
                "Contraseña válida: Test123!\n" +
                "Contraseña inválida: weak");

        boolean validResult = authRestController.isValidPassword("Test123!");
        boolean invalidResult = authRestController.isValidPassword("weak");

        Allure.addAttachment("Datos de Salida",
                "Resultado contraseña válida: " + validResult + "\n" +
                "Resultado contraseña inválida: " + invalidResult);

        assertTrue(validResult);
        assertFalse(invalidResult);
    }

    /**
     * Prueba unitaria para verificar el método isAdult del controlador AuthRestController.
     * Este método de prueba asegura que el método isAdult identifica correctamente si una persona
     * es mayor de edad o no, basándose en su fecha de nacimiento.
     * <p>
     * El método isAdult debe devolver true si la edad calculada a partir de la fecha de nacimiento
     * es 18 años o más, y false si es menor. Se realizan dos aserciones:
     * - La primera verifica que una persona de 20 años es considerada adulta.
     * - La segunda verifica que una persona de 17 años no es considerada adulta.
     */
    @Test
    @Story("Validación de Edad")
    @Description("Debe validar correctamente si una persona es mayor de edad")
    void testIsAdult() {
        LocalDate adultDate = LocalDate.now().minusYears(20);
        LocalDate minorDate = LocalDate.now().minusYears(17);

        Allure.addAttachment("Datos de Entrada",
                "Fecha de nacimiento adulto: " + adultDate + "\n" +
                "Fecha de nacimiento menor: " + minorDate);

        boolean adultResult = authRestController.isAdult(adultDate);
        boolean minorResult = authRestController.isAdult(minorDate);

        Allure.addAttachment("Datos de Salida",
                "Resultado adulto: " + adultResult + "\n" +
                "Resultado menor: " + minorResult);

        assertTrue(adultResult);
        assertFalse(minorResult);
    }

    /**
     * Verifica que el método formatIdentification de AuthRestController funcione correctamente
     * al formatear un número de identificación de 9 dígitos.
     * Este método de prueba comprueba que al proporcionar un número de identificación
     * de 9 dígitos como "123456789", el método devuelva el número en formato
     * "1-2345-6789".
     * Se utiliza assertEquals para validar si el resultado del método bajo prueba
     * coincide con el formato esperado.
     */
    @Test
    @Story("Formateo de Identificación")
    @Description("Debe formatear correctamente un número de identificación")
    void testFormatIdentification() {
        String input = "123456789";

        Allure.addAttachment("Datos de Entrada",
                "Identificación sin formato: " + input);

        String result = authRestController.formatIdentification(input);

        Allure.addAttachment("Datos de Salida",
                "Identificación formateada: " + result);

        assertEquals("1-2345-6789", result);
    }

    /**
     * Verifica que el método formatPhoneNumber de AuthRestController funcione correctamente
     * al formatear un número de teléfono de 8 dígitos.
     * <br>
     * Este método de prueba comprueba que al proporcionar un número de teléfono
     * de 8 dígitos como "12345678", el método devuelva el número en formato
     * "1234-5678".
     * <br>
     * Se utiliza assertEquals para validar si el resultado del método bajo prueba
     * coincide con el formato esperado.
     */
    @Test
    @Story("Formateo de Número Telefónico")
    @Description("Debe formatear correctamente un número de teléfono")
    void testFormatPhoneNumber() {
        String input = "12345678";

        Allure.addAttachment("Datos de Entrada",
                "Número telefónico sin formato: " + input);

        String result = authRestController.formatPhoneNumber(input);

        Allure.addAttachment("Datos de Salida",
                "Número telefónico formateado: " + result);

        assertEquals("1234-5678", result);
    }
}