package com.project.demo.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de datos de prueba para el módulo de Notificaciones.
 * <p>
 * Provee payloads JSON para los endpoints de /notifications,
 * separando los datos de prueba de la lógica de los tests
 * (Capa de Datos de Prueba).
 */
public final class NotificationTestData {

    private NotificationTestData() {}

    public static Map<String, Object> validNotificationPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Nueva puja en tu subasta");
        payload.put("description", "Se realizó una nueva oferta en tu publicación de ganado bovino.");
        payload.put("state", "UNREAD");
        return payload;
    }

    public static Map<String, Object> updateNotificationStatePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("state", "READ");
        return payload;
    }

    public static Map<String, Object> fullUpdateNotificationPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Notificación Actualizada");
        payload.put("description", "Descripción actualizada de la notificación.");
        payload.put("state", "READ");
        return payload;
    }
}
