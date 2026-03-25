# Fase 2 – Automatización de Pruebas Funcionales de API

**Curso:** BISOFT-32 Calidad, Verificación y Validación de Software
**Universidad:** CENFOTEC
**Proyecto:** Rural Animal Backend
**Rama:** `feat/proyecto2`

---

## 1. Resumen ejecutivo

Se implementó un framework de automatización de pruebas funcionales de API usando **REST Assured 5.4.0** con **Spring MockMvc** sobre el proyecto Spring Boot existente. Se cubrieron **5 endpoints distintos** con **38 casos de prueba automatizados** (positivos y negativos), organizados en una arquitectura de **3 capas** que separa configuración, datos y lógica de prueba.

**Resultado de ejecución:** 125 tests totales, 0 fallos.

---

## 2. Tecnologías y herramientas

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.2.5 | Framework de la aplicación |
| Gradle | 8.8 | Gestión de dependencias y build |
| JUnit 5 | 5.10.2 | Framework de pruebas (runner) |
| REST Assured | 5.4.0 | Framework de automatización de API |
| spring-mock-mvc | 5.4.0 | Integración REST Assured con MockMvc |
| Mockito | 5.x | Mocking de dependencias |
| Allure | 2.24.0 | Reportes de ejecución |
| Git | — | Control de versiones |

> **Nota sobre Maven vs Gradle:** El proyecto ya estaba configurado con Gradle. Ambas herramientas cumplen el mismo propósito de gestión de dependencias; Gradle es equivalente funcional a Maven para este proyecto.

---

## 3. Cómo correr las pruebas

### 3.1 Requisitos previos

- Java 21 instalado
- No se necesita base de datos, Redis ni ningún servicio externo — las pruebas usan MockMvc con dependencias mockeadas.

### 3.2 Correr solo las pruebas de API (REST Assured)

```bash
./gradlew test --tests "com.project.demo.api.*"
```

### 3.3 Correr la suite completa de pruebas

```bash
./gradlew test
```

### 3.4 Correr una clase específica

```bash
./gradlew test --tests "com.project.demo.api.auth.AuthApiTest"
./gradlew test --tests "com.project.demo.api.user.UserApiTest"
./gradlew test --tests "com.project.demo.api.notification.NotificationApiTest"
./gradlew test --tests "com.project.demo.api.publication.PublicationApiTest"
./gradlew test --tests "com.project.demo.api.veterinary.VeterinaryAppointmentApiTest"
```

### 3.5 Generar reporte Allure

```bash
./gradlew allureReport
```

El reporte se genera en: `build/reports/allure-report/allureReport/index.html`

### 3.6 Ver reporte HTML básico de JUnit

Después de correr los tests:

```
build/reports/tests/test/index.html
```

---

## 4. Arquitectura implementada

Se aplicó una arquitectura de **3 capas** separadas, alineada con el principio de separación de responsabilidades:

```
src/test/java/com/project/demo/
│
├── api/                                ← NUEVA: Pruebas funcionales REST Assured
│   ├── config/                         ← CAPA 1: Configuración
│   │   ├── RestAssuredConfig.java      ← Base reutilizable con specs JWT
│   │   └── SecurityMockBeans.java      ← Beans de seguridad compartidos
│   │
│   ├── data/                           ← CAPA 2: Datos de prueba
│   │   ├── AuthTestData.java
│   │   ├── UserTestData.java
│   │   ├── NotificationTestData.java
│   │   └── PublicationTestData.java
│   │
│   ├── auth/                           ← CAPA 3: Lógica de pruebas
│   │   └── AuthApiTest.java
│   ├── user/
│   │   └── UserApiTest.java
│   ├── publication/
│   │   └── PublicationApiTest.java
│   ├── notification/
│   │   └── NotificationApiTest.java
│   └── veterinary/
│       └── VeterinaryAppointmentApiTest.java
│
├── logic/                              ← EXISTENTE: Pruebas unitarias (servicio/lógica)
└── rest/                               ← EXISTENTE: Pruebas unitarias (controllers con Mockito)
```

