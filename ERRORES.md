# Errores a corregir — Feria+

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Auditoría: 2026-10-26. Última revisión: 2026-10-26 (2ª auditoría completa, tras reinstalar dependencias).
> Git: `main` = `367a07c` (PR #6, Spring Boot 4.1.2, tag `v1.0.0`). `fix/seguridad-auth` = `20b5706` (1.1 + `main` fusionada), sincronizada con `origin`. Cambios sin commit: ver tabla.
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`

## Estado del entorno

| Comprobación | Resultado |
|---|---|
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME` | ✅ reinstalado el 2026-10-26 con `winget install EclipseAdoptium.Temurin.21.JDK` (no estaba instalado). `JAVA_HOME` a nivel de máquina; abre una terminal nueva para que lo vea. |
| Dependencias | ✅ `npm ci` en raíz y `frontend/`, `mvnw dependency:go-offline` en `backend/` |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main` y `fix/seguridad-auth` |
| `.\mvnw.cmd verify` (backend, `fix/seguridad-auth`) | ✅ compila — ⚠️ 0 tests |
| `docker compose up --build` (MySQL 8 + backend) | ✅ probado en `chore/spring-boot-4`: 20 endpoints con el mismo resultado que en 3.4.1 |
| `npm run lint` (frontend) | ✅ 0 errores |
| `npm run build` (frontend) | ✅ (aviso de bundle > 500 kB) |
| `npm audit` raíz y frontend | ✅ 0 vulnerabilidades |
| `npm outdated` | ⚠️ ver 5.17 |
| Pendiente de commit | Quitado `@Repository` (redundante en interfaces de Spring Data) de `CategoriaRepository`, `EventoRepository` y `UsuarioRepository`. Compila bien; falta commit. |

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

---

## 🔴 1. Crítico — seguridad

- [ ] **1.5 (Decisión)** `ROLE_ORGANIZADOR` sigue siendo autoasignable en el registro (`frontend/src/pages/RegisterPage.tsx:162`). Decidir si se mantiene o pasa a asignarlo un admin.

- [ ] **1.2 Rol `null` rompe el login** — `B/security/UserDetailsServiceImpl.java:31` hace `getRol().name()` → NPE. 🟡 Parcial: el registro ya exige rol (1.1). Falta 2.3 y revisar usuarios existentes en BD con rol `null`.
- [ ] **1.3 Secretos versionados** — `backend/src/main/resources/application.properties:9` (password MySQL) y `:20` (`jwt.secret`). → Leer de variables de entorno (`${JWT_SECRET}`), pasarlas en `docker-compose.yml`, dejar de versionar el archivo (`git rm --cached`) y **rotar el secreto** (ya está en el historial).
- [ ] **1.4 `organizadorId` viene del cliente** — `B/controller/EventoController.java:44` + `B/service/EventoService.java:118-119`. → Tomar el organizador de `Authentication` (email del token).

## 🟠 2. Bugs

- [ ] **2.1 Errores de auth devuelven 500** — `B/exception/GlobalExceptionHandler.java:41` captura todo `RuntimeException`, incluidos `BadCredentialsException` (→ 401) y `AccessDeniedException` (→ 403). Añadir handlers específicos.
- [ ] **2.2 Borrar usuario con reseñas/reservas/asistencias → 500** por FK — `B/service/UsuarioService.java:159`. Decidir: bloquear con 409 o borrar dependencias.
- [ ] **2.3 Admin puede dejar rol `null`** — `B/service/UsuarioService.java:152`. `PUT` de `B/controller/AdminController.java:54` no lleva `@Valid`, así que el `@NotNull` nuevo del DTO no se aplica. → Validar el rol en el servicio. **No** basta con `@Valid`: `frontend/src/pages/AdminEditForm.tsx:48` no envía `password` y el `@NotBlank` del DTO rompería la edición desde el panel.
- [ ] **2.4 Frontend llama a endpoints inexistentes** — `frontend/src/services/userService.ts:7` (`GET /perfil/me`) y `frontend/src/services/eventoService.ts:35` (`DELETE /eventos/{id}`). Crear el endpoint o quitar la llamada.
- [ ] **2.5 Crear evento con ubicación nueva falla** (`TransientPropertyValueException`) — `B/service/EventoService.java:112`. Buscar la ubicación por id en el repositorio en vez de mapear una entidad suelta.
- [ ] **2.6 Borrar una ubicación borra sus eventos** — `B/model/Ubicacion.java:34` (`CascadeType.ALL`).
- [ ] **2.7 `Reserva` ↔ `Puesto` inconsistente** — `B/model/Reserva.java:27` usa `@OneToOne`, pero `Puesto` declara `@OneToMany reservas`. Cambiar a `@ManyToOne`.
- [ ] **2.8 NPE en dashboard** si `fecha` es null — `B/service/UsuarioService.java:95` (en `EventoService.java:76` ya está protegido; copiar ese patrón).
- [ ] **2.9 Mapa claro con tema "system" oscuro** — `frontend/src/pages/EventPage.tsx:43` compara `theme === "dark"`; usar el tema resuelto. Lo mismo con el marcador en `:142` (`theme === "light"`): con "system" pinta el icono oscuro sobre el mapa claro.
- [ ] **2.10 Tras registrarse, la Navbar muestra sesión iniciada sin token** — `frontend/src/pages/RegisterPage.tsx:56` guarda la respuesta del registro en `localStorage.usuario`. Al recargar, `AppContext` la lee y `frontend/src/components/Navbar.tsx:76` decide por `usuario` (no por `isLogged`): aparecen "Mi panel" y "Salir" sin estar logueado. → Quitar ese `setItem` (el login ya guarda el usuario). De paso, `authService.ts:17` tipa la respuesta como `UsuarioRegistro`, pero el backend devuelve `UsuarioResponseDTO`.
- [ ] **2.11 Un login fallido recargará la página al arreglar 2.1** — `frontend/src/services/apiService.ts:41` trata cualquier 401 como sesión caducada y hace `window.location.href = '/login'`. Cuando credenciales incorrectas devuelvan 401 (2.1), la página se recargará y se perderá el toast "Error en las credenciales". → Excluir `/auth/*` de esa redirección. Hacerlo junto con 2.1.
- [ ] **2.12 Panel admin: el botón "Limpiar" no hace nada y el diálogo no se cierra al guardar** — `frontend/src/pages/AdminEditForm.tsx:109` usa `type="reset"`, que no afecta a inputs controlados por estado; `frontend/src/pages/AdminPage.tsx:273` deja el diálogo abierto tras guardar (lo dice el propio comentario).
- [ ] **2.13 Registro: el selector de rol aparenta estar vacío** — `frontend/src/pages/RegisterPage.tsx:155`: el `Select` no recibe `value`, así que muestra "Selecciona un rol" aunque el estado ya es `ROLE_VISITANTE`. → `value={rol}`.
- [ ] **2.14 Botón » (última página) solo funciona pulsando el icono** — `frontend/src/pages/AdminPage.tsx:318-320` y `frontend/src/pages/EventsPage.tsx:178-180`: el `onClick` está en `<ChevronsRight>`, no en el `<Button>`. Pulsar el borde del botón o usar el teclado (Enter/Espacio) no hace nada. → Mover el `onClick` al `Button`.
- [ ] **2.15 Token caducado bloquea las páginas públicas** — `B/security/JwtAuthenticationFilter.java:45,49` responde 401 si llega un token inválido/caducado, aunque la ruta sea `permitAll` (`GET /api/eventos`). Junto con `apiService.ts:41`, quien vuelve tras 24 h es enviado a `/login` al abrir Home o Eventos. → Si el token no es válido, seguir la cadena sin autenticar y dejar que `SecurityConfig` decida.
- [ ] **2.16 Cambiar el email desde `PUT /perfil/me` invalida la sesión** — `B/service/UsuarioService.java:120-125`: el JWT lleva el email antiguo como `subject`, así que la siguiente petición da 401 (`JwtAuthenticationFilter.java:68`). Además `B/controller/UsuarioController.java:31` no lleva `@Valid`: acepta `""` o un email mal formado. → Devolver un token nuevo al cambiar el email (o usar el id como `subject`) y validar el email. (El frontend aún no usa este endpoint.)
- [ ] **2.17 Dashboard se queda en "Cargando…" si falla la petición** — `frontend/src/pages/DashboardPage.tsx:21-26` no tiene `try/catch`: error → promesa rechazada sin manejar y pantalla de carga infinita. De paso, `formatDate` (`:6-15`) añade hora y minutos, y las fechas de evento (`"2026-10-31"`, solo día) salen como `31/10/2026, 02:00`.

## 🟡 3. Configuración / entorno

- [ ] **3.1** `npm run dev:backend` fuera de Docker no conecta: `application.properties:7` apunta al host `mysql`. → Perfil `dev` con `localhost:3307`.
- [ ] **3.2** `docker-compose.yml` no pasa `JWT_SECRET` (va con 1.3).
- [ ] **3.3** `ddl-auto=update` en config principal — `application.properties:13`.
- [ ] **3.4** `health.show-details=always` expone detalles internos — `application.properties:17`.
- [ ] **3.5** Seeder: contraseñas `123456` y `admin@feriaplus.com` es ORGANIZADOR — `B/bootstrap/DataSeeder.java:44-51`.
- [ ] **3.6** README dice que MapStruct mapea entidades↔DTOs; solo se usa para `Ubicacion`.
- [ ] **3.7** `shadcn` (CLI) está en `dependencies` — `frontend/package.json:30` → mover a `devDependencies`.
- [ ] **3.8 Falta `.dockerignore` en `frontend/`** — el `Dockerfile` hace `npm install` y luego `COPY . .`, que copia el `node_modules/` de Windows encima del instalado en Linux. Los paquetes con binarios nativos (rolldown de Vite 8, `@tailwindcss/oxide`, lightningcss) pueden fallar en el `npm run build` del contenedor. → `.dockerignore` con `node_modules` y `dist`, y `npm ci` en vez de `npm install`.
- [ ] **3.9 Restos de configuración** — `backend/src/main/resources/application-test.properties` (H2) está versionado pero no hay tests que lo usen, y `h2` (scope `runtime`) acaba dentro del jar de producción → pasar `h2` a scope `test` cuando se haga 4.1. En `pom.xml` la propiedad `lombok.version` está definida pero la versión se repite a mano (`:64`, `:124`).

## 🟢 4. Calidad / deuda

- [ ] **4.1 Cero tests.** Empezar por tests de integración de auth/registro (cubren 1.1, 1.2, 2.1). `spring-boot-starter-test` no está en `pom.xml`; hay que añadirlo.
- [ ] **4.2** `console.log` sueltos — `EventPage.tsx:61`, `LoginPage.tsx:36`, `RegisterPage.tsx:57`.
- [ ] **4.3** Rutas de organizador pendientes — `frontend/src/App.tsx:31` (TODO).
- [ ] **4.4** Toasts duplicados: el interceptor (`frontend/src/services/apiService.ts:48`) y las páginas muestran ambos un error.
- [ ] **4.5** Campo `reseñas` con `ñ` — `B/model/Evento.java:59`.
- [ ] **4.6** Bundle JS de 650 kB → `React.lazy` por rutas (Leaflet solo en `EventPage`).
- [ ] **4.7 Accesibilidad**:
  - Los `<label>` de `LoginPage.tsx:64,74` y `RegisterPage.tsx:85,95,105,128,154` no están asociados a su input (falta `htmlFor`/`id`).
  - Los botones de mostrar/ocultar contraseña (`LoginPage.tsx:84`, `RegisterPage.tsx:114,140`) no tienen `aria-label`.
  - Los botones « » de paginación (`AdminPage.tsx:294,318`, `EventsPage.tsx:154,178`) tampoco tienen `aria-label`.
  - `AdminPage.tsx:257` pone un texto en `aria-describedby`, que espera un id.
- [ ] **4.8 Tipos TS desalineados con el backend** — `frontend/src/types/UsuarioResponseDTO.ts:4` y `UbicacionDTO.ts:4` declaran `id: string` (en el backend `UsuarioResponseDTO.id` es `Long`; `B/DTO/UbicacionDTO.java` también declara `String id` y conviene pasarlo a `Long` a la vez); `DashboardDTO.ts` tipa los puestos con `descripcion`, pero `B/service/UsuarioService.java:107` mete ahí las `dimensiones`; `EventoDTO.ts:12` exige `organizadorId`, que desaparecerá con 1.4.
- [ ] **4.9** Token guardado dos veces: `AuthService.login` (`authService.ts:11`) y `LoginPage.tsx:34`.
- [ ] **4.10 Código muerto** — `B/service/CategoriaService.java` y `B/exception/CategoriaNoEncontradaException.java` no tienen controlador (y la excepción no tiene handler: daría 500), `EventoService.listarPorCategoria`, `JwtUtils.getRolesFromToken`, `UsuarioRepository.findByNombre`. Usarlos o quitarlos.
- [ ] **4.11** `B/exception/GlobalExceptionHandler.java:53` hace cast a `FieldError` de todos los errores de validación; una validación a nivel de clase lanzaría `ClassCastException` (→ 500). Usar `instanceof`.

## 🔵 5. Mejoras posibles con Spring Boot 4.1 (ya disponible en `main` y `fix/seguridad-auth`)

> No son errores: son funciones que trae la actualización (o versiones 3.x intermedias que ya vienen incluidas) y que resolverían puntos de esta lista con menos código. Versión indicada entre paréntesis.

### Limpieza directa tras la actualización
- [ ] **5.1 Filtrar `FACTOR_PASSWORD` del JWT** (Security 7) — `B/security/JwtUtils.java:27-29` mete todas las authorities en el claim `roles`; Security 7 añade `FACTOR_PASSWORD` al autenticar. → Filtrar las que empiezan por `ROLE_`.
- [ ] **5.2 Quitar `spring.jpa.database-platform`** (Hibernate 7) — `application.properties:11`. Hibernate detecta MySQL solo y avisa `HHH90000025` en el arranque.
- [ ] **5.3 `spring.jpa.open-in-view=false`** — el arranque avisa de que está activo por defecto (consultas a BD durante la serialización). Comprobar antes que no aparezca `LazyInitializationException` en los mapeos a DTO.
- [ ] **5.4 Desactivar Swagger en producción** — springdoc 3 avisa al arrancar: `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false` en el perfil de producción (va con 3.1).

### Errores de esta lista que se simplifican
- [ ] **5.5 `ProblemDetail` (RFC 9457) para los errores** (Spring 6 / Boot 3.0) — sustituir los `Map<String,Object>` de `B/exception/GlobalExceptionHandler.java` por `ProblemDetail` (o `ErrorResponseException`). Formato estándar `{type,title,status,detail}`, y facilita 2.1 (401/403 correctos) y 4.4 (el frontend puede leer `detail` en vez de mostrar un toast genérico).
- [ ] **5.6 Soporte de Docker Compose en desarrollo** (Boot 3.1) — dependencia `spring-boot-docker-compose` (scope `developmentOnly`): al hacer `npm run dev:backend`, Spring arranca el MySQL del `docker-compose.yml` y configura la conexión solo. Resuelve 3.1 sin crear un perfil a mano.
- [ ] **5.7 Tests con Testcontainers + `@ServiceConnection`** (Boot 3.1) y **`RestTestClient`** (Spring 7) — tests de integración contra un MySQL real en contenedor, sin configurar URLs. Es la forma recomendada de empezar 4.1 (auth/registro, 1.1, 1.2, 2.1).
- [ ] **5.8 Null-safety con JSpecify** (Spring 7) — `@NullMarked` en `package-info.java` y `@Nullable` donde proceda; el IDE avisa de NPE como las de 1.2 (`getRol().name()`) y 2.8 (`fecha` null) antes de ejecutar.

### Nuevas funciones opcionales (para ampliar el TFG)
- [ ] **5.9 Versionado de API nativo** (Spring 7) — `@GetMapping(path = "/eventos", version = "1")` + `spring.mvc.apiversion.*`. Útil si se cambia el contrato (p. ej. al quitar `organizadorId` en 1.4) sin romper el frontend.
- [ ] **5.10 MFA / segundo factor** (Security 7) — `@EnableMultiFactorAuthentication` para exigir un segundo factor (p. ej. código por email) a `ROLE_ADMIN`.
- [ ] **5.11 Hilos virtuales** (Boot 3.2, Java 21) — `spring.threads.virtual.enabled=true`; más peticiones concurrentes con el mismo hardware, una línea de configuración.
- [ ] **5.12 Logs estructurados en JSON** (Boot 3.4) — `logging.structured.format.console=ecs`; logs legibles por herramientas (Docker, ELK, Grafana).
- [ ] **5.13 DTOs como `record`** (Java 16+, soportado por Jackson 3 y MapStruct) — los 11 DTOs de `B/DTO/` usan `@Data` de Lombok; los de solo lectura (`UsuarioResponseDTO`, `DashboardDTO`, `LoginRequestDTO`) quedarían inmutables y sin Lombok.

### Dependencias que conviene subir junto a la actualización
- [ ] **5.14 jjwt 0.11.5 → 0.13.0** — `B/security/JwtUtils.java` usa API obsoleta (`parserBuilder()`, `setSigningKey`, `parseClaimsJws`, `SignatureAlgorithm.HS256`). Cambia a `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` y `.signWith(key)`.
- [ ] **5.15 Lombok 1.18.32 → 1.18.48 y MapStruct 1.5.5 → 1.6.x** — versiones fijadas a mano en `pom.xml` (también en `annotationProcessorPaths`); las nuevas soportan JDK 25.
- [ ] **5.16 `spring-boot-starter-web` → `spring-boot-starter-webmvc`** — `pom.xml:40`. Boot 4 renombró el starter; el antiguo sigue funcionando pero está obsoleto.
- [ ] **5.17 Dependencias npm desactualizadas** (sin vulnerabilidades) — dentro del rango (`npm update` en `frontend/`, bajo riesgo): `lucide-react` 1.8 → 1.61, `radix-ui` 1.4 → 1.8, `react`/`react-dom` 19.2 → 19.3, `tailwindcss` 4.2 → 4.3, `typescript-eslint`, `date-fns`, etc. Versiones mayores a evaluar aparte: `eslint` 10, `typescript` 7, `react-day-picker` 10, `concurrently` 10 (raíz).

## ✅ Resuelto

- [x] **Spring Boot 3.4.1 → 4.1.2** (la 3.4 estaba sin soporte OSS) y **springdoc 2.3.0 → 3.2.0** — PR #6, commit `f6f04a8`, tag `v1.0.0`. Arregla además Swagger (`/v3/api-docs`), que ya fallaba en 3.4.1 con `NoSuchMethodError`.

- [x] **1.1 Registro con rol arbitrario** (commits `ef2a121` + `8d55115` en `fix/seguridad-auth`; aún no en `main` ni en `v1.0.0`) — `B/service/UsuarioService.java:177-181` rechaza `ROLE_ADMIN` con `NoAutorizadoException` → **403** (handler en `B/exception/GlobalExceptionHandler.java:65-71`, clave `"message"`); rol `null` → 400 vía `@NotNull` en `UsuarioRegistroDTO` + `@Valid` en `AuthController`. Retoques de la revisión anterior (401→403, errata `messsage`, espacio sobrante) aplicados.
- [x] Vulnerabilidades npm (2 critical raíz, 26 frontend) → `npm audit fix`, 0 restantes.
- [x] JDK 21 + `JAVA_HOME` instalados; `mvnw verify` OK.
- [x] Mensaje del merge de `fix/seguridad-auth` limpio (ahora `20b5706`, "Merge branch 'main' into fix/seguridad-auth").
- [x] Descartado: `curl` sí existe en `eclipse-temurin:21-jre-jammy` (healthcheck válido).

## Orden sugerido

1. Rama `fix/seguridad-auth` (actual, 1.1 hecho): 2.3 → 1.2 → 2.1 + 2.11 + 2.15 (+ 5.5) → 2.10 → 1.5 (+ test 4.1 con 5.7) → PR a `main` → tag `v1.1.0`.
2. Rama `fix/secretos-config`: 1.3 → 3.2 → 3.1 → 3.3 → 3.4.
3. Rama `fix/eventos`: 1.4 (+ 4.8) → 2.5 → 2.6 → 2.8 → 2.4.
4. Rama `fix/frontend-ui`: 2.9 → 2.12 → 2.13 → 2.14 → 2.17 → 4.7 → 4.2 → 4.9.
5. Rama `chore/spring-boot-4-limpieza` (tras la actualización): 5.1 → 5.2 → 5.14 → 5.15 → 5.3.
6. Al empezar tests (4.1): 5.7. Al tratar 2.1/4.4: 5.5. Al tratar 3.1: 5.6.
7. Resto (2.2, 2.7, 2.16, 3.x, 4.x, 5.16, 5.17) y mejoras opcionales 5.9–5.13.
