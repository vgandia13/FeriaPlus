# Errores a corregir — Feria+

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Auditoría: 2026-10-26. Última revisión: 2026-10-27 (3ª auditoría completa: cada punto comprobado en el código y, en el backend, reproducido con el jar en H2).
> Git: `main` = `367a07c` (PR #6, Spring Boot 4.1.2, tag `v1.0.0`). `fix/seguridad-auth` = `f6804b0` (1.1 + quitar `@Repository`).
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`
> Se eliminan de la lista los puntos resueltos y los rechazados (1.1 resuelto; 1.2, 1.3, 1.5 y 3.2 descartados por decisión: proyecto académico, rol autoasignable de organizador se mantiene).
> **Orden:** por prioridad de arreglo (P1 → P4, y dentro de cada nivel de arriba abajo). Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) se conservan porque se citan entre sí.

## Estado del entorno

| Comprobación                                                              | Resultado                                                                                                                                                                        |
| ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME`                                 | ✅                                                                                                                                                                               |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main` y `fix/seguridad-auth`                                                                                                                                              |
| `.\mvnw.cmd verify` (backend, `fix/seguridad-auth`)                       | ✅ compila (2026-10-27) — ⚠️ 0 tests                                                                                                                                             |
| `npm run lint` (frontend)                                                 | ✅ 0 errores (2026-10-27)                                                                                                                                                        |
| `npm run build` (frontend)                                                | ✅ (aviso de bundle > 500 kB, ver 4.6)                                                                                                                                           |
| `npm audit` raíz y frontend                                               | ✅ 0 vulnerabilidades                                                                                                                                                            |
| `npm outdated`                                                            | ⚠️ ver 5.17                                                                                                                                                                      |

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

---

## 🔴 P1 — Seguridad e integridad de datos (arreglar primero)

- [ ] **1.6 Cualquier ORGANIZADOR puede sobrescribir cualquier evento** — `B/service/EventoService.java:106` copia `dto.getId()` en la entidad y `save()` hace *merge*. `POST /api/eventos` con `{"id":2,...}` reescribe el evento 2 (probado: 200) y, como `asistentes` llega vacío, **borra sus asistencias**. → `evento.setId(null)` al crear; para editar, un `PUT` que compruebe que el usuario es el organizador. Hacer junto con 1.4.
- [ ] **1.4 `organizadorId` viene del cliente** — `B/controller/EventoController.java:44` + `B/service/EventoService.java:118-119`. Un ORGANIZADOR puede crear eventos a nombre de cualquier usuario (probado con un VISITANTE como organizador). → Tomar el organizador de `Authentication` (email del token).
- [ ] **2.3 Admin puede dejar rol, email o nombre `null`** — `B/service/UsuarioService.java:150-152`; el `PUT` de `B/controller/AdminController.java:54` no lleva `@Valid`. Sin `rol` → el login de ese usuario da 500 (NPE en `B/security/UserDetailsServiceImpl.java:31`). Sin `email` → se guarda `null` (`equals(null)` y `existsByEmail(null)` no lo frenan) y la cuenta queda inaccesible. → Validar en el servicio (o un DTO de edición propio con `@Valid`). **No** basta con `@Valid` sobre `UsuarioRegistroDTO`: `frontend/src/pages/AdminEditForm.tsx:48` no envía `password`. De paso, añadir `required` a nombre y email en `AdminEditForm.tsx:67-87`.
- [ ] **2.20 El admin puede editarse, degradarse o borrarse a sí mismo** — `B/service/UsuarioService.java:152,158` y `frontend/src/pages/AdminPage.tsx:225-283`. Borrar su propio id da 204 y puede dejar el sistema sin ningún ADMIN; si cambia su email o su rol, su sesión queda incoherente (401/403 y Navbar desactualizada). → Rechazar en el backend la operación sobre uno mismo o sobre el último admin, y ocultar los botones en su fila.
- [ ] **1.7 Cambiar la contraseña no pide la actual** — `B/service/UsuarioService.java:133-135` (`PUT /perfil/me`). Con un JWT robado (válido 24 h) se puede cambiar la contraseña y quedarse la cuenta. → Exigir `passwordActual`.
- [ ] **2.19 Errores del cliente dan 500 y filtran detalles internos** — `B/exception/GlobalExceptionHandler.java:41-47` devuelve `ex.getMessage()` con status 500 para JSON mal formado, `/api/eventos/abc`, `fecha:"xx"`, `?sort=campoInexistente` y errores de FK; el cuerpo incluye clases de Hibernate, nombres de restricciones y SQL. → Handlers 400 para esos casos y mensaje genérico en el 500 (detalle solo en el log). Hacer junto con 2.1 (+ 5.5).

## 🟠 P2 — Bugs que rompen flujos visibles

- [ ] **2.18 `/error` no es público: todo error llega como 403 vacío** — `B/security/SecurityConfig.java:38-44`. Cuando Spring reenvía a `/error` (`sendError`), un anónimo recibe 403: token inválido → 403 (no 401), ruta inexistente → 403 (no 404), método no soportado → 403 (no 405). → `.requestMatchers("/error").permitAll()`. Es la base de 2.11, 2.15 y 2.16: arreglar antes.
- [ ] **2.1 Errores de auth devuelven 500** — `B/exception/GlobalExceptionHandler.java:41` captura todo `RuntimeException`: login con credenciales malas → 500 (`BadCredentialsException`, debería ser 401); `@PreAuthorize` denegado (visitante hace `POST /api/eventos`) → 500 (`AccessDeniedException`, debería ser 403). Las reglas por URL de `SecurityConfig` sí dan 403. Añadir handlers específicos.
- [ ] **2.11 Un login fallido recargará la página al arreglar 2.1** — `frontend/src/services/apiService.ts:41` trata cualquier 401 como sesión caducada y hace `window.location.href = '/login'`. → Excluir `/auth/*` de esa redirección. Hacerlo junto con 2.1.
- [ ] **2.15 Token caducado bloquea las páginas públicas** — `B/security/JwtAuthenticationFilter.java:45,49` corta la petición si el token es inválido/caducado aunque la ruta sea `permitAll` (`GET /api/eventos`). Por 2.18 el cliente recibe **403 vacío** (no 401), así que `apiService.ts:41` no redirige: Home y Eventos fallan sin más. → Si el token no es válido, seguir la cadena sin autenticar.
- [ ] **3.11 `frontend/index.html` incompleto** — falta `<meta name="viewport">` (en móvil la app se pinta a ~980 px: nada es responsive), `<meta charset>` y `<title>`, y declara `lang="en"`. Además, el script del tema quita la clase `dark` si lo guardado es `"system"` con el sistema en oscuro (destello claro). → Añadir las metas, `<title>Feria+</title>`, `lang="es"` y tratar `"system"` como `!theme`.
- [ ] **2.21 Evento sin ubicación deja la app en blanco** — `frontend/src/pages/EventPage.tsx:96,146` usa `event.ubicacion.nombre` sin `?.`; el backend devuelve `ubicacion: null` (`EventoService.java:82`) y no hay ErrorBoundary. → `event.ubicacion?.nombre ?? "Sin ubicación"` (como `EventsPage.tsx:139`).
- [ ] **2.10 Tras registrarse, la Navbar muestra sesión iniciada sin token** — `frontend/src/pages/RegisterPage.tsx:56` guarda la respuesta del registro en `localStorage.usuario`. Al recargar, `AppContext` la lee y `frontend/src/components/Navbar.tsx:76` decide por `usuario` (no por `isLogged`): aparecen "Mi panel" y "Salir" sin estar logueado. Si había otra sesión abierta, se mezcla el usuario nuevo con el token anterior. → Quitar ese `setItem`. De paso, `authService.ts:17` tipa la respuesta como `UsuarioRegistro`, pero el backend devuelve `UsuarioResponseDTO`.
- [ ] **2.22 `ProtectedRoute` no comprueba el rol si `usuario` es `null`** — `frontend/src/components/ProtectedRoute.tsx:16`. Con token y `usuario` ausente o corrupto (`AppContext.tsx:35` devuelve `null`), cualquiera entra en `/admin` (el backend responde 403, pero se ve el panel). → `if (allowedRoles && (!usuario || !allowedRoles.includes(usuario.rol)))`.
- [ ] **2.5 Crear evento con ubicación falla** — `B/service/EventoService.java:112`: ubicación nueva → `TransientPropertyValueException` (500); `id` inexistente → 500 por FK; `id` no numérico → 500 (`NumberFormatException`, `UbicacionDTO.id` es `String`). Buscar la ubicación por id en el repositorio. (Va con 1.6/1.4.)
- [ ] **2.8 NPE en dashboard** si `fecha` es null — `B/service/UsuarioService.java:95` (en `EventoService.java:76` ya está protegido; copiar ese patrón).
- [ ] **2.17 Dashboard se queda en "Cargando…" si falla la petición** — `frontend/src/pages/DashboardPage.tsx:21-26` sin `try/catch`. De paso, `formatDate` (`:6-15`) añade hora: `"2026-10-31"` sale como `31/10/2026, 01:00`.
- [ ] **2.16 `PUT /perfil/me`: cambiar el email invalida la sesión y acepta emails vacíos** — `B/service/UsuarioService.java:120-125`: el JWT lleva el email antiguo como `subject` (la siguiente petición da 403, ver 2.18). `B/controller/UsuarioController.java:31` no lleva `@Valid`: `email:""` se acepta (y luego se puede hacer login con email vacío, porque `AuthController` tampoco valida el login); un email mal formado da 500 al fallar el `@Email` de la entidad en el commit. → Token nuevo al cambiar email (o id como `subject`) y DTO de actualización con `@NotBlank @Email` + `@Valid`. (El frontend aún no usa este endpoint.)
- [ ] **2.2 Borrar usuario con reseñas/reservas/asistencias/eventos → 500** por FK — `B/service/UsuarioService.java:159`. Decidir: bloquear con 409 o borrar dependencias.
- [ ] **3.10 El healthcheck del backend siempre falla** — `docker-compose.yml:34` hace `curl -f /actuator/health`, que da 403 porque esa ruta exige autenticación: el contenedor queda "unhealthy" para siempre (no se nota porque nada depende de él). → `.requestMatchers("/actuator/health").permitAll()` junto con 3.4.
- [ ] **3.4** `health.show-details=always` — `application.properties:17`. El anónimo recibe 403 (ver 3.10), pero cualquier usuario registrado ve detalles de BD y disco (incluida la ruta local). → `when-authorized` o `never`. Hacer junto con 3.10.

## 🟡 P3 — Bugs menores de UI/UX

- [ ] **2.23 El detalle de evento exige login y pierde la ruta** — `frontend/src/App.tsx:27` protege `/events/:id`, aunque `GET /api/eventos/**` es público; `ProtectedRoute.tsx:13` no guarda el origen y `LoginPage.tsx:41` siempre navega a `/`. Un anónimo pulsa "Ver más" → login → acaba en Home. → Sacar la ruta de `ProtectedRoute`, o pasar `state={{ from: location }}` y volver ahí tras el login.
- [ ] **2.14 Paginación: botón », índice -1 y página vacía tras borrar** — `frontend/src/pages/AdminPage.tsx:316-320` y `frontend/src/pages/EventsPage.tsx:176-180`: el `onClick` de » está en `<ChevronsRight>`, no en el `<Button>` (el borde y el teclado no hacen nada); con 0 resultados `Math.min(totalPages-1, …)` da `-1` → usar `Math.max(0, …)` y `disabled`. En `AdminPage.tsx:126-129`, al borrar el único usuario de la última página se muestra "No hay usuarios" → retroceder una página.
- [ ] **2.12 Panel admin: "Limpiar" no hace nada y el diálogo se cierra por accidente** — `frontend/src/pages/AdminEditForm.tsx:109` usa `type="reset"`, que no afecta a inputs controlados. `frontend/src/pages/AdminPage.tsx:273`: el diálogo solo se cierra porque `loadUsers` desmonta la fila (skeleton); el foco se pierde. → `open` controlado.
- [ ] **2.13 Registro: el selector de rol aparenta estar vacío** — `frontend/src/pages/RegisterPage.tsx:155`: falta `value={rol}`.
- [ ] **2.9 Mapa claro con tema "system" oscuro** — `frontend/src/pages/EventPage.tsx:43` compara `theme === "dark"`; usar el tema resuelto (`matchMedia`, como `ThemeToggle.tsx:8-10`). Lo mismo con el marcador en `:142`.
- [ ] **2.24 Fechas de solo día se interpretan en UTC** — `frontend/src/pages/EventsPage.tsx:140`, `frontend/src/components/CarruselEventos.tsx:105`, `frontend/src/pages/EventPage.tsx:99`: `new Date("2026-10-31")` muestra el 30/10 en zonas con desfase negativo (en España sale bien). → `parseISO` de `date-fns` (ya instalado).
- [ ] **4.7 Accesibilidad**:
  - Los `<label>` de `LoginPage.tsx:64,74` y `RegisterPage.tsx:85,95,105,128,154` no están asociados a su input (falta `htmlFor`/`id`).
  - Los botones de mostrar/ocultar contraseña (`LoginPage.tsx:84`, `RegisterPage.tsx:114,140`) no tienen `aria-label`.
  - Los botones « » de paginación (`AdminPage.tsx:294,318`, `EventsPage.tsx:154,178`) tampoco tienen `aria-label`.
  - Anterior/Siguiente (`ui/pagination.tsx:54`, usado en `AdminPage.tsx:301,310` y `EventsPage.tsx:161,170`) es un `<a>` sin `href`: no recibe foco ni responde a Enter; "deshabilitado" es solo `pointer-events-none` y el texto está en inglés.
  - `AdminPage.tsx:257` pone un texto en `aria-describedby`, que espera un id.
- [ ] **4.13** Estados de carga engañosos — `EventPage.tsx:27` empieza con `loading=false` (parpadea "Evento no encontrado."); `CarruselEventos.tsx:43` muestra "no hay eventos" mientras carga y si hay error.
- [ ] **2.7 `Reserva` ↔ `Puesto` inconsistente** — `B/model/Reserva.java:27-28` usa `@OneToOne` (Hibernate crea UNIQUE en `puesto_id`: un puesto solo admite una reserva en toda su vida, aunque se cancele), pero `Puesto` declara `@OneToMany reservas`. Cambiar a `@ManyToOne`. (Aún no hay endpoints de reservas.)
- [ ] **4.12** El ordenamiento público acepta propiedades anidadas — `B/controller/EventoController.java:33`: `?sort=organizador.password` funciona (ordena por el hash bcrypt). → Lista blanca de campos.

## 🟢 P4 — Configuración, deuda y limpieza

- [ ] **4.1 Cero tests.** No existe `backend/src/test` ni `spring-boot-starter-test`. Empezar por tests de integración de auth/registro y eventos (cubren 1.6, 2.1, 2.3). Conviene ir añadiéndolos mientras se arregla P1/P2 (ver 5.7).
- [ ] **4.8 Tipos TS desalineados con el backend** — `frontend/src/types/UsuarioResponseDTO.ts:4` y `UbicacionDTO.ts:4` declaran `id: string` (`B/DTO/UbicacionDTO.java:9` también `String`; pasar ambos a número/`Long`); `UbicacionDTO.ts:8` exige `eventos`, que llega `null`; `DashboardDTO.ts:22` tipa los puestos con `descripcion`, pero `B/service/UsuarioService.java:106-107` mete ahí las `dimensiones`; `EventoDTO.ts:12` exige `organizadorId`, que desaparecerá con 1.4.
- [ ] **3.1** `npm run dev:backend` fuera de Docker no conecta: `application.properties:7` apunta al host `mysql`. → Perfil `dev` con `localhost:3307` (o 5.6).
- [ ] **3.3** `ddl-auto=update` en config principal — `application.properties:13`.
- [ ] **3.8 Falta `.dockerignore` en `frontend/`** — `frontend/Dockerfile:5-6` hace `npm install` y luego `COPY . .`, que copia el `node_modules/` de Windows encima del de Linux (binarios nativos de rolldown, `@tailwindcss/oxide`, lightningcss). → `.dockerignore` con `node_modules` y `dist`, y `npm ci`.
- [ ] **3.5** Seeder: contraseñas `123456` y `admin@feriaplus.com` es ORGANIZADOR (el admin real es `admin@gmail.com`) — `B/bootstrap/DataSeeder.java:42-64`.
- [ ] **4.4** Toasts duplicados: el interceptor (`frontend/src/services/apiService.ts:48`) y las páginas muestran ambos un error.
- [ ] **4.9** Token guardado dos veces: `AuthService.login` (`authService.ts:11`) y `LoginPage.tsx:34`.
- [ ] **2.4 Frontend llama a endpoints inexistentes** — `frontend/src/services/userService.ts:7` (`GET /perfil/me`; solo existe `PUT` → 405) y `frontend/src/services/eventoService.ts:35` (`DELETE /eventos/{id}`). Ninguna de las dos funciones (ni `EventoService.create`) se usa: hoy es código muerto. Crear el endpoint o quitar la llamada.
- [ ] **4.10 Código muerto** — `B/service/CategoriaService.java` y `B/exception/CategoriaNoEncontradaException.java` sin controlador ni handler, `EventoService.listarPorCategoria`, `JwtUtils.getRolesFromToken`, `UsuarioRepository.findByNombre`. Usarlos o quitarlos.
- [ ] **4.2** `console.log` sueltos — `EventPage.tsx:61`, `LoginPage.tsx:36`, `RegisterPage.tsx:57`.
- [ ] **4.6** Bundle JS de 659 kB → `React.lazy` por rutas (Leaflet solo en `EventPage`).
- [ ] **4.3** Rutas de organizador pendientes — `frontend/src/App.tsx:31` (TODO).
- [ ] **3.9 Restos de configuración** — `application-test.properties` (H2) versionado sin tests, y `h2` (scope `runtime`) acaba dentro del jar de producción → scope `test` con 4.1. En `pom.xml` la propiedad `lombok.version` (`:31`) existe pero la versión se repite a mano (`:64`, `:124`).
- [ ] **3.7** `shadcn` (CLI) está en `dependencies` — `frontend/package.json:30` → mover a `devDependencies`. `next-themes` (`:22`) tampoco se usa (solo lo importa `ui/sonner.tsx`, que nadie usa).
- [ ] **3.6** `README.md:19` dice que MapStruct mapea entidades↔DTOs en general; solo se usa para `Ubicacion` (la línea 68 ya lo dice bien).
- [ ] **4.5** Campo `reseñas` con `ñ` — `B/model/Evento.java:59`.
- [ ] **2.6 (latente) Borrar una ubicación borraría sus eventos** — `B/model/Ubicacion.java:34` (`CascadeType.ALL`). Hoy ningún endpoint borra ubicaciones.
- [ ] **4.11 (latente)** `B/exception/GlobalExceptionHandler.java:53` hace cast a `FieldError` de todos los errores de validación; una validación a nivel de clase lanzaría `ClassCastException`. Hoy ningún DTO la tiene. Usar `instanceof`.

## 🔵 5. Mejoras posibles con Spring Boot 4.1 (no son errores; en orden de utilidad)

> Funciones que trae la actualización (o versiones 3.x intermedias ya incluidas) y que resolverían puntos de esta lista con menos código. Versión indicada entre paréntesis.

### Ayudan a arreglar P1–P2

- [ ] **5.5 `ProblemDetail` (RFC 9457) para los errores** (Spring 6 / Boot 3.0) — sustituir los `Map<String,Object>` de `B/exception/GlobalExceptionHandler.java`. Facilita 2.1, 2.19 y 4.4.
- [ ] **5.7 Tests con Testcontainers + `@ServiceConnection`** (Boot 3.1) y **`RestTestClient`** (Spring 7) — forma recomendada de empezar 4.1.
- [ ] **5.6 Soporte de Docker Compose en desarrollo** (Boot 3.1) — `spring-boot-docker-compose` (scope `developmentOnly`). Resuelve 3.1.
- [ ] **5.8 Null-safety con JSpecify** (Spring 7) — `@NullMarked` en `package-info.java`; el IDE avisaría de NPE como 2.3 (`getRol().name()`) y 2.8 (`fecha` null).

### Limpieza tras la actualización

- [ ] **5.3 `spring.jpa.open-in-view=false`** — ⚠️ hacerlo sin más rompe la app: `EventoService.obtenerTodos`/`obtenerPorId` no son transaccionales y `mapToDTO` (`:77-80`, `:94`) toca relaciones LAZY → `LazyInitializationException`. Antes, `@Transactional(readOnly = true)` en esos métodos (y resolver el N+1: ~4 consultas por evento).
- [ ] **5.4 Desactivar Swagger en producción** — `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false` en el perfil de producción (va con 3.1).
- [ ] **5.14 jjwt 0.11.5 → 0.13.0** — `B/security/JwtUtils.java` usa API obsoleta (`parserBuilder()`, `setSigningKey`, `parseClaimsJws`, `SignatureAlgorithm.HS256`).
- [ ] **5.1 Filtrar `FACTOR_PASSWORD` del JWT** (Security 7) — `B/security/JwtUtils.java:27-29` emite `"roles":"ROLE_ORGANIZADOR,FACTOR_PASSWORD"`. Sin efecto hoy (nadie lee el claim). → Filtrar las que empiezan por `ROLE_`.
- [ ] **5.2 Quitar `spring.jpa.database-platform`** (Hibernate 7) — `application.properties:11` (aviso `HHH90000025`).
- [ ] **5.15 Lombok 1.18.32 → 1.18.48 y MapStruct 1.5.5 → 1.6.x** — `pom.xml:31,64,107,124,134`.
- [ ] **5.16 `spring-boot-starter-web` → `spring-boot-starter-webmvc`** — `pom.xml:40`.
- [ ] **5.17 Dependencias npm desactualizadas** (sin vulnerabilidades) — dentro del rango (`npm update` en `frontend/`): `lucide-react` 1.8 → 1.62, `radix-ui` 1.4 → 1.8.1, `react`/`react-dom` 19.2 → 19.3.1, `tailwindcss` 4.2 → 4.3.3, etc. Mayores a evaluar aparte: `eslint` 10, `typescript` 7, `react-day-picker` 10, `concurrently` 10 (raíz).

### Nuevas funciones opcionales (para ampliar el TFG)

- [ ] **5.9 Versionado de API nativo** (Spring 7) — útil si se cambia el contrato (p. ej. al quitar `organizadorId` en 1.4).
- [ ] **5.13 DTOs como `record`** (Java 16+) — los de solo lectura (`UsuarioResponseDTO`, `DashboardDTO`, `LoginRequestDTO`).
- [ ] **5.11 Hilos virtuales** (Boot 3.2, Java 21) — `spring.threads.virtual.enabled=true`.
- [ ] **5.12 Logs estructurados en JSON** (Boot 3.4) — `logging.structured.format.console=ecs`.
- [ ] **5.10 MFA / segundo factor** (Security 7) — `@EnableMultiFactorAuthentication` para `ROLE_ADMIN`.

## Ramas propuestas

1. `fix/seguridad-auth` (actual): 2.3 → 2.20 → 1.7 → 2.18 → 2.1 + 2.11 + 2.19 (+ 5.5) → 2.15 → 2.10 → 2.22 → 2.16 (+ tests 4.1 / 5.7) → PR a `main` → tag `v1.1.0`.
2. `fix/eventos`: 1.6 + 1.4 + 2.5 (+ 4.8) → 2.21 → 2.8 → 2.17 → 2.2 → 4.12.
3. `fix/config`: 3.10 + 3.4 → 3.1 (+ 5.6) → 3.3 → 3.8 → 3.5.
4. `fix/frontend-ui`: 3.11 → 2.23 → 2.14 → 2.12 → 2.13 → 2.9 → 2.24 → 4.7 → 4.13 → 4.4 → 4.9 → 4.2.
5. `chore/limpieza`: resto de P4 y 5.x.
