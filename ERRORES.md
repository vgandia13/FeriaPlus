# Errores a corregir — Feria+

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Auditoría: 2026-10-26. Última revisión: 2026-10-27 (3ª auditoría completa: cada punto comprobado en el código y, en el backend, reproducido con el jar en H2).
> Git: `main` = `367a07c` (PR #6, Spring Boot 4.1.2, tag `v1.0.0`). `fix/seguridad-auth` = `f6804b0` (1.1 + quitar `@Repository`).
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`
> Descartados por decisión: 1.2, 1.3, 1.5 y 3.2 (proyecto académico; el rol de organizador sigue siendo autoasignable). Resueltos: 1.1, 2.3 y 2.20.

## Cómo leer este archivo

- Cada apartado es **una rama**, en el orden en que conviene trabajarlas. Dentro de cada rama, los puntos van en orden de arreglo.
- Prioridad: 🔴 **P1** seguridad / pérdida de datos · 🟠 **P2** rompe un flujo visible · 🟡 **P3** bug menor de UI/UX · 🟢 **P4** configuración / deuda.
- Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) se conservan porque se citan entre sí.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| # | Rama | Contenido | Prioridad máx. | Puntos |
|---|---|---|---|---|
| 1 | `fix/seguridad-auth` (actual) | Usuarios, admin, auth y errores HTTP | 🔴 P1 | 11 |
| 2 | `fix/eventos` | Crear/editar eventos y modelo JPA | 🔴 P1 | 11 |
| 3 | `fix/config` | Docker, actuator, perfiles, seeder | 🟠 P2 | 8 |
| 4 | `fix/frontend-ui` | HTML base, navegación, paginación, accesibilidad | 🟠 P2 | 15 |
| 5 | `chore/limpieza-spring-boot-4` | Código muerto y dependencias | 🟢 P4 | 12 |
| 6 | `feature/panel-organizador` | Funcionalidad pendiente | — | 1 |
| — | Sin rama | Mejoras opcionales para el TFG | — | 7 |

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

### Estado del entorno (2026-10-27)

| Comprobación | Resultado |
|---|---|
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME` | ✅ |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main` y `fix/seguridad-auth` |
| `.\mvnw.cmd verify` (backend) | ✅ compila — ⚠️ 0 tests |
| `npm run lint` / `npm run build` (frontend) | ✅ 0 errores (aviso de bundle > 500 kB, ver 4.6) |
| `npm audit` raíz y frontend | ✅ 0 vulnerabilidades |

---

## 1️⃣ Rama `fix/seguridad-auth` — usuarios, admin y errores HTTP

> Rama actual. Al terminar: PR a `main` → tag `v1.1.0`.

- [ ] **2.2 🟠 Borrar un usuario con reseñas, reservas, asistencias o eventos da 500**
  - **Problema:** `B/service/UsuarioService.java:172`. Las FK sin cascada provocan `DataIntegrityViolationException`, que llega como 500.
  - **Solución:**
    - **A)** Comprobar las dependencias antes de borrar y responder **409** con un mensaje claro ("el usuario tiene eventos/reservas").
    - **B)** Borrado lógico: un campo `activo=false` en lugar de `delete`.
    - **C)** Borrar las dependencias en cascada (desaconsejado: se pierden eventos y reseñas).

- [ ] **1.7 🔴 Cambiar la contraseña no pide la actual**
  - **Problema:** `B/service/UsuarioService.java:133-135` (`PUT /perfil/me`). Con un JWT robado (válido 24 h) se puede cambiar la contraseña y quedarse la cuenta.
  - **Solución:**
    - **A)** Añadir `passwordActual` al DTO de actualización y comprobarlo con `passwordEncoder.matches(...)` antes de cambiarla.
    - **B)** Además (opcional): guardar `passwordChangedAt` en `Usuario` y rechazar en `JwtAuthenticationFilter` los tokens con `iat` anterior.

- [ ] **2.18 🟠 `/error` no es público: todo error llega como 403 vacío**
  - **Problema:** `B/security/SecurityConfig.java:38-44`. Cuando Spring reenvía a `/error`, un anónimo recibe 403: token inválido → 403 (no 401), ruta inexistente → 403 (no 404), método no soportado → 403 (no 405). Es la causa de fondo de 2.11, 2.15 y 2.16: **arreglar antes que ellos**.
  - **Solución:**
    - **A)** `.requestMatchers("/error").permitAll()` en `SecurityConfig`.
    - **B)** `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` (cubre cualquier ruta de error).

- [ ] **2.1 🟠 Los errores de autenticación devuelven 500**
  - **Problema:** `B/exception/GlobalExceptionHandler.java:41` captura todo `RuntimeException`. Un login con credenciales malas da 500 (`BadCredentialsException`, debería ser 401); un `@PreAuthorize` denegado da 500 (`AccessDeniedException`, debería ser 403).
  - **Solución:**
    - **A)** Añadir `@ExceptionHandler(AuthenticationException.class)` → 401 y `@ExceptionHandler(AccessDeniedException.class)` → 403, antes del catch-all.
    - **B)** Hacerlo directamente con `ProblemDetail` (ver 5.5), así queda resuelto también 2.19.

- [ ] **2.11 🟠 Un login fallido recargará la página al arreglar 2.1**
  - **Problema:** `frontend/src/services/apiService.ts:41` trata cualquier 401 como sesión caducada y hace `window.location.href = '/login'`. Cuando 2.1 devuelva 401, se perderá el toast "Error en las credenciales".
  - **Solución:**
    - **A)** Excluir de la redirección las URLs que empiezan por `/auth/` (`if (status === 401 && !error.config.url.startsWith('/auth'))`).
    - **B)** Redirigir solo si había token guardado (si no lo había, no es una sesión caducada).

- [ ] **2.19 🔴 Los errores del cliente dan 500 y filtran detalles internos**
  - **Problema:** `B/exception/GlobalExceptionHandler.java:41-47` devuelve `ex.getMessage()` con status 500 para JSON mal formado, `/api/eventos/abc`, `fecha:"xx"`, `?sort=campoInexistente` y errores de FK. El cuerpo incluye clases de Hibernate, nombres de restricciones y SQL.
  - **Solución:**
    - **A)** Handlers específicos → 400 para `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `DateTimeParseException` y `PropertyReferenceException`; → 409 para `DataIntegrityViolationException`. En el catch-all, mensaje genérico ("Error interno") y `log.error(...)` con el detalle.
    - **B)** Extender `ResponseEntityExceptionHandler` (Spring ya da los 400 estándar) + `ProblemDetail` (5.5).

