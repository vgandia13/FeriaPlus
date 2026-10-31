# Errores a corregir — Feria+

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Solo **fallos** de la aplicación (incluidos los latentes). Las recomendaciones, la deuda técnica, los tests y las funcionalidades nuevas están en `MEJORAS.md`.
> Auditoría: 2026-10-26. Última revisión: 2026-10-31 (1.6, 1.4, 2.5, 4.8, 2.21, 2.8, 2.17, 2.6, 2.7 y 4.12 resueltos en la rama `fix/eventos`, fusionada en `main` con la PR #9). Nuevo: 3.12.
> Git: `main` = `10a46c1` (PR #9, rama `fix/eventos`). Tags: `v1.0.0` (Spring Boot 4.1.2) y `v1.1.0` (PR #7, rama `fix/seguridad-auth`); `v1.2.0` (PR #9) pendiente de crear sobre `10a46c1`.
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`
> Descartados por decisión: 1.2, 1.3, 1.5 y 3.2 (proyecto académico; el rol de organizador sigue siendo autoasignable). Resueltos: 1.1, 1.7 (opción A: `PUT /perfil/me` exige la contraseña actual + formulario `ProfileEditForm` en el Dashboard; la B, invalidar JWT antiguos, no se hizo), 2.3, 2.16 (opción B: el `subject` del JWT es el id del usuario; `@Valid` en login y perfil), 2.18 (opción A: `/error` público), 2.19 (opción A: handlers 400/409 con mensajes fijos; el catch-all responde "Error interno" y registra el detalle con `log.error`), 2.20, 2.2 (409 con mensaje claro si el usuario tiene eventos, reservas o reseñas), 2.1 (401 en `AuthenticationException` y 403 en `AccessDeniedException`), 2.11 (el interceptor no redirige con un 401 de `/auth/*`), 2.15 (el filtro JWT ya no corta la petición; `HttpStatusEntryPoint` da 401 en rutas protegidas), 2.10 (el registro no guarda `usuario`; la Navbar exige `isLogged`), 2.22 (`ProtectedRoute` expulsa si no hay `usuario`), 4.1 (14 tests: 13 de integración con MockMvc + H2 y 1 unitario con Mockito; `h2` en scope `test`) y 3.5 (seeder con emails según el rol y contraseñas `12345678`; no se hizo la B, `@Profile("dev")`), 1.6 (opción A: `mapToEntity` ya no copia el `id`; nuevo `PUT /api/eventos/{id}` con `@PreAuthorize` ORGANIZADOR/ADMIN que solo deja editar al organizador del evento o a un ADMIN, 403 si no; edita `nombre` y `descripcion`; 4 tests nuevos, 18 en total), 1.4 (opción A: `POST /api/eventos` asigna como organizador al usuario autenticado y se ignora el `organizadorId` del JSON, que deja de ser obligatorio también en el `PUT`; no se hizo la B porque el `POST` solo lo permite a ORGANIZADOR; 1 test nuevo, 19 en total), 2.5 (opción A: `mapToEntity` busca la ubicación con `ubicacionRepository.findById` y responde 404 con `UbicacionNoEncontradaException` si falta o no existe; `UbicacionDTO.id` pasa a `Long`, así que un `id` no numérico da 400; la ubicación es obligatoria al crear; no se hizo la B, crear ubicaciones al vuelo; 1 test nuevo, 20 en total), 4.8 (opción A: `id: number` en `UbicacionDTO.ts` y `UsuarioResponseDTO.ts`, `eventos?` opcional y `dimensiones` en los puestos de `DashboardDTO.ts`, que el Dashboard ya muestra; `AdminPage` y `adminService.deleteUser` usan `number`; no se hizo la B, `openapi-typescript`), 2.21 (opción A: `EventPage.tsx:96,146` usa `event.ubicacion?.nombre ?? "Sin Ubicación"` y el mapa ya se ocultaba sin coordenadas; no se hizo la B, un `ErrorBoundary` en `MainLayout`), 2.8 (opción A: `UsuarioService.java:97` usa `fecha != null ? fecha.toString() : null`, como `EventoService`, y el Dashboard muestra "N/A"; no se hizo la B, `@Column(nullable = false)`, porque con `ddl-auto=update` habría que limpiar antes las filas con `fecha` null; el DTO ya tenía `@NotBlank`), 2.17 (opción A: `try/catch/finally` dentro de la función async, estados `loading` y `error`, mensaje en pantalla y toast si falla; `formatDate` solo con día/mes/año, por lo que el registro y la reserva tampoco muestran la hora; el `parseISO` queda para 2.24), 2.6 (opción A: `Ubicacion.eventos` sin `cascade`; no se hizo la B porque no hay endpoint para borrar ubicaciones), 2.7 (opción A: `Reserva.puesto` pasa a `@ManyToOne(fetch = LAZY)`; en MySQL hay que quitar a mano el UNIQUE antiguo de `puesto_id` con `ALTER TABLE reserva ADD INDEX idx_reserva_puesto (puesto_id), DROP INDEX <UK>` o recrear la BD), 4.12 (opción A: lista blanca `nombre`/`fecha`/`id` en `EventoService.obtenerTodos`; `OrdenInvalidoException` da 400; 2 tests nuevos, 22 en total).

## Cómo leer este archivo

- Cada apartado es **una rama**, en el orden en que conviene trabajarlas. Dentro de cada rama, los puntos van en orden de arreglo.
- Prioridad: 🔴 **P1** seguridad / pérdida de datos · 🟠 **P2** rompe un flujo visible · 🟡 **P3** bug menor de UI/UX · 🟢 **P4** configuración / deuda.
- Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) son comunes con `MEJORAS.md` y se conservan porque se citan entre sí.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| # | Rama | Contenido | Prioridad máx. | Puntos |
|---|---|---|---|---|
| 1 | `fix/seguridad-auth` | Usuarios, admin, auth y errores HTTP | 🔴 P1 | 0 |
| 2 | `fix/eventos` | Crear/editar eventos y modelo JPA | 🟡 P3 | 0 |
| 3 | `fix/config` | Docker, actuator, perfiles | 🔴 P1 | 5 |
| 4 | `fix/frontend-ui` | HTML base, navegación, paginación, accesibilidad | 🟠 P2 | 10 |
| 5 | `chore/limpieza-spring-boot-4` | Fallo latente en la validación | 🟢 P4 | 1 |

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

### Estado del entorno (2026-10-31)

| Comprobación | Resultado |
|---|---|
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME` | ✅ |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main` |
| `.\mvnw.cmd verify` (backend) | ✅ 22 tests (20 de integración + 2 unitarios) |
| `npm run lint` / `npm run build` (frontend) | ✅ 0 errores (aviso de bundle > 500 kB, ver 4.6 en `MEJORAS.md`) |
| `npm audit` raíz | ✅ 0 vulnerabilidades |
| `npm audit` frontend | ⚠️ 17 (2 bajas + 15 moderadas), solo en herramientas de desarrollo y sin arreglo publicado (ver 5.17 en `MEJORAS.md`). **No usar `npm audit fix --force`**: baja `shadcn` a 1.0 y `typescript-eslint` a 8.14 y rompe build y lint |

---

## 1️⃣ Rama `fix/seguridad-auth` — usuarios, admin y errores HTTP

> Fusionada en `main` (PR #7) y publicada como `v1.1.0`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 2️⃣ Rama `fix/eventos` — crear/editar eventos y modelo JPA

> Fusionada en `main` (PR #9). La tag `v1.2.0` está pendiente.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 3️⃣ Rama `fix/config` — Docker, actuator y perfiles

- [ ] **3.12 🔴 `docker compose down` borra la base de datos**
  - **Problema:** `docker-compose.yml` no declara ningún volumen para `mysql`. La imagen crea un volumen anónimo que `down` desvincula del contenedor, y el siguiente `up` arranca con otro vacío: se pierden los usuarios, eventos y reservas creados a mano (comprobado el 2026-10-31). Además, cada `down` deja un volumen huérfano ocupando disco. El README decía lo contrario ("conserva los datos").
  - **Solución:**
    - **A)** Volumen con nombre: `volumes: - mysql_data:/var/lib/mysql` en el servicio `mysql` y `volumes: mysql_data:` al final del fichero. Así `down` conserva los datos y `down -v` los borra. Después, actualizar la sección "Detener la aplicación" del README.
    - Limpieza de los volúmenes huérfanos ya creados: `docker volume prune` (borra **todos** los volúmenes sin contenedor, revisar antes con `docker volume ls -f dangling=true`).

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
  - **Problema:** `EventsPage.tsx:140`, `CarruselEventos.tsx:105`, `EventPage.tsx:99` y `formatDate` de `DashboardPage.tsx`: `new Date("2026-10-31")` muestra el 30/10 en zonas con desfase negativo (en España sale bien).
  - **Solución:**
    - **A)** `parseISO` de `date-fns` (ya instalado), que interpreta la fecha en hora local.
    - **B)** Una función `formatDate` común en `lib/` y usarla en todas las páginas (incluidos el Dashboard y `AdminPage`).

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
