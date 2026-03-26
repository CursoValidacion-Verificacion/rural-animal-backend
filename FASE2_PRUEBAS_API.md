# Fase 2 - Automatizacion de Pruebas Funcionales de API

**Curso:** BISOFT-32 Calidad, Verificacion y Validacion de Software  
**Universidad:** CENFOTEC  
**Proyecto:** Rural Animal Backend  
**Rama:** `feat/proyecto2`

---

## 1. Resumen ejecutivo

Se implemento una suite de pruebas funcionales de API con **REST Assured 5.4.0** sobre el backend Spring Boot existente. La ejecucion final cubre **5 endpoints distintos** con **38 casos automatizados** entre escenarios positivos y negativos.

La implementacion final **no usa `@WebMvcTest` ni `MockMvc` como estrategia principal**. Las pruebas corren con **`@SpringBootTest(webEnvironment = RANDOM_PORT)`**, levantando la aplicacion en un puerto aleatorio y enviando peticiones HTTP reales contra `http://localhost:{port}`.

El entorno de prueba utiliza:

- **MariaDB real** en Docker
- **Redis real** en Docker
- **Mocks puntuales** para dependencias externas no requeridas en la validacion del contrato HTTP, como correo y servicios ligados a Google Calendar

**Resultado validado de ejecucion:** 125 tests totales, 0 fallos.  
**Resultado de la suite API:** 38 tests, 0 fallos.

---

## 2. Tecnologias y herramientas

| Tecnologia | Version | Rol |
|---|---|---|
| Java | 21 | Lenguaje de programacion |
| Spring Boot | 3.2.5 | Framework de la aplicacion |
| Gradle | 8.8 | Build y dependencias |
| JUnit 5 | 5.10.2 | Runner de pruebas |
| REST Assured | 5.4.0 | Pruebas HTTP de API |
| Spring Boot Test | 3.2.5 | Levantar contexto y servidor embebido |
| Spring Security Test | 6.x | Soporte de seguridad en pruebas |
| Mockito | 5.x | Mocking puntual de dependencias externas |
| Allure | 2.24.0 | Reportes de ejecucion |
| Docker | - | MariaDB y Redis para entorno de prueba |
| Git | - | Control de versiones |

> **Nota sobre Maven vs Gradle:** El proyecto ya estaba configurado con Gradle. Se mantuvo esa herramienta sin migrar el build.

---

## 3. Como correr las pruebas

### 3.1 Requisitos previos

- Java 21 instalado
- Docker Desktop o Docker Engine disponible
- Contenedores de MariaDB y Redis levantados

### 3.2 Levantar servicios requeridos

```bash
docker compose up -d
```

### 3.3 Correr solo las pruebas de API

```bash
./gradlew test --tests "com.project.demo.api.*"
```

### 3.4 Correr la suite completa

```bash
./gradlew test
```

### 3.5 Correr una clase especifica

```bash
./gradlew test --tests "com.project.demo.api.auth.AuthApiTest"
./gradlew test --tests "com.project.demo.api.user.UserApiTest"
./gradlew test --tests "com.project.demo.api.notification.NotificationApiTest"
./gradlew test --tests "com.project.demo.api.publication.PublicationApiTest"
./gradlew test --tests "com.project.demo.api.veterinary.VeterinaryAppointmentApiTest"
```

### 3.6 Generar reporte Allure

```bash
./gradlew allureReport
```

El reporte se genera en:

```text
build/reports/allure-report/allureReport/index.html
```

### 3.7 Ver reporte HTML de JUnit

```text
build/reports/tests/test/index.html
```

---

## 4. Arquitectura implementada

La implementacion final quedo organizada en tres bloques practicos:

```text
src/test/java/com/project/demo/
|
|- api/
|  |- data/                           <- Datos reutilizables de prueba
|  |  |- AuthTestData.java
|  |  |- UserTestData.java
|  |  |- NotificationTestData.java
|  |  `- PublicationTestData.java
|  |
|  |- auth/
|  |  `- AuthApiTest.java
|  |- user/
|  |  `- UserApiTest.java
|  |- publication/
|  |  `- PublicationApiTest.java
|  |- notification/
|  |  `- NotificationApiTest.java
|  `- veterinary/
|     `- VeterinaryAppointmentApiTest.java
|
|- logic/                            <- Pruebas unitarias existentes de servicios
`- rest/                             <- Pruebas unitarias existentes de controllers
```

Adicionalmente, la configuracion del entorno de pruebas vive en:

```text
src/test/resources/application-test.properties
```

### 4.1 Configuracion de entorno

La suite usa:

- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- `@ActiveProfiles("test")`
- `RestAssured.baseURI = "http://localhost"`
- `RestAssured.port = @LocalServerPort`

