# Errores a corregir — Feria+

> ⚠️ **4 errores pendientes** desde la revisión del 2026-11-02 (rama 6: 1.8, 2.25, 2.26 y 3.13). Hasta el 2026-10-31 no quedaba ninguno. El archivo sirve también como **registro** de los fallos encontrados y de cómo se resolvió cada uno (ver "Resueltos" más abajo). Las mejoras pendientes siguen en `MEJORAS.md`, que cita estos números.

> Archivo versionado (desde 2026-10-26) para seguir el trabajo entre dispositivos.
> Solo **fallos** de la aplicación (incluidos los latentes). Las recomendaciones, la deuda técnica, los tests y las funcionalidades nuevas están en `MEJORAS.md`.
> Auditoría: 2026-10-26. Última revisión: 2026-11-02 (1.6, 1.4, 2.5, 4.8, 2.21, 2.8, 2.17, 2.6, 2.7 y 4.12 resueltos en la rama `fix/eventos`, fusionada en `main` con la PR #9); 3.12, 3.4, 3.10, 3.1 y 3.8 resueltos en la rama `fix/config`, fusionada con la PR #11; 3.11, 2.23, 2.14, 2.12, 2.13, 2.9, 2.24, 4.7, 4.13 y 4.4 resueltos en la rama `fix/frontend-ui`, fusionada con la PR #12 y publicada como `v1.2.0`; 4.11 resuelto en la rama `chore/limpieza-spring-boot-4`, fusionada con la PR #13 y publicada como `v1.2.1`. Revisión del 2026-11-02: nuevos 1.8, 2.25, 2.26 y 3.13, reproducidos con tests temporales (ya borrados) y, el 3.13, arrancando el jar; descartados el `DELETE /api/eventos/{id}` muerto de `eventoService.ts` (ya es 2.4 en `MEJORAS.md`) y las versiones de `ci.yml` (`actions/checkout@v7`, `setup-java@v6` y `setup-node@v7` existen).
> Git: `main` = `fd107cb` (PR #13, rama `chore/limpieza-spring-boot-4`). Tags: `v1.0.0` (Spring Boot 4.1.2), `v1.1.0` (PR #7, rama `fix/seguridad-auth`), `v1.2.0` (PR #9-#12: `fix/eventos`, `chore/ci`, `fix/config` y `fix/frontend-ui`) y `v1.2.1` (PR #13).
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`
> Descartados por decisión: 1.2, 1.3, 1.5 y 3.2 (proyecto académico; el rol de organizador sigue siendo autoasignable). Resueltos: 1.1, 1.7 (opción A: `PUT /perfil/me` exige la contraseña actual + formulario `ProfileEditForm` en el Dashboard; la B, invalidar JWT antiguos, no se hizo), 2.3, 2.16 (opción B: el `subject` del JWT es el id del usuario; `@Valid` en login y perfil), 2.18 (opción A: `/error` público), 2.19 (opción A: handlers 400/409 con mensajes fijos; el catch-all responde "Error interno" y registra el detalle con `log.error`), 2.20, 2.2 (409 con mensaje claro si el usuario tiene eventos, reservas o reseñas), 2.1 (401 en `AuthenticationException` y 403 en `AccessDeniedException`), 2.11 (el interceptor no redirige con un 401 de `/auth/*`), 2.15 (el filtro JWT ya no corta la petición; `HttpStatusEntryPoint` da 401 en rutas protegidas), 2.10 (el registro no guarda `usuario`; la Navbar exige `isLogged`), 2.22 (`ProtectedRoute` expulsa si no hay `usuario`), 4.1 (14 tests: 13 de integración con MockMvc + H2 y 1 unitario con Mockito; `h2` en scope `test`) y 3.5 (seeder con emails según el rol y contraseñas `12345678`; no se hizo la B, `@Profile("dev")`), 1.6 (opción A: `mapToEntity` ya no copia el `id`; nuevo `PUT /api/eventos/{id}` con `@PreAuthorize` ORGANIZADOR/ADMIN que solo deja editar al organizador del evento o a un ADMIN, 403 si no; edita `nombre` y `descripcion`; 4 tests nuevos, 18 en total), 1.4 (opción A: `POST /api/eventos` asigna como organizador al usuario autenticado y se ignora el `organizadorId` del JSON, que deja de ser obligatorio también en el `PUT`; no se hizo la B porque el `POST` solo lo permite a ORGANIZADOR; 1 test nuevo, 19 en total), 2.5 (opción A: `mapToEntity` busca la ubicación con `ubicacionRepository.findById` y responde 404 con `UbicacionNoEncontradaException` si falta o no existe; `UbicacionDTO.id` pasa a `Long`, así que un `id` no numérico da 400; la ubicación es obligatoria al crear; no se hizo la B, crear ubicaciones al vuelo; 1 test nuevo, 20 en total), 4.8 (opción A: `id: number` en `UbicacionDTO.ts` y `UsuarioResponseDTO.ts`, `eventos?` opcional y `dimensiones` en los puestos de `DashboardDTO.ts`, que el Dashboard ya muestra; `AdminPage` y `adminService.deleteUser` usan `number`; no se hizo la B, `openapi-typescript`), 2.21 (opción A: `EventPage.tsx:96,146` usa `event.ubicacion?.nombre ?? "Sin Ubicación"` y el mapa ya se ocultaba sin coordenadas; no se hizo la B, un `ErrorBoundary` en `MainLayout`), 2.8 (opción A: `UsuarioService.java:97` usa `fecha != null ? fecha.toString() : null`, como `EventoService`, y el Dashboard muestra "N/A"; no se hizo la B, `@Column(nullable = false)`, porque con `ddl-auto=update` habría que limpiar antes las filas con `fecha` null; el DTO ya tenía `@NotBlank`), 2.17 (opción A: `try/catch/finally` dentro de la función async, estados `loading` y `error`, mensaje en pantalla y toast si falla; `formatDate` solo con día/mes/año, por lo que el registro y la reserva tampoco muestran la hora; el `parseISO` queda para 2.24), 2.6 (opción A: `Ubicacion.eventos` sin `cascade`; no se hizo la B porque no hay endpoint para borrar ubicaciones), 2.7 (opción A: `Reserva.puesto` pasa a `@ManyToOne(fetch = LAZY)`; en MySQL hay que quitar a mano el UNIQUE antiguo de `puesto_id` con `ALTER TABLE reserva ADD INDEX idx_reserva_puesto (puesto_id), DROP INDEX <UK>` o recrear la BD), 4.12 (opción A: lista blanca `nombre`/`fecha`/`id` en `EventoService.obtenerTodos`; `OrdenInvalidoException` da 400; 2 tests nuevos, 22 en total), 3.12 (opción A: volumen con nombre `mysql_data` en `docker-compose.yml`; `docker compose down` conserva los datos y `down -v` los borra; comprobado borrando y recreando los contenedores), 3.4 (opción A: `show-details=never`) y 3.10 (opción A: `/actuator/health` con `permitAll`, solo esa ruta; el contenedor del backend sale _healthy_; 1 test nuevo que exige 200 y sin `components`, 23 en total), 3.1 (sin las opciones A/B/C: `spring.datasource.url` apunta a `localhost:3307` y en Docker la sobrescribe la variable `SPRING_DATASOURCE_URL` del compose; nuevos scripts `dev:backend-windows` y `dev:windows`, porque `./mvnw` no funciona en el cmd que usa npm en Windows), 3.8 (opción A: `frontend/.dockerignore` con `node_modules` y `dist`, sin `.env` porque Vite incrusta `VITE_API_URL` al compilar; `npm ci --ignore-scripts` en `frontend/Dockerfile`, antes de `COPY . .` para aprovechar la caché de capas), 3.11 (opción A: `charset`, `viewport`, `<title>Feria+</title>` y `lang="es"`; el script del tema cubre `"system"` sin perder `"dark"` elegido a mano), 2.23 (opción A: `/events/:id` fuera de `ProtectedRoute`), 2.14 (opción B: componente `components/Paginacion.tsx` común, con `<Button disabled>` reales, `aria-label` y sin índice -1; el Admin vuelve a la página anterior si borra el último usuario de la última), 2.12 (opción A: "Limpiar" pasa a "Deshacer cambios" con `setUpdateUser(user)`; un único `Dialog` controlado con el estado `editando` y `DialogDescription`), 2.13 (opción A: `value={rol}` y label asociado), 2.9 (opción B: `resolvedTheme` en `ThemeProvider`, usado por el mapa, el `ThemeToggle` y `ui/sonner.tsx`, que leía `next-themes` sin provider y por eso los toasts seguían al sistema; `next-themes` desinstalado), 2.24 (opciones A y B: `lib/fechas.ts` con `formatDate` y `formatFechaHora` sobre `parseISO`; comprobado con zonas de América), 4.7 (opción B: `Label` con `htmlFor`/`id`, `aria-label` y `aria-pressed` en los botones de mostrar contraseña con foco visible, `autoComplete`; extra: el carril del switch en `rem` para que escale con el tamaño de letra y `aria-label` en el switch del tema), 4.13 (opción A: `loading` empieza en `true` en `EventPage` y el carrusel tiene estados `loading`/`error` con `Skeleton`) y 4.4 (opción A: los interceptores ya no muestran toasts; cada página muestra el suyo y la recarga del Dashboard tras editar el perfil tiene su `try/catch`), 4.11 (opción B: `error instanceof FieldError fe ? fe.getField() : error.getObjectName()`, así un error de validación a nivel de clase da 400 con su mensaje en lugar de `ClassCastException` y 500; test unitario `GlobalExceptionHandlerTest` que falla con el código anterior, 24 en total).

## Cómo leer este archivo

- Cada apartado es **una rama**, en el orden en que conviene trabajarlas. Dentro de cada rama, los puntos van en orden de arreglo.
- Prioridad: 🔴 **P1** seguridad / pérdida de datos · 🟠 **P2** rompe un flujo visible · 🟡 **P3** bug menor de UI/UX · 🟢 **P4** configuración / deuda.
- Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) son comunes con `MEJORAS.md` y se conservan porque se citan entre sí.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| #   | Rama                           | Contenido                                        | Prioridad máx. | Puntos |
| --- | ------------------------------ | ------------------------------------------------ | -------------- | ------ |
| 1   | `fix/seguridad-auth`           | Usuarios, admin, auth y errores HTTP             | 🔴 P1          | 0      |
| 2   | `fix/eventos`                  | Crear/editar eventos y modelo JPA                | 🟡 P3          | 0      |
| 3   | `fix/config`                   | Docker, actuator, perfiles                       | 🟢 P4          | 0      |
| 4   | `fix/frontend-ui`              | HTML base, navegación, paginación, accesibilidad | 🟠 P2          | 0      |
| 5   | `chore/limpieza-spring-boot-4` | Fallo latente en la validación                   | 🟢 P4          | 0      |
| 6   | `fix/revision-2026-11`         | Perfil, `PUT` de eventos y perfil `test`         | 🔴 P1          | 4      |

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

### Estado del entorno (2026-10-31)

| Comprobación                                                              | Resultado                                                                                                                                                                                                                         |
| ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME`                                 | ✅                                                                                                                                                                                                                                |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main`                                                                                                                                                                                                                      |
| `.\mvnw.cmd verify` (backend)                                             | ✅ 24 tests (21 de integración + 3 unitarios)                                                                                                                                                                                     |
| `npm run lint` / `npm run build` (frontend)                               | ✅ 0 errores (aviso de bundle > 500 kB, ver 4.6 en `MEJORAS.md`)                                                                                                                                                                  |
| `npm audit` raíz                                                          | ✅ 0 vulnerabilidades                                                                                                                                                                                                             |
| `npm audit` frontend                                                      | ⚠️ 17 (2 bajas + 15 moderadas), solo en herramientas de desarrollo y sin arreglo publicado (ver 5.17 en `MEJORAS.md`). **No usar `npm audit fix --force`**: baja `shadcn` a 1.0 y `typescript-eslint` a 8.14 y rompe build y lint |

---

## 1️⃣ Rama `fix/seguridad-auth` — usuarios, admin y errores HTTP

> Fusionada en `main` (PR #7) y publicada como `v1.1.0`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 2️⃣ Rama `fix/eventos` — crear/editar eventos y modelo JPA

> Fusionada en `main` (PR #9) y publicada en `v1.2.0`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 3️⃣ Rama `fix/config` — Docker, actuator y perfiles

> Fusionada en `main` (PR #11).

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 4️⃣ Rama `fix/frontend-ui` — HTML base, navegación, paginación y accesibilidad

> Fusionada en `main` (PR #12) y publicada como `v1.2.0`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 5️⃣ Rama `chore/limpieza-spring-boot-4` — fallo latente (el resto de la rama está en MEJORAS.md)

> Fusionada en `main` (PR #13) y publicada como `v1.2.1`.

Todos los puntos de esta rama están resueltos (ver "Resueltos" en la cabecera).

---

## 6️⃣ Rama `fix/revision-2026-11` — perfil, `PUT` de eventos y perfil `test` (revisión del 2026-11-02)

- [Hecho] **1.8 🔴 Cambiar el email del perfil no pide la contraseña actual**
  - **Problema:** `B/service/UsuarioService.java:123-128` (`actualizarPerfilPorUsername`) cambia el email sin comprobar `oldPassword`; solo la pide para la contraseña nueva (`:136-142`, arreglo 1.7). El front tampoco la manda si no hay contraseña nueva (`frontend/src/pages/ProfileEditForm.tsx:37`). Como el login es por email, quien tenga un token robado (válido 24 h y sin revocación, 1.7 B) puede cambiar el email y dejar al dueño sin poder entrar: comprobado, `PUT /api/perfil/me` con `{"nombre":"Carlos","email":"atacante@x.com"}` da 200 y después el login con `carlos@gmail.com` da 401. Solo un admin puede devolverle el email desde el panel.
  - **Solución:**
    - **A)** En el servicio, exigir que `oldPassword` coincida también cuando el email cambia (misma comprobación que la contraseña) y, en `ProfileEditForm`, mandar `oldPassword` si cambia el email o la contraseña. Test de integración: cambiar el email sin contraseña da 403.
    - **B)** Además, invalidar los JWT antiguos al cambiar email o contraseña (1.7 B pendiente).

- [Hecho] **2.25 🟡 `PUT /api/eventos/{id}` exige fecha y ubicación pero las ignora**
  - **Problema:** `B/controller/EventoController.java:46` valida con `@Valid EventoDTO`, que obliga a enviar `fecha` y `ubicacion` (`B/DTO/EventoDTO.java:22-25`), pero `B/service/EventoService.java:81-82` solo copia `nombre` y `descripcion` (decisión de 1.6). Comprobado: con `fecha` `2030-01-01` y otra ubicación responde 200 y el evento conserva las antiguas, sin avisar; con solo el nombre da 400. Hoy ninguna pantalla usa el `PUT` (el panel de organizador es 4.3 en `MEJORAS.md`), pero cualquier cliente que lo use pierde esos cambios en silencio.
  - **Solución:**
    - **A)** Que `editar` actualice también `fecha` y `ubicacion` (reutilizando la búsqueda de ubicación de 2.5, con su 404) y documentar qué campos se pueden editar. Test: el `PUT` cambia la fecha.
    - **B)** Un DTO propio para editar (`EventoEdicionDTO` con `nombre` y `descripcion`) para que el contrato solo pida lo que se usa.

- [ ] **2.26 🟢 (latente) Editar un evento sin organizador da 500**
  - **Problema:** `B/service/EventoService.java:78` hace `evento.getOrganizador().getEmail()` cuando quien edita no es admin, y `organizador_id` admite null (`B/model/Evento.java:42-44`). Comprobado guardando un evento sin organizador: un ORGANIZADOR que lo edita recibe 500 (`NullPointerException`, "Error no controlado" en el log) en lugar de 403. Hoy la API no crea eventos sin organizador (1.4) ni deja borrar a un usuario con eventos (2.2), así que solo pasaría con filas antiguas o insertadas a mano.
  - **Solución:**
    - **A)** Comparar sin NPE: `evento.getOrganizador() == null || !evento.getOrganizador().getEmail().equals(email)` → 403 (solo un admin podría editarlo).
    - **B)** Hacer la columna obligatoria (`@JoinColumn(nullable = false)`). Con `ddl-auto=update` (3.3) antes hay que asignar o borrar las filas con `organizador_id` null.

- [Hecho] **3.13 🟢 El perfil `test` va dentro del jar de producción y no arranca**
  - **Problema:** `backend/src/main/resources/application-test.properties` configura H2, pero `h2` está en scope `test` (`backend/pom.xml:73-74`, arreglo 4.1). El fichero se empaqueta en el jar (`BOOT-INF/classes/application-test.properties`) y, si alguien arranca la app con `--spring.profiles.active=test`, falla con `Cannot load driver class: org.h2.Driver` (comprobado con `mvnw package` + `java -jar`). Los tests no se ven afectados.
  - **Solución:**
    - **A)** Moverlo a `backend/src/test/resources/application-test.properties` (la carpeta aún no existe; Maven la reconoce sola). Los tests con `@ActiveProfiles("test")` lo siguen encontrando y deja de llegar al jar. Comprobar: `.\mvnw.cmd verify` en verde y `jar tf target\backend-*.jar` sin `application-test`.
    - **B)** Quitarlo al hacer 5.7 (Testcontainers) en `MEJORAS.md`, que ya lo prevé.
