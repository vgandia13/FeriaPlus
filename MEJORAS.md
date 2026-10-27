# Mejoras recomendadas — Feria+

> Recomendaciones que **no son fallos**: la aplicación funciona, pero conviene hacerlas (deuda técnica, dependencias, documentación, tests y funcionalidades nuevas).
> Los fallos están en `ERRORES.md`. La numeración es común a los dos archivos y se conserva porque los puntos se citan entre sí.
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`

## Cómo leer este archivo

- Cada apartado indica la **rama** donde conviene hacerlo, normalmente junto con los fallos de esa misma rama en `ERRORES.md`.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| Apartado | Rama | Puntos |
|---|---|---|
| Tests y CI | `test/unitarios` y siguientes | 5 |
| Eventos | `fix/eventos` | 1 |
| Configuración | `fix/config` | 3 |
| Frontend | `fix/frontend-ui` | 5 |
| Limpieza y dependencias | `chore/limpieza-spring-boot-4` | 11 |
| Panel de organizador | `feature/panel-organizador` | 1 |
| Opcionales para el TFG | sin rama | 6 |

---

## 🧪 Tests y CI (en este orden)

- [ ] **4.15 🟢 Tests unitarios que no cubre la integración**
  - **Problema:** los 13 tests de integración de 4.1 solo usan `"Bearer tokenfalso"` (ni siquiera tiene forma de JWT) y solo prueban los caminos de error. Hay reglas de `JwtUtils` y `UsuarioService` que ningún test toca.
  - **Solución:**
    - **A)** Por orden de importancia:
      1. `B/security/JwtUtils.java` (JUnit solo, sin Spring ni Mockito: `new JwtUtils()` + `ReflectionTestUtils.setField(..., "JWT_SECRET", ...)`): el token guarda el id del usuario (2.16); un token bien formado pero caducado da `validateToken == false` (2.15; construirlo con `Jwts.builder()` y `setExpiration(new Date(0))`); un token firmado con otra clave da `false`.
      2. `UsuarioService.actualizarPerfil` (Mockito): asignar `ROLE_ADMIN` lanza `NoAutorizadoException` y nunca llama a `save`; editar a un admin lanza `NoAutorizadoException`.
      3. `UsuarioService.actualizarPerfilPorUsername` (Mockito): con la contraseña actual correcta se guarda la nueva codificada; un email ya registrado lanza `EmailRegistradoException`.
      4. `UsuarioService.eliminarUsuario` (Mockito): sin datos sí llama a `delete`; con reservas, reseñas o asistencias lanza `UsuarioConDatosException`.
    - No merece la pena: `obtenerDatosDashboard` (solo copia datos) ni los handlers de `GlobalExceptionHandler` (ya los cubre la integración).

- [ ] **4.14 🟢 El frontend no tiene tests**
  - **Problema:** no hay framework de tests en `frontend/`. Los arreglos 2.10, 2.11 y 2.22 (Navbar tras el registro, interceptor de 401, `ProtectedRoute` sin `usuario`) solo se han probado a mano.
  - **Solución:**
    - **A)** Vitest + React Testing Library (`npm i -D vitest @testing-library/react @testing-library/jest-dom jsdom`) y un script `"test": "vitest"`. Vitest y no Jest porque reutiliza la configuración de Vite (alias `@`, TypeScript) sin Babel. Primeros tests: `ProtectedRoute` (sin token → `/login`; token sin `usuario` en ruta con rol → `/`), `Navbar` (`usuario` sin token → no muestra "Mi panel") y el interceptor de `apiService.ts` (401 en `/auth/login` no redirige).

- [ ] **5.7 Tests con Testcontainers + `@ServiceConnection`** (Boot 3.1) y **`RestTestClient`** (Spring 7) — tests contra un MySQL real en contenedor (ver 4.1).

- [ ] **5.18 Tests end-to-end con Playwright** — flujos completos en un navegador real contra backend + frontend (login correcto/incorrecto, registro, acceso a `/admin` según el rol). Playwright mejor que Cypress: multi-navegador, paralelo sin pagar y `npx playwright codegen` para grabar tests. Hacer después de 4.1 y 4.14.

- [ ] **5.19 Pipeline de CI con GitHub Actions** — `.github/workflows/ci.yml` que en cada push y PR ejecute `mvnw verify` (backend) y `npm run lint`, `npm run build` y los tests (frontend). Los runners de GitHub traen Docker, así que también sirve para Testcontainers (5.7). Con protección de rama en `main` para no fusionar con el CI en rojo.

---

## 2️⃣ Rama `fix/eventos`

- [ ] **4.5 🟢 Campo `reseñas` con `ñ`**
  - **Problema:** `B/model/Evento.java:59`. Los identificadores con `ñ` dan problemas en JSON, JPQL y en algunos editores.
  - **Solución:**
    - **A)** Renombrar a `resenas` (y su getter/setter y los usos en `EventoService`).

---

## 3️⃣ Rama `fix/config`

- [ ] **5.4 🟢 Desactivar Swagger en producción** (va con 3.1)
  - **Problema:** springdoc 3 avisa al arrancar de que la documentación de la API está expuesta.
  - **Solución:**
    - **A)** `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false` en el perfil de producción (o al revés: activarlos solo en `dev`).

- [ ] **3.3 🟢 `ddl-auto=update` en la configuración principal**
  - **Problema:** `application.properties:13`. Hibernate modifica el esquema en cada arranque y nunca borra columnas o restricciones antiguas (ver 2.7).
  - **Solución:**
    - **A)** `update` solo en `dev`; `validate` en producción.
    - **B)** Migraciones con Flyway (`spring-boot-starter-flyway`) y `validate`. Más robusto, pero añade trabajo.

- [ ] **3.9 🟢 Restos de configuración**
  - **Problema:** en `pom.xml`, `lombok.version` (`:31`) existe pero la versión se repite a mano (`:64`, `:139`).
  - **Solución:**
    - **A)** Usar `${lombok.version}` en `:64` y `:139`.
    - **B)** Si se usan Testcontainers (5.7): quitar `h2` y `application-test.properties`.

---

## 4️⃣ Rama `fix/frontend-ui`

- [ ] **4.9 🟢 El token se guarda dos veces**
  - **Problema:** `AuthService.login` (`authService.ts:11`) y `LoginPage.tsx:34`.
  - **Solución:**
    - **A)** Guardarlo solo en un sitio, en `AppContext`/`login()` o en el servicio, y quitar el otro.

- [ ] **4.2 🟢 `console.log` sueltos**
  - **Problema:** `EventPage.tsx:61`, `LoginPage.tsx:36` y `RegisterPage.tsx:57`.
  - **Solución:**
    - **A)** Borrarlos.
    - **B)** Regla `no-console: warn` en `eslint.config.js` para que no vuelvan.

- [ ] **4.6 🟢 Bundle JS de 659 kB**
  - **Problema:** todo va en un único archivo, incluido Leaflet, que solo se usa en `EventPage`.
  - **Solución:**
    - **A)** `React.lazy` + `Suspense` por rutas en `App.tsx` (como mínimo `EventPage` y `AdminPage`).

- [ ] **2.4 🟢 El frontend llama a endpoints inexistentes**
  - **Problema:** `frontend/src/services/userService.ts:7` (`GET /perfil/me`; solo existe `PUT` → 405) y `eventoService.ts:35` (`DELETE /eventos/{id}`). Ninguna de las dos funciones se usa: hoy es código muerto.
  - **Solución:**
    - **A)** Quitar las dos llamadas (y `EventoService.create`, que tampoco se usa).
    - **B)** Crear `GET /perfil/me` y `DELETE /eventos/{id}` en el backend, si se van a usar con el panel de organizador.

- [ ] **3.7 🟢 Dependencias mal clasificadas o sin uso**
  - **Problema:** `shadcn` (CLI) está en `dependencies` (`frontend/package.json:30`). `next-themes` (`:22`) solo lo importa `ui/sonner.tsx`, que nadie usa.
  - **Solución:**
    - **A)** `npm i -D shadcn`; borrar `ui/sonner.tsx` y `npm uninstall next-themes`.

---

## 5️⃣ Rama `chore/limpieza-spring-boot-4`

- [ ] **4.10 🟢 Código muerto en el backend**
  - **Problema:** `B/service/CategoriaService.java` y `B/exception/CategoriaNoEncontradaException.java` no tienen controlador ni handler; `EventoService.listarPorCategoria`, `JwtUtils.getRolesFromToken` y `UsuarioRepository.findByNombre` no se usan.
  - **Solución:**
    - **A)** Borrarlos.
    - **B)** Usarlos: `GET /api/categorias` + filtro por categoría en Eventos (y handler 404 para la excepción).

- [ ] **3.6 🟢 El README describe mal MapStruct**
  - **Problema:** `README.md:19` dice que MapStruct mapea entidades↔DTOs en general; solo se usa para `Ubicacion` (la línea 68 ya lo dice bien).
  - **Solución:**
    - **A)** Corregir la línea 19.

- [ ] **5.3 🟢 `spring.jpa.open-in-view=false`**
  - **Problema:** está activo por defecto (consultas a la BD durante la serialización, N+1 de ~4 consultas por evento). ⚠️ Desactivarlo sin más **rompe la app**: `EventoService.obtenerTodos`/`obtenerPorId` no son transaccionales y `mapToDTO` (`:77-80`, `:94`) toca relaciones LAZY.
  - **Solución:**
    - **A)** Primero `@Transactional(readOnly = true)` en esos métodos, luego `open-in-view=false`.
    - **B)** Además: `@EntityGraph` o `JOIN FETCH` en `EventoRepository` para resolver el N+1.

- [ ] **5.14 🟢 jjwt 0.11.5 → 0.13.0**
  - **Problema:** `B/security/JwtUtils.java` usa API obsoleta (`parserBuilder()`, `setSigningKey`, `parseClaimsJws`, `SignatureAlgorithm.HS256`).
  - **Solución:**
    - **A)** Subir la versión y cambiar a `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` y `.signWith(key)`.

- [ ] **5.1 🟢 Filtrar `FACTOR_PASSWORD` del JWT** (Security 7)
  - **Problema:** `B/security/JwtUtils.java:27-29` emite `"roles":"ROLE_ORGANIZADOR,FACTOR_PASSWORD"`. Hoy no tiene efecto (nadie lee el claim).
  - **Solución:**
    - **A)** Filtrar las authorities que empiezan por `ROLE_`.
    - **B)** Quitar el claim `roles` (junto con `getRolesFromToken`, ver 4.10).

- [ ] **5.2 🟢 Quitar `spring.jpa.database-platform`** (Hibernate 7)
  - **Problema:** `application.properties:11`; Hibernate detecta MySQL solo y avisa `HHH90000025` al arrancar.
  - **Solución:**
    - **A)** Borrar la línea.

- [ ] **5.15 🟢 Lombok 1.18.32 → 1.18.48 y MapStruct 1.5.5 → 1.6.x**
  - **Problema:** versiones fijadas a mano en `pom.xml:31,64,107,124,134`.
  - **Solución:**
    - **A)** Subirlas en la propiedad y usar `${...}` en `annotationProcessorPaths` (junto con 3.9).

- [ ] **5.16 🟢 `spring-boot-starter-web` → `spring-boot-starter-webmvc`**
  - **Problema:** `pom.xml:40`; Boot 4 renombró el starter y el antiguo está obsoleto.
  - **Solución:**
    - **A)** Cambiar el `artifactId`.

- [ ] **5.17 🟢 Dependencias npm desactualizadas** (sin vulnerabilidades)
  - **Problema:** `lucide-react` 1.8 → 1.62, `radix-ui` 1.4 → 1.8.1, `react`/`react-dom` 19.2 → 19.3.1, `tailwindcss` 4.2 → 4.3.3, etc.
  - **Solución:**
    - **A)** `npm update` en `frontend/` (dentro del rango, bajo riesgo) + lint + build.
    - **B)** Las mayores (`eslint` 10, `typescript` 7, `react-day-picker` 10, `concurrently` 10 en la raíz), cada una aparte y probándola.

- [ ] **5.8 🟢 Null-safety con JSpecify** (Spring 7)
  - **Problema:** nada avisa de NPE como las de 2.3 (`getRol().name()`) y 2.8 (`fecha` null) antes de ejecutar.
  - **Solución:**
    - **A)** `@NullMarked` en `package-info.java` de cada paquete y `@Nullable` donde proceda.

- [ ] **5.5 🟢 `ProblemDetail` (RFC 9457) para los errores** (si no se hizo en la rama 1)
  - **Problema:** `B/exception/GlobalExceptionHandler.java` devuelve `Map<String,Object>` con un formato propio.
  - **Solución:**
    - **A)** Devolver `ProblemDetail` (`{type,title,status,detail}`) y que el frontend lea `detail` (ayuda con 4.4).

---

## 6️⃣ Rama `feature/panel-organizador`

- [ ] **4.3 Rutas de organizador pendientes**
  - **Problema:** `frontend/src/App.tsx:31` (TODO). No hay ninguna pantalla para que un organizador cree o edite eventos.
  - **Solución:**
    - **A)** Ruta `/organizador` protegida con `allowedRoles={["ROLE_ORGANIZADOR"]}`, que liste sus eventos y permita crear/editar (requiere el `PUT` de 1.6 y la asignación del organizador de 1.4).

---

## ➕ Opcionales para ampliar el TFG (sin rama)

- [ ] **5.6 Soporte de Docker Compose en desarrollo** (Boot 3.1) — opción A de 3.1.
- [ ] **5.9 Versionado de API nativo** (Spring 7) — útil si se cambia el contrato (p. ej. al quitar `organizadorId` en 1.4).
- [ ] **5.13 DTOs como `record`** (Java 16+) — los de solo lectura (`UsuarioResponseDTO`, `DashboardDTO`, `LoginRequestDTO`).
- [ ] **5.11 Hilos virtuales** (Boot 3.2, Java 21) — `spring.threads.virtual.enabled=true`.
- [ ] **5.12 Logs estructurados en JSON** (Boot 3.4) — `logging.structured.format.console=ecs`.
- [ ] **5.10 MFA / segundo factor** (Security 7) — `@EnableMultiFactorAuthentication` para `ROLE_ADMIN`.
