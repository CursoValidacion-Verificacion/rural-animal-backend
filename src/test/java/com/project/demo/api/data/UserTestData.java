package com.project.demo.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de datos de prueba para el módulo de Usuarios.
 * <p>
 * Contiene los payloads JSON reutilizables para los endpoints de /users,
 * manteniendo los datos de prueba separados de la lógica de los tests
 * (Capa de Datos de Prueba).
 */
public final class UserTestData {

    private UserTestData() {}

    // -----------------------------------------------------------------------
    // Payloads de usuario válido
    // -----------------------------------------------------------------------

    public static Map<String, Object> validUserPayload() {
        Map<String, Object> direction = new HashMap<>();
        direction.put("province", "San José");
        direction.put("canton", "Goicoechea");
        direction.put("district", "San Francisco");
        direction.put("otherDetails", "Frente al parque");

        Map<String, Object> role = new HashMap<>();
        role.put("title", "BUYER");

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Carlos");
        payload.put("lastName1", "Rodríguez");
        payload.put("lastName2", "Vargas");
        payload.put("email", "carlos.test@ruraltest.com");
        payload.put("password", "Segura123!");
        payload.put("birthDate", "1995-03-20");
        payload.put("identification", "112345678");
        payload.put("phoneNumber", "88001122");
        payload.put("direction", direction);
        payload.put("role", role);
        return payload;
    }

    public static Map<String, Object> partialUpdatePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "CarlosActualizado");
        payload.put("lastName1", "RodríguezNuevo");
        return payload;
    }

    // -----------------------------------------------------------------------
    // Payloads con datos inválidos (escenarios negativos)
    // -----------------------------------------------------------------------

    public static Map<String, Object> userWithEmptyNamePayload() {
        Map<String, Object> payload = validUserPayload();
        payload.put("name", "");
        payload.put("email", "emptynamecreate@ruraltest.com");
        return payload;
    }

    public static Map<String, Object> userWithInvalidEmailPayload() {
        Map<String, Object> payload = validUserPayload();
        payload.put("email", "not-an-email");
        return payload;
    }

    public static Map<String, Object> userWithInvalidIdentificationPayload() {
        Map<String, Object> payload = validUserPayload();
        payload.put("identification", "000000000"); // comienza con 0, inválido
        payload.put("email", "badinput@ruraltest.com");
        return payload;
    }

    public static Map<String, Object> userWithInvalidPhonePayload() {
        Map<String, Object> payload = validUserPayload();
        payload.put("phoneNumber", "1234"); // demasiado corto
        payload.put("email", "badphone@ruraltest.com");
        return payload;
    }

    // Email que coincide con el usuario insertado en @BeforeEach del test
    public static Map<String, Object> duplicateEmailPayload() {
        Map<String, Object> payload = validUserPayload();
        payload.put("email", "carlos.user.test@ruraltest.com");
        return payload;
    }
}