- [ ] **2.15 🟠 Un token caducado bloquea las páginas públicas**
  - **Problema:** `B/security/JwtAuthenticationFilter.java:45,49` corta la petición si el token es inválido o ha caducado, aunque la ruta sea `permitAll` (`GET /api/eventos`). Por 2.18 el cliente recibe un 403 vacío y `apiService.ts:41` no redirige: Home y Eventos fallan sin más.
  - **Solución:**
    - **A)** Si el token no es válido, no responder: llamar a `filterChain.doFilter(...)` sin autenticar y dejar que `SecurityConfig` decida (las rutas protegidas darán 401 por sí solas).
    - **B)** Frontend (complementario): al recibir 401/403 con token caducado, borrar el token y reintentar la petición sin él.

- [ ] **2.10 🟠 Tras registrarse, la Navbar muestra la sesión iniciada sin token**
  - **Problema:** `frontend/src/pages/RegisterPage.tsx:56` guarda la respuesta del registro en `localStorage.usuario`. Al recargar, `Navbar.tsx:76` decide por `usuario` (no por `isLogged`) y muestra "Mi panel" y "Salir" sin estar logueado. `authService.ts:17` tipa la respuesta como `UsuarioRegistro`, pero el backend devuelve `UsuarioResponseDTO`.
  - **Solución:**
    - **A)** Quitar ese `localStorage.setItem` (el login ya guarda el usuario) y corregir el tipo a `UsuarioResponseDTO`.
    - **B)** Además: que `Navbar` decida con `isLogged && usuario`.
    - **C)** Alternativa de UX: tras registrarse, hacer login automático con las mismas credenciales.

- [ ] **2.22 🟠 `ProtectedRoute` no comprueba el rol si `usuario` es `null`**
  - **Problema:** `frontend/src/components/ProtectedRoute.tsx:16`. Con token y `usuario` ausente o corrupto (`AppContext.tsx:35` devuelve `null`), cualquiera entra en `/admin`. El backend responde 403, pero se ve el panel.
  - **Solución:**
    - **A)** `if (allowedRoles && (!usuario || !allowedRoles.includes(usuario.rol)))`.
    - **B)** Además: si hay token pero no `usuario`, hacer logout en `AppContext`.

