package com.project.demo.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de datos de prueba para el módulo de Autenticación.
 * <p>
 * Centraliza todos los payloads y valores de entrada utilizados en los
 * tests de los endpoints /auth/login y /auth/signup, evitando duplicación
 * de datos entre los casos de prueba (Capa de Datos de Prueba).
 */
public final class AuthTestData {

    private AuthTestData() {}

    // -----------------------------------------------------------------------
    // Credenciales válidas
    // -----------------------------------------------------------------------

    public static Map<String, Object> validLoginPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "andres.test.auth@ruraltest.com");
        payload.put("password", "Test123!");
        return payload;
    }

    // -----------------------------------------------------------------------
    // Credenciales inválidas / escenarios negativos
    // -----------------------------------------------------------------------

    public static Map<String, Object> wrongPasswordPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "andres@gmail.com");
        payload.put("password", "WrongPass99!");
        return payload;
    }

    public static Map<String, Object> nonExistentUserPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "noexiste@test.com");
        payload.put("password", "Test123!");
        return payload;
    }

    public static Map<String, Object> emptyCredentialsPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "");
        payload.put("password", "");
        return payload;
    }

    // -----------------------------------------------------------------------
    // Registro de usuarios
    // -----------------------------------------------------------------------

    public static Map<String, Object> validSignupPayload() {
        Map<String, Object> direction = new HashMap<>();
        direction.put("province", "San José");
        direction.put("canton", "Central");
        direction.put("district", "Catedral");
        direction.put("otherDetails", "Casa 1");

        Map<String, Object> role = new HashMap<>();
        role.put("title", "BUYER");

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Juan");
        payload.put("lastName1", "Pérez");
        payload.put("lastName2", "Mora");
        payload.put("email", "juan.test.signup@ruraltest.com");
        payload.put("password", "Test123!");
        payload.put("birthDate", "2000-01-15");
        payload.put("identification", "112345678");
        payload.put("phoneNumber", "88001122");
        payload.put("direction", direction);
        payload.put("role", role);
        return payload;
    }

    public static Map<String, Object> signupWithInvalidEmailPayload() {
        Map<String, Object> payload = validSignupPayload();
        payload.put("email", "correo-invalido");
        return payload;
    }

    public static Map<String, Object> signupWithWeakPasswordPayload() {
        Map<String, Object> payload = validSignupPayload();
        payload.put("email", "weakpass@ruraltest.com");
        payload.put("password", "weak");
        return payload;
    }

    public static Map<String, Object> signupWithMinorAgePayload() {
        Map<String, Object> payload = validSignupPayload();
        payload.put("email", "minor@ruraltest.com");
        payload.put("birthDate", "2015-06-01"); // Menos de 18 anos
        return payload;
    }

    // Payload con email que ya existe en BD (mismo que el usuario insertado en @BeforeEach)
    public static Map<String, Object> duplicateEmailSignupPayload() {
        Map<String, Object> payload = validSignupPayload();
        payload.put("email", "andres.test.auth@ruraltest.com");
        return payload;
    }
}
