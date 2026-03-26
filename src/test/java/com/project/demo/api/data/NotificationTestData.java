package com.project.demo.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de datos de prueba para el modulo de Notificaciones.
 *
 * Provee payloads JSON para los endpoints de /notifications,
 * separando los datos de prueba de la logica de los tests
 * (Capa de Datos de Prueba).
 */
public final class NotificationTestData {

    private NotificationTestData() {}

    public static Map<String, Object> validNotificationPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Nueva puja en tu subasta");
        payload.put("description", "Se realizo una nueva oferta en tu publicacion de ganado bovino.");
        payload.put("type", "BID");
        payload.put("state", "Active");
        return payload;
    }

    public static Map<String, Object> updateNotificationStatePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Notificacion leida");
        payload.put("description", "Descripcion actualizada.");
        payload.put("type", "BID");
        payload.put("state", "READ");
        return payload;
    }

    public static Map<String, Object> fullUpdateNotificationPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Notificacion Actualizada");
        payload.put("description", "Descripcion actualizada de la notificacion.");
        payload.put("type", "SYSTEM");
        payload.put("state", "READ");
        return payload;
    }
}