- [ ] **2.16 🟠 `PUT /perfil/me`: cambiar el email invalida la sesión y acepta emails vacíos**
  - **Problema:** `B/service/UsuarioService.java:120-125`. El JWT lleva el email antiguo como `subject`, así que la siguiente petición falla. `UsuarioController.java:31` no lleva `@Valid`: acepta `email:""`, y después se puede hacer login con email vacío (el login tampoco valida). Un email mal formado da 500. (El frontend aún no usa este endpoint.)
  - **Solución:**
    - **A)** DTO de actualización propio (`@NotBlank @Email`) con `@Valid`, también en el login. Si el email cambia, devolver un token nuevo en la respuesta.
    - **B)** Usar el **id** del usuario como `subject` del JWT, en vez del email (cambiar el email deja de afectar al token).
    - **C)** No permitir cambiar el email desde el perfil.

- [ ] **4.1 🟢 Cero tests** (empezar en esta rama)
  - **Problema:** no existe `backend/src/test` ni `spring-boot-starter-test`.
  - **Solución:**
    - **A)** Añadir `spring-boot-starter-test` y escribir tests de integración de lo arreglado aquí: login correcto/incorrecto (401), registro con `ROLE_ADMIN` (403), `PUT` de admin sin rol (400), admin editando o borrando a un admin (403).
    - **B)** Hacerlo con Testcontainers + MySQL real (5.7). Requiere Docker en marcha al ejecutar `mvnw verify`.
    - Al añadirlos: pasar `h2` a scope `test` (ver 3.9).

---

## 2️⃣ Rama `fix/eventos` — crear/editar eventos y modelo JPA

- [ ] **1.6 🔴 Cualquier ORGANIZADOR puede sobrescribir cualquier evento**
  - **Problema:** `B/service/EventoService.java:106` copia `dto.getId()` en la entidad y `save()` hace *merge*. Un `POST /api/eventos` con `{"id":2,...}` reescribe el evento 2 (probado: 200) y, como `asistentes` llega vacío, **borra sus asistencias**.
  - **Solución:**
    - **A)** Al crear, ignorar el id (`evento.setId(null)`, o no copiarlo en `mapToEntity`). Para editar, añadir `PUT /api/eventos/{id}` que cargue el evento existente, compruebe que el usuario autenticado es su organizador (o ADMIN) y copie solo los campos editables.
    - **B)** Quitar `id` del DTO de entrada (un `EventoCreateDTO` sin id ni organizador).

- [ ] **1.4 🔴 `organizadorId` viene del cliente**
  - **Problema:** `B/controller/EventoController.java:44` + `B/service/EventoService.java:118-119`. Un ORGANIZADOR puede crear eventos a nombre de cualquier usuario (probado con un VISITANTE como organizador).
  - **Solución:**
    - **A)** Recibir `Authentication`/`Principal` en el controlador, buscar el usuario por email y asignarlo como organizador; ignorar `organizadorId` del DTO.
    - **B)** Si un ADMIN debe poder crear eventos para otros: aceptar `organizadorId` solo cuando el rol sea ADMIN y comprobar que el destino es ORGANIZADOR.

- [ ] **2.5 🟠 Crear un evento con ubicación falla**
  - **Problema:** `B/service/EventoService.java:112` mapea la ubicación a una entidad suelta. Ubicación nueva → `TransientPropertyValueException` (500); `id` inexistente → 500 por FK; `id` no numérico → 500 (`UbicacionDTO.id` es `String`).
  - **Solución:**
    - **A)** Recibir solo `ubicacionId` (Long) y hacer `ubicacionRepository.findById(...).orElseThrow(...)` → 404 si no existe.
    - **B)** Si se quieren crear ubicaciones al vuelo: si viene sin id, guardarla antes con `ubicacionRepository.save(...)`.

- [ ] **4.8 🟢 Tipos TS desalineados con el backend** (hacer junto con 1.4)
  - **Problema:** `frontend/src/types/UsuarioResponseDTO.ts:4` y `UbicacionDTO.ts:4` declaran `id: string` (en el backend `UbicacionDTO.java:9` también es `String`); `UbicacionDTO.ts:8` exige `eventos`, que llega `null`; `DashboardDTO.ts:22` tipa los puestos con `descripcion`, pero `B/service/UsuarioService.java:106-107` mete ahí las `dimensiones`; `EventoDTO.ts:12` exige `organizadorId`, que desaparecerá con 1.4.
  - **Solución:**
    - **A)** Corregir a mano: `id: number` en ambos lados (`Long` en Java), `eventos?:` opcional, renombrar a `dimensiones` en `PuestoDTO` y quitar `organizadorId` del tipo de entrada.
    - **B)** Generar los tipos desde el OpenAPI de springdoc (`openapi-typescript`). Evita que se vuelvan a desalinear, pero añade una dependencia.