### Descripción de cada capa

#### Capa 1 — Configuración (`api/config/`)

**`SecurityMockBeans.java`**
Clase base abstracta que declara los `@MockBean` necesarios para que Spring Security no bloquee las peticiones en los tests `@WebMvcTest`. Centraliza:
- `JwtService`
- `JwtAuthenticationFilter`
- `UserDetailsService`
- `ClientRegistrationRepository` (OAuth2)

Cada clase de test extiende esta clase, evitando duplicar 4 declaraciones en cada archivo.

**`RestAssuredConfig.java`**
Provee métodos de fábrica para construir `MockMvcRequestSpecification` con tokens JWT ya configurados (para tests futuros que requieran autenticación real).

#### Capa 2 — Datos de prueba (`api/data/`)

Clases con métodos estáticos que retornan `Map<String, Object>` con los payloads JSON. Esto:
- Elimina duplicación de datos entre casos de prueba
- Hace los datos fácilmente mantenibles en un solo lugar
- Permite reutilizar el mismo payload con pequeñas variaciones

```java
// Ejemplo de uso en un test
.body(AuthTestData.signupWithWeakPasswordPayload())
```

#### Capa 3 — Lógica de pruebas (`api/{modulo}/`)

Clases `@WebMvcTest` con `@AutoConfigureMockMvc(addFilters = false)` que:
- Inicializan `RestAssuredMockMvc.mockMvc(mockMvc)` en `@BeforeEach`
- Declaran `@MockBean` para los repositorios/servicios del controller
- Definen los casos de prueba con la sintaxis `given/when/then` de REST Assured

---

## 5. Endpoints cubiertos y casos de prueba

### 5.1 `/auth` — Autenticación y Registro (9 casos)

| ID | Nombre | Tipo | HTTP | Entrada | Status esperado |
|---|---|---|---|---|---|
| TC-AUTH-01 | Login con credenciales válidas retorna token | Positivo | POST /auth/login | email + password correctos | 200 |
| TC-AUTH-02 | Registro con datos válidos retorna 200 | Positivo | POST /auth/signup | datos completos y válidos | 200 |
| TC-AUTH-03 | Confirmar email con ID válido | Positivo | GET /auth/emailConfirm/1 | ID de usuario existente | 200 |
| TC-AUTH-04 | Login con usuario inexistente retorna 401 | Negativo | POST /auth/login | email que no existe | 401 |
| TC-AUTH-05 | Registro con email inválido retorna 400 | Negativo | POST /auth/signup | email sin formato válido | 400 |
| TC-AUTH-06 | Registro con contraseña débil retorna 400 | Negativo | POST /auth/signup | contraseña sin mayúscula/número/símbolo | 400 |
| TC-AUTH-07 | Registro con menor de edad retorna 400 | Negativo | POST /auth/signup | fecha de nacimiento < 18 años | 400 |
| TC-AUTH-08 | Registro con email duplicado retorna 409 | Negativo | POST /auth/signup | email ya registrado en BD | 409 |
| TC-AUTH-09 | Confirmar email con ID inexistente retorna 400 | Negativo | GET /auth/emailConfirm/999 | ID que no existe | 400 |

### 5.2 `/users` — Gestión de Usuarios (8 casos)

| ID | Nombre | Tipo | HTTP | Entrada | Status esperado |
|---|---|---|---|---|---|
| TC-USER-01 | Crear usuario con datos válidos retorna 200 | Positivo | POST /users | payload completo válido | 200 |
| TC-USER-02 | Actualización parcial de usuario existente | Positivo | PATCH /users/1 | nombre y apellido nuevos | 200 |
| TC-USER-03 | Filtrar usuarios por keyword retorna lista | Positivo | GET /users/filter?keyword=Carlos | keyword de búsqueda | 200 |
| TC-USER-04 | Crear usuario con nombre vacío retorna 400 | Negativo | POST /users | nombre = "" | 400 |
| TC-USER-05 | Crear usuario con email inválido retorna 400 | Negativo | POST /users | email sin @ | 400 |
| TC-USER-06 | Crear usuario con identificación inválida retorna 400 | Negativo | POST /users | cédula que empieza en 0 | 400 |
| TC-USER-07 | Actualizar usuario con ID inexistente retorna 404 | Negativo | PATCH /users/9999 | ID que no existe | 404 |
| TC-USER-08 | Crear usuario con email duplicado retorna 409 | Negativo | POST /users | email ya registrado | 409 |

