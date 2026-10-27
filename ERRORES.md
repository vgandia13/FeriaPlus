# Errores a corregir — Feria+

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Solo **fallos** de la aplicación (incluidos los latentes). Las recomendaciones, la deuda técnica, los tests y las funcionalidades nuevas están en `MEJORAS.md`.
> Auditoría: 2026-10-26. Última revisión: 2026-10-27 (3ª auditoría completa: cada punto comprobado en el código y, en el backend, reproducido con el jar en H2).
> Git: `main` = `13e62a7` (PR #8, dependencias actualizadas). Tags: `v1.0.0` (Spring Boot 4.1.2) y `v1.1.0` (PR #7, rama `fix/seguridad-auth`).
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`
> Descartados por decisión: 1.2, 1.3, 1.5 y 3.2 (proyecto académico; el rol de organizador sigue siendo autoasignable). Resueltos: 1.1, 1.7 (opción A: `PUT /perfil/me` exige la contraseña actual + formulario `ProfileEditForm` en el Dashboard; la B, invalidar JWT antiguos, no se hizo), 2.3, 2.16 (opción B: el `subject` del JWT es el id del usuario; `@Valid` en login y perfil), 2.18 (opción A: `/error` público), 2.19 (opción A: handlers 400/409 con mensajes fijos; el catch-all responde "Error interno" y registra el detalle con `log.error`), 2.20, 2.2 (409 con mensaje claro si el usuario tiene eventos, reservas o reseñas), 2.1 (401 en `AuthenticationException` y 403 en `AccessDeniedException`), 2.11 (el interceptor no redirige con un 401 de `/auth/*`), 2.15 (el filtro JWT ya no corta la petición; `HttpStatusEntryPoint` da 401 en rutas protegidas), 2.10 (el registro no guarda `usuario`; la Navbar exige `isLogged`), 2.22 (`ProtectedRoute` expulsa si no hay `usuario`), 4.1 (14 tests: 13 de integración con MockMvc + H2 y 1 unitario con Mockito; `h2` en scope `test`) y 3.5 (seeder con emails según el rol y contraseñas `12345678`; no se hizo la B, `@Profile("dev")`).

## Cómo leer este archivo

- Cada apartado es **una rama**, en el orden en que conviene trabajarlas. Dentro de cada rama, los puntos van en orden de arreglo.
- Prioridad: 🔴 **P1** seguridad / pérdida de datos · 🟠 **P2** rompe un flujo visible · 🟡 **P3** bug menor de UI/UX · 🟢 **P4** configuración / deuda.
- Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) son comunes con `MEJORAS.md` y se conservan porque se citan entre sí.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| # | Rama | Contenido | Prioridad máx. | Puntos |
|---|---|---|---|---|
| 1 | `fix/seguridad-auth` (actual) | Usuarios, admin, auth y errores HTTP | 🔴 P1 | 0 |
| 2 | `fix/eventos` | Crear/editar eventos y modelo JPA | 🔴 P1 | 10 |
| 3 | `fix/config` | Docker, actuator, perfiles | 🟠 P2 | 4 |
| 4 | `fix/frontend-ui` | HTML base, navegación, paginación, accesibilidad | 🟠 P2 | 10 |
| 5 | `chore/limpieza-spring-boot-4` | Fallo latente en la validación | 🟢 P4 | 1 |

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
| `.\mvnw.cmd verify` (backend) | ✅ 14 tests (13 de integración + 1 unitario) |
| `npm run lint` / `npm run build` (frontend) | ✅ 0 errores (aviso de bundle > 500 kB, ver 4.6 en `MEJORAS.md`) |
| `npm audit` raíz y frontend | ✅ 0 vulnerabilidades |

---

## 1️⃣ Rama `fix/seguridad-auth` — usuarios, admin y errores HTTP

> Fusionada en `main` (PR #7) y publicada como `v1.1.0`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

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
  - **Problema:** `frontend/src/types/UsuarioResponseDTO.ts:4` y `UbicacionDTO.ts:4` declaran `id: string` (en el backend `UbicacionDTO.java:9` también es `String`); `UbicacionDTO.ts:8` exige `eventos`, que llega `null`; `DashboardDTO.ts:22` tipa los puestos con `descripcion`, pero `B/service/UsuarioService.java:109` mete ahí las `dimensiones`; `EventoDTO.ts:12` exige `organizadorId`, que desaparecerá con 1.4.
  - **Solución:**
    - **A)** Corregir a mano: `id: number` en ambos lados (`Long` en Java), `eventos?:` opcional, renombrar a `dimensiones` en `PuestoDTO` y quitar `organizadorId` del tipo de entrada.
    - **B)** Generar los tipos desde el OpenAPI de springdoc (`openapi-typescript`). Evita que se vuelvan a desalinear, pero añade una dependencia.

- [ ] **2.21 🟠 Un evento sin ubicación deja la app en blanco**
  - **Problema:** `frontend/src/pages/EventPage.tsx:96,146` usa `event.ubicacion.nombre` sin `?.`; el backend devuelve `ubicacion: null` y no hay ErrorBoundary.
  - **Solución:**
    - **A)** `event.ubicacion?.nombre ?? "Sin ubicación"` (como ya hace `EventsPage.tsx:139`) y no pintar el mapa si no hay coordenadas.
    - **B)** Además: un `ErrorBoundary` en `MainLayout`, para que un fallo de render no deje toda la app en blanco.

- [ ] **2.8 🟠 NPE en el dashboard si `fecha` es null**
  - **Problema:** `B/service/UsuarioService.java:97`.
  - **Solución:**
    - **A)** Copiar el patrón de `EventoService.java:76` (`fecha != null ? fecha.toString() : null`).
    - **B)** Hacer `fecha` obligatoria (`@Column(nullable = false)` + `@NotNull` en el DTO).

- [ ] **2.17 🟠 El dashboard se queda en "Cargando…" si falla la petición**
  - **Problema:** `frontend/src/pages/DashboardPage.tsx:32-39` no tiene `try/catch`. Además, `formatDate` (`:17-26`) añade la hora: `"2026-10-31"` sale como `31/10/2026, 01:00`.
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

---

## 3️⃣ Rama `fix/config` — Docker, actuator y perfiles

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
    - **A)** Soporte de Docker Compose de Spring Boot (5.6 en `MEJORAS.md`): Spring arranca el MySQL del `docker-compose.yml` y configura la conexión solo.
    - **B)** Perfil `application-dev.properties` con `localhost:3307` y `"dev:backend"` lanzando con `-Dspring-boot.run.profiles=dev`.
    - **C)** `spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/...}` y pasar `DB_URL` en `docker-compose.yml`.

- [ ] **3.8 🟢 Falta `.dockerignore` en `frontend/`**
  - **Problema:** `frontend/Dockerfile:5-6` hace `npm install` y luego `COPY . .`, que copia el `node_modules/` de Windows encima del de Linux (binarios nativos de rolldown, `@tailwindcss/oxide`, lightningcss).
  - **Solución:**
    - **A)** `frontend/.dockerignore` con `node_modules` y `dist`, y `npm ci` en lugar de `npm install`.

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
    - **B)** Dejar solo el del interceptor, leyendo el `message`/`detail` del backend (va mejor con 5.5, en `MEJORAS.md`).

---

## 5️⃣ Rama `chore/limpieza-spring-boot-4` — fallo latente (el resto de la rama está en MEJORAS.md)

- [ ] **4.11 🟢 (latente) Cast inseguro en el handler de validación**
  - **Problema:** `B/exception/GlobalExceptionHandler.java:53` hace cast a `FieldError` de todos los errores; una validación a nivel de clase lanzaría `ClassCastException`. Hoy ningún DTO la tiene.
  - **Solución:**
    - **A)** Usar `ex.getBindingResult().getFieldErrors()` en vez de `getAllErrors()`.
    - **B)** `if (error instanceof FieldError fe)`.