- [ ] **2.21 🟠 Un evento sin ubicación deja la app en blanco**
  - **Problema:** `frontend/src/pages/EventPage.tsx:96,146` usa `event.ubicacion.nombre` sin `?.`; el backend devuelve `ubicacion: null` y no hay ErrorBoundary.
  - **Solución:**
    - **A)** `event.ubicacion?.nombre ?? "Sin ubicación"` (como ya hace `EventsPage.tsx:139`) y no pintar el mapa si no hay coordenadas.
    - **B)** Además: un `ErrorBoundary` en `MainLayout`, para que un fallo de render no deje toda la app en blanco.

- [ ] **2.8 🟠 NPE en el dashboard si `fecha` es null**
  - **Problema:** `B/service/UsuarioService.java:95`.
  - **Solución:**
    - **A)** Copiar el patrón de `EventoService.java:76` (`fecha != null ? fecha.toString() : null`).
    - **B)** Hacer `fecha` obligatoria (`@Column(nullable = false)` + `@NotNull` en el DTO).

- [ ] **2.17 🟠 El dashboard se queda en "Cargando…" si falla la petición**
  - **Problema:** `frontend/src/pages/DashboardPage.tsx:21-26` no tiene `try/catch`. Además, `formatDate` (`:6-15`) añade la hora: `"2026-10-31"` sale como `31/10/2026, 01:00`.
  - **Solución:**
    - **A)** `try/catch/finally` con un estado `error` y un mensaje en pantalla; `formatDate` solo con día/mes/año y `parseISO` (ver 2.24).

- [ ] **4.12 🟡 El ordenamiento público acepta propiedades anidadas**
  - **Problema:** `B/controller/EventoController.java:33`: `?sort=organizador.password` funciona (ordena por el hash bcrypt).
  - **Solución:**
    - **A)** Lista blanca: comprobar que cada `Sort.Order` sea `nombre`, `fecha` o `id`; si no, 400.
    - **B)** Quitar `Pageable` libre y aceptar solo `?orden=fecha|nombre`.

- [ ] **2.7 🟡 `Reserva` ↔ `Puesto` inconsistente**
  - **Problema:** `B/model/Reserva.java:27-28` usa `@OneToOne` (Hibernate crea UNIQUE en `puesto_id`: un puesto solo admite una reserva en toda su vida, aunque se cancele), pero `Puesto` declara `@OneToMany reservas`. (Aún no hay endpoints de reservas.)
  - **Solución:**
    - **A)** Cambiar a `@ManyToOne` en `Reserva.puesto`. Con `ddl-auto=update` la UNIQUE antigua **no** se borra: eliminarla a mano en MySQL o recrear la BD.

- [ ] **2.6 🟢 (latente) Borrar una ubicación borraría sus eventos**
  - **Problema:** `B/model/Ubicacion.java:34` (`CascadeType.ALL`). Hoy ningún endpoint borra ubicaciones.
  - **Solución:**
    - **A)** Quitar el `cascade` (o dejar solo `PERSIST`/`MERGE`).
    - **B)** Si se añade un borrado de ubicaciones: responder 409 cuando tenga eventos.

- [ ] **4.5 🟢 Campo `reseñas` con `ñ`**
  - **Problema:** `B/model/Evento.java:59`. Los identificadores con `ñ` dan problemas en JSON, JPQL y en algunos editores.
  - **Solución:**
    - **A)** Renombrar a `resenas` (y su getter/setter y los usos en `EventoService`).

---

## 3️⃣ Rama `fix/config` — Docker, actuator, perfiles y seeder

- [ ] **3.10 🟠 El healthcheck del backend siempre falla**
  - **Problema:** `docker-compose.yml:34` hace `curl -f /actuator/health`, que da 403 porque esa ruta exige autenticación. El contenedor queda "unhealthy" para siempre (no se nota porque nada depende de él).
  - **Solución:**
    - **A)** `.requestMatchers("/actuator/health").permitAll()` en `SecurityConfig`, junto con 3.4.
    - **B)** Exponer el actuator en un puerto de gestión aparte (`management.server.port=8081`), no publicado en el host.