### 5.3 `/publications` — Publicaciones (7 casos)

| ID | Nombre | Tipo | HTTP | Entrada | Status esperado |
|---|---|---|---|---|---|
| TC-PUB-01 | Listar ventas retorna 200 con lista | Positivo | GET /publications/sales | page=1, size=6 | 200 |
| TC-PUB-02 | Listar subastas retorna 200 con lista | Positivo | GET /publications/auctions | page=1, size=6 | 200 |
| TC-PUB-03 | Publicaciones filtradas retorna 200 | Positivo | GET /publications/filtered | type, search, sort | 200 |
| TC-PUB-04 | Actualizar publicación existente retorna 200 | Positivo | PATCH /publications/1 | payload válido | 200 |
| TC-PUB-05 | Publicaciones por usuario existente retorna 200 | Positivo | GET /publications/user/1/publications | userId existente | 200 |
| TC-PUB-06 | Publicaciones de usuario inexistente retorna 404 | Negativo | GET /publications/user/9999/publications | userId que no existe | 404 |
| TC-PUB-07 | Actualizar publicación inexistente retorna 404 | Negativo | PATCH /publications/9999 | ID que no existe | 404 |

### 5.4 `/notifications` — Notificaciones (8 casos)

| ID | Nombre | Tipo | HTTP | Entrada | Status esperado |
|---|---|---|---|---|---|
| TC-NOT-01 | Obtener notificaciones de usuario existente | Positivo | GET /notifications/1 | userId existente | 200 |
| TC-NOT-02 | Crear notificación para usuario existente | Positivo | POST /notifications/1 | payload válido | 201 |
| TC-NOT-03 | Actualización completa de notificación | Positivo | PUT /notifications/1 | payload completo | 200 |
| TC-NOT-04 | Actualización parcial (PATCH) de notificación | Positivo | PATCH /notifications/1 | state=READ | 200 |
| TC-NOT-05 | Eliminar notificación existente | Positivo | DELETE /notifications/1 | ID existente | 200 |
| TC-NOT-06 | Notificaciones de usuario inexistente retorna 404 | Negativo | GET /notifications/9999 | userId que no existe | 404 |
| TC-NOT-07 | Crear notificación para usuario inexistente retorna 404 | Negativo | POST /notifications/9999 | userId que no existe | 404 |
| TC-NOT-08 | Eliminar notificación inexistente retorna 404 | Negativo | DELETE /notifications/9999 | ID que no existe | 404 |

### 5.5 `/veterinary_appointments` — Citas Veterinarias (6 casos)

| ID | Nombre | Tipo | HTTP | Entrada | Status esperado |
|---|---|---|---|---|---|
| TC-VET-01 | Obtener citas del usuario autenticado | Positivo | GET /veterinary_appointments | usuario autenticado | 200 |
| TC-VET-02 | Consultar disponibilidad con fechas válidas | Positivo | GET /veterinary_appointments/availability | startDate + endDate | 200 |
| TC-VET-03 | Crear cita con datos válidos | Positivo | POST /veterinary_appointments | appointmentDate, reason, veterinaryId | 200 |
| TC-VET-04 | Listar todas las citas (admin) | Positivo | GET /veterinary_appointments/all | — | 200 |
| TC-VET-05 | Error de servicio al obtener citas retorna 500 | Negativo | GET /veterinary_appointments | servicio lanza RuntimeException | 500 |
| TC-VET-06 | Crear cita con horario no disponible retorna 400 | Negativo | POST /veterinary_appointments | servicio lanza ResponseStatusException | 400 |

---

## 6. Cumplimiento de requisitos del proyecto