Esto permite validar:

- Routing real
- Serializacion JSON real
- Seguridad Spring real
- Persistencia real contra MariaDB
- Integracion real con Redis en el contexto

### 4.2 Datos de prueba

Las clases en `api/data/` centralizan payloads JSON reutilizables con `Map<String, Object>`, reduciendo duplicacion y facilitando mantenimiento.

### 4.3 Logica de prueba

Cada clase de prueba:

- levanta el backend real en puerto aleatorio
- inserta datos base en MariaDB para el escenario
- ejecuta requests HTTP reales con REST Assured
- valida `statusCode()` y, cuando aplica, fragmentos del body JSON

---

## 5. Alcance funcional de la suite

### 5.1 `/auth` - Autenticacion y registro (9 casos)

| ID | Nombre | Tipo | HTTP | Status esperado |
|---|---|---|---|---|
| TC-AUTH-01 | Login con credenciales validas retorna token | Positivo | POST `/auth/login` | 200 |
| TC-AUTH-02 | Registro con datos validos retorna 200 | Positivo | POST `/auth/signup` | 200 |
| TC-AUTH-03 | Confirmar email con ID valido | Positivo | GET `/auth/emailConfirm/{id}` | 200 |
| TC-AUTH-04 | Login con usuario inexistente retorna 401 | Negativo | POST `/auth/login` | 401 |
| TC-AUTH-05 | Registro con email invalido retorna 400 | Negativo | POST `/auth/signup` | 400 |
| TC-AUTH-06 | Registro con contrasena debil retorna 400 | Negativo | POST `/auth/signup` | 400 |
| TC-AUTH-07 | Registro con menor de edad retorna 400 | Negativo | POST `/auth/signup` | 400 |
| TC-AUTH-08 | Registro con email duplicado retorna 409 | Negativo | POST `/auth/signup` | 409 |
| TC-AUTH-09 | Confirmar email con ID inexistente retorna 400 | Negativo | GET `/auth/emailConfirm/{id}` | 400 |

### 5.2 `/users` - Gestion de usuarios (8 casos)

| ID | Nombre | Tipo | HTTP | Status esperado |
|---|---|---|---|---|
| TC-USER-01 | Crear usuario con datos validos retorna 200 | Positivo | POST `/users` | 200 |
| TC-USER-02 | Actualizacion parcial de usuario existente | Positivo | PATCH `/users/{id}` | 200 |
| TC-USER-03 | Filtrar usuarios por keyword retorna lista | Positivo | GET `/users/filter` | 200 |
| TC-USER-04 | Crear usuario con nombre vacio retorna 400 | Negativo | POST `/users` | 400 |
| TC-USER-05 | Crear usuario con email invalido retorna 400 | Negativo | POST `/users` | 400 |
| TC-USER-06 | Crear usuario con identificacion invalida retorna 400 | Negativo | POST `/users` | 400 |
| TC-USER-07 | Actualizar usuario con ID inexistente retorna 404 | Negativo | PATCH `/users/{id}` | 404 |
| TC-USER-08 | Crear usuario con email duplicado retorna 409 | Negativo | POST `/users` | 409 |

### 5.3 `/publications` - Publicaciones (7 casos)

| ID | Nombre | Tipo | HTTP | Status esperado |
|---|---|---|---|---|
| TC-PUB-01 | Listar ventas retorna 200 con lista | Positivo | GET `/publications/sales` | 200 |
| TC-PUB-02 | Listar subastas retorna 200 con lista | Positivo | GET `/publications/auctions` | 200 |
| TC-PUB-03 | Publicaciones filtradas retorna 200 | Positivo | GET `/publications/filtered` | 200 |
| TC-PUB-04 | Actualizar publicacion existente retorna 200 | Positivo | PATCH `/publications/{id}` | 200 |
| TC-PUB-05 | Publicaciones por usuario existente retorna 200 | Positivo | GET `/publications/user/{userId}/publications` | 200 |
| TC-PUB-06 | Publicaciones de usuario inexistente retorna 404 | Negativo | GET `/publications/user/{userId}/publications` | 404 |
| TC-PUB-07 | Actualizar publicacion inexistente retorna 404 | Negativo | PATCH `/publications/{id}` | 404 |

### 5.4 `/notifications` - Notificaciones (8 casos)

