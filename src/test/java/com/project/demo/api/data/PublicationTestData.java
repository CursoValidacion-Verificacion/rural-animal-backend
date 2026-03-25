package com.project.demo.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de datos de prueba para el módulo de Publicaciones.
 * <p>
 * Centraliza los payloads JSON para los endpoints de /publication,
 * manteniendo los datos separados de la lógica de validación
 * (Capa de Datos de Prueba).
 */
public final class PublicationTestData {

    private PublicationTestData() {}

    public static Map<String, Object> validSalePublicationPayload() {
        Map<String, Object> direction = new HashMap<>();
        direction.put("province", "Alajuela");
        direction.put("canton", "Grecia");
        direction.put("district", "San Isidro");
        direction.put("otherDetails", "Finca Los Pinos");

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Venta de novillo Holstein");
        payload.put("specie", "bovino");
        payload.put("price", 350000);
        payload.put("type", "SALE");
        payload.put("state", "ACTIVE");
        payload.put("description", "Novillo de dos años en excelentes condiciones");
        payload.put("direction", direction);
        return payload;
    }

    public static Map<String, Object> validAuctionPublicationPayload() {
        Map<String, Object> direction = new HashMap<>();
        direction.put("province", "Heredia");
        direction.put("canton", "Belén");
        direction.put("district", "La Ribera");
        direction.put("otherDetails", "Hacienda El Potrero");

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Subasta de cerdo Pietrain");
        payload.put("specie", "porcino");
        payload.put("price", 100000);
        payload.put("type", "AUCTION");
        payload.put("state", "ACTIVE");
        payload.put("description", "Cerdo de raza Pietrain, excelente reproducción");
        payload.put("minimumIncrease", 5000);
        payload.put("startDate", "2026-04-01T08:00:00");
        payload.put("endDate", "2026-04-07T18:00:00");
        payload.put("direction", direction);
        return payload;
    }
}