- [ ] **3.4 🟠 `health.show-details=always` expone detalles internos**
  - **Problema:** `application.properties:17`. Cualquier usuario registrado ve detalles de la BD y del disco (incluida la ruta local).
  - **Solución:**
    - **A)** `management.endpoint.health.show-details=never`.
    - **B)** `when-authorized` + `management.endpoint.health.roles=ADMIN`.

- [ ] **3.1 🟢 `npm run dev:backend` fuera de Docker no conecta**
  - **Problema:** `application.properties:7` apunta al host `mysql`, que solo existe dentro de Docker.
  - **Solución:**
    - **A)** Soporte de Docker Compose de Spring Boot (5.6): Spring arranca el MySQL del `docker-compose.yml` y configura la conexión solo.
    - **B)** Perfil `application-dev.properties` con `localhost:3307` y `"dev:backend"` lanzando con `-Dspring-boot.run.profiles=dev`.
    - **C)** `spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/...}` y pasar `DB_URL` en `docker-compose.yml`.

- [ ] **5.4 🟢 Desactivar Swagger en producción** (va con 3.1)
  - **Problema:** springdoc 3 avisa al arrancar de que la documentación de la API está expuesta.
  - **Solución:**
    - **A)** `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false` en el perfil de producción (o al revés: activarlos solo en `dev`).

- [ ] **3.3 🟢 `ddl-auto=update` en la configuración principal**
  - **Problema:** `application.properties:13`. Hibernate modifica el esquema en cada arranque y nunca borra columnas o restricciones antiguas (ver 2.7).
  - **Solución:**
    - **A)** `update` solo en `dev`; `validate` en producción.
    - **B)** Migraciones con Flyway (`spring-boot-starter-flyway`) y `validate`. Más robusto, pero añade trabajo.

- [ ] **3.8 🟢 Falta `.dockerignore` en `frontend/`**
  - **Problema:** `frontend/Dockerfile:5-6` hace `npm install` y luego `COPY . .`, que copia el `node_modules/` de Windows encima del de Linux (binarios nativos de rolldown, `@tailwindcss/oxide`, lightningcss).
  - **Solución:**
    - **A)** `frontend/.dockerignore` con `node_modules` y `dist`, y `npm ci` en lugar de `npm install`.

- [ ] **3.5 🟢 Seeder con contraseñas débiles y emails confusos**
  - **Problema:** `B/bootstrap/DataSeeder.java:42-64`. Todas las contraseñas son `123456`, `admin@feriaplus.com` es ORGANIZADOR y el admin real es `admin@gmail.com`.
  - **Solución:**
    - **A)** Renombrar los emails según el rol (`organizador@feriaplus.com`, `admin@feriaplus.com`…) y usar contraseñas de ≥ 8 caracteres (coherente con la validación del registro).
    - **B)** Ejecutar el seeder solo en el perfil `dev` (`@Profile("dev")`).

- [ ] **3.9 🟢 Restos de configuración**
  - **Problema:** `application-test.properties` (H2) está versionado sin tests, y `h2` (scope `runtime`) acaba dentro del jar de producción. En `pom.xml`, `lombok.version` (`:31`) existe pero la versión se repite a mano (`:64`, `:124`).
  - **Solución:**
    - **A)** `h2` → `<scope>test</scope>` (cuando existan tests, 4.1) y usar `${lombok.version}` en `:64` y `:124`.
    - **B)** Si se usan Testcontainers (5.7): quitar `h2` y `application-test.properties`.

---

## 4️⃣ Rama `fix/frontend-ui` — HTML base, navegación, paginación y accesibilidad

- [ ] **3.11 🟠 `frontend/index.html` incompleto**
  - **Problema:** falta `<meta name="viewport">` (en el móvil la app se pinta a ~980 px: nada es responsive), `<meta charset>` y `<title>`, y declara `lang="en"`. El script del tema quita la clase `dark` si lo guardado es `"system"` con el sistema en oscuro (destello claro al cargar).
  - **Solución:**
    - **A)** Añadir `<meta charset="UTF-8">`, `<meta name="viewport" content="width=device-width, initial-scale=1">`, `<title>Feria+</title>` y `lang="es"`. En el script: `(!theme || theme === "system") && matchMedia(...)`.