| ID | Nombre | Tipo | HTTP | Status esperado |
|---|---|---|---|---|
| TC-NOT-01 | Obtener notificaciones de usuario existente | Positivo | GET `/notifications/{userId}` | 200 |
| TC-NOT-02 | Crear notificacion para usuario existente | Positivo | POST `/notifications/{userId}` | 201 |
| TC-NOT-03 | Actualizacion completa de notificacion | Positivo | PUT `/notifications/{id}` | 200 |
| TC-NOT-04 | Actualizacion parcial de notificacion | Positivo | PATCH `/notifications/{id}` | 200 |
| TC-NOT-05 | Eliminar notificacion existente | Positivo | DELETE `/notifications/{id}` | 200 |
| TC-NOT-06 | Notificaciones de usuario inexistente retorna 404 | Negativo | GET `/notifications/{userId}` | 404 |
| TC-NOT-07 | Crear notificacion para usuario inexistente retorna 404 | Negativo | POST `/notifications/{userId}` | 404 |
| TC-NOT-08 | Eliminar notificacion inexistente retorna 404 | Negativo | DELETE `/notifications/{id}` | 404 |

### 5.5 `/veterinary_appointments` - Citas veterinarias (6 casos)

| ID | Nombre | Tipo | HTTP | Status esperado |
|---|---|---|---|---|
| TC-VET-01 | Obtener citas del usuario autenticado | Positivo | GET `/veterinary_appointments` | 200 |
| TC-VET-02 | Consultar disponibilidad con fechas validas | Positivo | GET `/veterinary_appointments/availability` | 200 |
| TC-VET-03 | Crear cita con datos validos | Positivo | POST `/veterinary_appointments` | 200 |
| TC-VET-04 | Listar todas las citas como admin | Positivo | GET `/veterinary_appointments/all` | 200 |
| TC-VET-05 | Error de servicio al obtener citas retorna 500 | Negativo | GET `/veterinary_appointments` | 500 |
| TC-VET-06 | Crear cita con horario no disponible retorna 400 | Negativo | POST `/veterinary_appointments` | 400 |

---

## 6. Manejo de dependencias externas en pruebas

La suite final deja **solo MariaDB y Redis como dependencias reales**. El resto se resolvio asi:

- `EmailService` se mockea en pruebas donde se dispararian correos
- `VeterinaryAppointmentService` se mockea en el modulo veterinario porque depende de Google Calendar
- OAuth2 se inicializa con propiedades dummy en el perfil `test` para permitir que Spring Security levante el contexto

### 6.1 Uso de seguridad real

En la implementacion final:

- `publication` usa JWT real en los requests protegidos
- `veterinary` usa JWT real para buyer y admin
- `auth`, `users` y `notifications` prueban endpoints que el backend expone sin autenticacion obligatoria en la configuracion actual

---

## 7. Diferencia frente a las pruebas existentes

El proyecto ya contaba con pruebas unitarias de servicios y controllers bajo `logic/` y `rest/`. La nueva suite de `api/` sube un nivel y valida el contrato HTTP del backend en ejecucion.

| Aspecto | Pruebas existentes | Nueva suite API |
|---|---|---|
| Nivel principal | Unidad | API / integracion funcional |
| Ejecucion HTTP real | No | Si |
| Servidor embebido | No | Si |
| Base de datos real | No | Si, MariaDB Docker |
| Redis real en contexto | No | Si |
| Seguridad real | No | Si, segun endpoint |
| Dependencias externas | Mockeadas | Solo se mockea lo externo no necesario |

---

## 8. Estado final validado

### Suite de API

- 38 tests ejecutados
- 0 fallos

### Suite completa del proyecto

- 125 tests ejecutados
- 0 fallos

---

## 9. Archivos modificados o creados para esta fase

### Modificados

- `build.gradle`
- `src/main/java/com/project/demo/rest/auth/AuthRestController.java`
- `src/main/java/com/project/demo/rest/publication/PublicationRestController.java`

### Creados o mantenidos en la suite de API

```text
src/test/java/com/project/demo/api/
|- data/
|  |- AuthTestData.java
|  |- UserTestData.java
|  |- NotificationTestData.java
|  `- PublicationTestData.java
|- auth/
|  `- AuthApiTest.java
|- user/
|  `- UserApiTest.java
|- publication/
|  `- PublicationApiTest.java
|- notification/
|  `- NotificationApiTest.java
`- veterinary/
   `- VeterinaryAppointmentApiTest.java
```

### Configuracion de pruebas

- `src/test/resources/application-test.properties`

---

## 10. Conclusiones

La fase quedo implementada como una **suite funcional de API con servidor embebido y requests HTTP reales**, no como un slice de controller con `MockMvc`. Esto la acerca mas a un escenario de integracion real del backend y permite validar:

- contrato HTTP
- seguridad
- serializacion
- persistencia real
- comportamiento observable de los endpoints

El resultado final cumple con el requerimiento minimo de **5 endpoints** y **20 casos**, superandolo con **38 casos automatizados** y una suite total estable de **125 pruebas exitosas**.