| Requisito | Requerido | Implementado |
|---|---|---|
| Lenguaje Java | Sí | ✅ Java 21 |
| Framework de pruebas JUnit o TestNG | Sí | ✅ JUnit 5 |
| Framework REST Assured | Sí | ✅ REST Assured 5.4.0 |
| Gestión de dependencias Maven/Gradle | Sí | ✅ Gradle 8.8 (equivalente) |
| Control de versiones Git | Sí | ✅ rama `feat/proyecto2` |
| Arquitectura en capas | Sí | ✅ config / data / tests |
| Separación lógica / configuración / datos | Sí | ✅ 3 paquetes distintos |
| Validaciones claras y mantenibles | Sí | ✅ `statusCode()` + `body()` |
| Código reutilizable sin duplicidad | Sí | ✅ `SecurityMockBeans`, `*TestData` |
| 5 endpoints distintos automatizados | Mín. 5 | ✅ 5 endpoints |
| 20 casos de prueba mínimo | Mín. 20 | ✅ 38 casos REST Assured |
| Escenarios positivos y negativos | Sí | ✅ ambos en cada endpoint |
| Validación de status code | Sí | ✅ `.statusCode(...)` |
| Validación de body response | Sí | ✅ `.body("data", notNullValue())` |

---

## 7. Diferencia entre las pruebas existentes y las nuevas

El proyecto ya contaba con 87 pruebas **unitarias** (Mockito). Las nuevas 38 pruebas son **funcionales de API**:

| Aspecto | Prueba Unitaria (existente) | Prueba API REST Assured (nueva) |
|---|---|---|
| ¿Qué prueba? | Lógica interna de un método Java | Contrato HTTP del endpoint completo |
| ¿Hay petición HTTP? | No — llamada directa al método | Sí — `POST /auth/login` |
| ¿Valida status code HTTP? | No | Sí — `.statusCode(200)` |
| ¿Valida body JSON? | No | Sí — `.body("token", equalTo(...))` |
| Nivel en la pirámide | Unidad | API / Integración |
| Requiere Spring context? | No (`@ExtendWith(MockitoExtension)`) | Sí (`@WebMvcTest`) |

---

## 8. Estructura de un caso de prueba tipo

```java
@Test
@Story("Autenticación")
@Description("TC-AUTH-01: Login con credenciales válidas debe retornar 200 y un token JWT")
@DisplayName("TC-AUTH-01: Login válido retorna 200 y token")
void login_withValidCredentials_returns200AndToken() {
    // Arrange — configurar mocks
    when(userRepository.findByEmail("andres@gmail.com")).thenReturn(Optional.of(testUser));
    when(jwtService.generateToken(any(TblUser.class))).thenReturn("jwt-token-mock");

    // Act + Assert — petición HTTP y validaciones
    RestAssuredMockMvc.given()
            .contentType("application/json")
            .body(AuthTestData.validLoginPayload())   // dato reutilizable
    .when()
            .post("/auth/login")                      // petición HTTP real
    .then()
            .statusCode(HttpStatus.OK.value())        // valida status
            .body("token", equalTo("jwt-token-mock")); // valida body JSON
}
```

---

## 9. Archivos modificados / creados

### Modificados
- `build.gradle` — se agregaron 4 dependencias de REST Assured

### Creados (nuevos)

```
src/test/java/com/project/demo/api/
├── config/
│   ├── RestAssuredConfig.java
│   └── SecurityMockBeans.java
├── data/
│   ├── AuthTestData.java
│   ├── UserTestData.java
│   ├── NotificationTestData.java
│   └── PublicationTestData.java
├── auth/
│   └── AuthApiTest.java         (9 tests)
├── user/
│   └── UserApiTest.java         (8 tests)
├── publication/
│   └── PublicationApiTest.java  (7 tests)
├── notification/
│   └── NotificationApiTest.java (8 tests)
└── veterinary/
    └── VeterinaryAppointmentApiTest.java (6 tests)
```

**Total nuevas pruebas REST Assured: 38**
**Total suite completa (incluyendo unitarias existentes): 125 tests, 0 fallos**