- [ ] **2.23 🟡 El detalle de evento exige login y pierde la ruta**
  - **Problema:** `frontend/src/App.tsx:27` protege `/events/:id`, aunque `GET /api/eventos/**` es público. `ProtectedRoute.tsx:13` no guarda el origen y `LoginPage.tsx:41` siempre navega a `/`. Un anónimo pulsa "Ver más" → login → acaba en Home.
  - **Solución:**
    - **A)** Sacar `/events/:id` de `ProtectedRoute` (es pública en el backend).
    - **B)** Si debe seguir protegida: `<Navigate to="/login" state={{ from: location }} replace />` y, tras el login, `navigate(location.state?.from?.pathname ?? "/")`.

- [ ] **2.14 🟡 Paginación: botón », índice -1 y página vacía tras borrar**
  - **Problema:** en `frontend/src/pages/AdminPage.tsx:316-320` y `EventsPage.tsx:176-180`, el `onClick` de » está en `<ChevronsRight>` y no en el `<Button>` (el borde y el teclado no hacen nada). Con 0 resultados, `Math.min(totalPages-1, …)` da `-1`. En `AdminPage.tsx:126-129`, al borrar el único usuario de la última página se muestra "No hay usuarios".
  - **Solución:**
    - **A)** Mover el `onClick` al `Button`, usar `Math.max(0, …)` y `disabled={page >= totalPages - 1}`. Tras borrar: `if (page >= totalPages && page > 0) setPage(totalPages - 1)`.
    - **B)** Extraer un componente `Paginacion` común a las dos páginas (incluye el arreglo de teclado de 4.7).

- [ ] **2.12 🟡 Panel admin: "Limpiar" no hace nada y el diálogo se cierra por accidente**
  - **Problema:** `frontend/src/pages/AdminEditForm.tsx:110` usa `type="reset"`, que no afecta a inputs controlados. En `AdminPage.tsx:273` el diálogo solo se cierra porque `loadUsers` desmonta la fila; el foco se pierde.
  - **Solución:**
    - **A)** "Limpiar" con `onClick` que restaure el estado inicial; `<Dialog open={editando} onOpenChange={setEditando}>` y cerrar en `onSuccess`.
    - **B)** Quitar el botón "Limpiar" (en un formulario de edición, "Cancelar" ya cumple esa función).

- [ ] **2.13 🟡 Registro: el selector de rol aparenta estar vacío**
  - **Problema:** `frontend/src/pages/RegisterPage.tsx:155`: el `Select` no recibe `value`, así que muestra "Selecciona un rol" aunque el estado ya sea `ROLE_VISITANTE`.
  - **Solución:**
    - **A)** `<Select value={rol} onValueChange={...}>`.

- [ ] **2.9 🟡 Mapa claro con el tema "system" en oscuro**
  - **Problema:** `frontend/src/pages/EventPage.tsx:43` compara `theme === "dark"` (y `:142` con `"light"` para el marcador). Con `"system"` no acierta.
  - **Solución:**
    - **A)** Calcular el tema resuelto: `theme === "system" ? (matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light") : theme`, como `ThemeToggle.tsx:8-10`.
    - **B)** Exponer `resolvedTheme` desde `ThemeProvider` y usarlo en los dos sitios.

- [ ] **2.24 🟡 Las fechas de solo día se interpretan en UTC**
  - **Problema:** `EventsPage.tsx:140`, `CarruselEventos.tsx:105` y `EventPage.tsx:99`: `new Date("2026-10-31")` muestra el 30/10 en zonas con desfase negativo (en España sale bien).
  - **Solución:**
    - **A)** `parseISO` de `date-fns` (ya instalado), que interpreta la fecha en hora local.
    - **B)** Una función `formatDate` común en `lib/` y usarla en todas las páginas (incluido 2.17).

- [ ] **4.7 🟡 Accesibilidad**
  - **Problema:**
    - Los `<label>` de `LoginPage.tsx:64,74` y `RegisterPage.tsx:85,95,105,128,154` no están asociados a su input.
    - Los botones de mostrar/ocultar contraseña (`LoginPage.tsx:84`, `RegisterPage.tsx:114,140`) no tienen `aria-label`.
    - Los botones « » (`AdminPage.tsx:294,318`, `EventsPage.tsx:154,178`) no tienen `aria-label`.
    - Anterior/Siguiente (`ui/pagination.tsx:54`) es un `<a>` sin `href`: no recibe foco ni responde a Enter; está en inglés.
    - `AdminPage.tsx:257` pone un texto en `aria-describedby`, que espera un id.
  - **Solución:**
    - **A)** `htmlFor`/`id` en cada par label-input; `aria-label="Mostrar contraseña"`, `"Primera página"`, `"Última página"`; sustituir `PaginationPrevious/Next` por `<Button disabled>` con texto "Anterior"/"Siguiente"; usar `<DialogDescription>` en lugar del texto en `aria-describedby`.
    - **B)** Usar el componente `Label` de `components/ui/` (ya existe) en lugar de `<label>` sueltos.

- [ ] **4.13 🟡 Estados de carga engañosos**
  - **Problema:** `EventPage.tsx:27` empieza con `loading=false` (parpadea "Evento no encontrado."); `CarruselEventos.tsx:43` muestra "no hay eventos" mientras carga y también si hay error.
  - **Solución:**
    - **A)** `useState(true)` en `EventPage`; en el carrusel, estados `loading` y `error` separados (con `Skeleton`, que ya existe).

- [ ] **4.4 🟢 Toasts duplicados**
  - **Problema:** el interceptor (`frontend/src/services/apiService.ts:48`) y las páginas muestran los dos un toast de error.
  - **Solución:**
    - **A)** Quitar el toast del interceptor y dejar que cada página muestre su mensaje.
    - **B)** Dejar solo el del interceptor, leyendo el `message`/`detail` del backend (va mejor con 5.5).

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

## 5️⃣ Rama `chore/limpieza-spring-boot-4` — código muerto y dependencias

- [ ] **4.10 🟢 Código muerto en el backend**
  - **Problema:** `B/service/CategoriaService.java` y `B/exception/CategoriaNoEncontradaException.java` no tienen controlador ni handler; `EventoService.listarPorCategoria`, `JwtUtils.getRolesFromToken` y `UsuarioRepository.findByNombre` no se usan.
  - **Solución:**
    - **A)** Borrarlos.
    - **B)** Usarlos: `GET /api/categorias` + filtro por categoría en Eventos (y handler 404 para la excepción).

- [ ] **4.11 🟢 (latente) Cast inseguro en el handler de validación**
  - **Problema:** `B/exception/GlobalExceptionHandler.java:53` hace cast a `FieldError` de todos los errores; una validación a nivel de clase lanzaría `ClassCastException`. Hoy ningún DTO la tiene.
  - **Solución:**
    - **A)** Usar `ex.getBindingResult().getFieldErrors()` en vez de `getAllErrors()`.
    - **B)** `if (error instanceof FieldError fe)`.

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

## 6️⃣ Rama `feature/panel-organizador` — funcionalidad pendiente

- [ ] **4.3 Rutas de organizador pendientes**
  - **Problema:** `frontend/src/App.tsx:31` (TODO). No hay ninguna pantalla para que un organizador cree o edite eventos.
  - **Solución:**
    - **A)** Ruta `/organizador` protegida con `allowedRoles={["ROLE_ORGANIZADOR"]}`, que liste sus eventos y permita crear/editar (requiere el `PUT` de 1.6 y la asignación del organizador de 1.4).

---

## ➕ Mejoras opcionales (sin rama asignada, para ampliar el TFG)

- [ ] **5.7 Tests con Testcontainers + `@ServiceConnection`** (Boot 3.1) y **`RestTestClient`** (Spring 7) — tests contra un MySQL real en contenedor (ver 4.1).
- [ ] **5.6 Soporte de Docker Compose en desarrollo** (Boot 3.1) — opción A de 3.1.
- [ ] **5.9 Versionado de API nativo** (Spring 7) — útil si se cambia el contrato (p. ej. al quitar `organizadorId` en 1.4).
- [ ] **5.13 DTOs como `record`** (Java 16+) — los de solo lectura (`UsuarioResponseDTO`, `DashboardDTO`, `LoginRequestDTO`).
- [ ] **5.11 Hilos virtuales** (Boot 3.2, Java 21) — `spring.threads.virtual.enabled=true`.
- [ ] **5.12 Logs estructurados en JSON** (Boot 3.4) — `logging.structured.format.console=ecs`.
- [ ] **5.10 MFA / segundo factor** (Security 7) — `@EnableMultiFactorAuthentication` para `ROLE_ADMIN`.
