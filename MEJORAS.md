# Mejoras recomendadas — Feria+

> Recomendaciones que **no son fallos**: la aplicación funciona, pero conviene hacerlas (deuda técnica, dependencias, documentación, tests y funcionalidades nuevas).
> Los fallos están en `ERRORES.md`. La numeración es común a los dos archivos y se conserva porque los puntos se citan entre sí.
> Resueltas en la PR #8 (`chore/dependencies`): 3.9 (Lombok lo gestiona Spring Boot), 5.14 (jjwt 0.13.0 con la API nueva), 5.15 (Lombok 1.18.48, MapStruct 1.6.3 y `maven-compiler-plugin` de Boot) y 5.17 A (`npm update`).
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`

## Cómo leer este archivo

- Cada apartado indica la **rama** donde conviene hacerlo, normalmente junto con los fallos de esa misma rama en `ERRORES.md`.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

| Apartado | Rama | Puntos |
|---|---|---|
| Tests y CI | `test/unitarios` y siguientes | 5 |
| Eventos | rama nueva (`fix/eventos` ya está fusionada) | 1 |
| Configuración | `fix/config` | 3 |
| Frontend | `fix/frontend-ui` | 5 |
| Limpieza y dependencias | `chore/limpieza-spring-boot-4` | 9 |
| Panel de organizador | `feature/panel-organizador` | 1 |
| JWT en cookie `HttpOnly` (guía) | `feature/auth-cookie` | 1 |
| Login con Google (guía, requiere 5.20) | `feature/google-login` | 1 |
| Opcionales para el TFG | sin rama | 6 |

---

## 🧪 Tests y CI (en este orden)

- [ ] **4.15 🟢 Tests unitarios que no cubre la integración**
  - **Problema:** los 13 tests de integración de 4.1 solo usan `"Bearer tokenfalso"` (ni siquiera tiene forma de JWT) y solo prueban los caminos de error. Hay reglas de `JwtUtils` y `UsuarioService` que ningún test toca.
  - **Solución:**
    - **A)** Por orden de importancia:
      1. `B/security/JwtUtils.java` (JUnit solo, sin Spring ni Mockito: `new JwtUtils()` + `ReflectionTestUtils.setField(..., "JWT_SECRET", ...)`): el token guarda el id del usuario (2.16); un token bien formado pero caducado da `validateToken == false` (2.15; construirlo con `Jwts.builder()` y `.expiration(new Date(0))`, API de jjwt 0.13); un token firmado con otra clave da `false`.
      2. `UsuarioService.actualizarPerfil` (Mockito): asignar `ROLE_ADMIN` lanza `NoAutorizadoException` y nunca llama a `save`; editar a un admin lanza `NoAutorizadoException`.
      3. `UsuarioService.actualizarPerfilPorUsername` (Mockito): con la contraseña actual correcta se guarda la nueva codificada; un email ya registrado lanza `EmailRegistradoException`.
      4. `UsuarioService.eliminarUsuario` (Mockito): sin datos sí llama a `delete`; con reservas, reseñas o asistencias lanza `UsuarioConDatosException`.
    - No merece la pena: `obtenerDatosDashboard` (solo copia datos) ni los handlers de `GlobalExceptionHandler` (ya los cubre la integración).

- [ ] **4.14 🟢 El frontend no tiene tests**
  - **Problema:** no hay framework de tests en `frontend/`. Los arreglos 2.10, 2.11 y 2.22 (Navbar tras el registro, interceptor de 401, `ProtectedRoute` sin `usuario`) solo se han probado a mano.
  - **Solución:**
    - **A)** Vitest + React Testing Library (`npm i -D vitest @testing-library/react @testing-library/jest-dom jsdom`) y un script `"test": "vitest"`. Vitest y no Jest porque reutiliza la configuración de Vite (alias `@`, TypeScript) sin Babel. Primeros tests: `ProtectedRoute` (sin token → `/login`; token sin `usuario` en ruta con rol → `/`), `Navbar` (`usuario` sin token → no muestra "Mi panel") y el interceptor de `apiService.ts` (401 en `/auth/login` no redirige).

- [ ] **5.7 Tests con Testcontainers + `@ServiceConnection`** (Boot 3.1) y **`RestTestClient`** (Spring 7) — tests contra un MySQL real en contenedor (ver 4.1). Al hacerlo, quitar `h2` y `application-test.properties`.

- [ ] **5.18 Tests end-to-end con Playwright** — flujos completos en un navegador real contra backend + frontend (login correcto/incorrecto, registro, acceso a `/admin` según el rol). Playwright mejor que Cypress: multi-navegador, paralelo sin pagar y `npx playwright codegen` para grabar tests. Hacer después de 4.1 y 4.14.

- [ ] **5.19 Pipeline de CI con GitHub Actions** — `.github/workflows/ci.yml` que en cada push y PR ejecute `mvnw verify` (backend) y `npm run lint`, `npm run build` y los tests (frontend). Los runners de GitHub traen Docker, así que también sirve para Testcontainers (5.7). Con protección de rama en `main` para no fusionar con el CI en rojo. En curso en la rama `chore/ci` (Node 22, como `frontend/Dockerfile`; usar `npm ci`, no `npm install`).

---

## 2️⃣ Eventos

> La rama `fix/eventos` se fusionó (PR #9) sin este punto: hacerlo en una rama nueva o junto con la limpieza.

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

- [ ] **5.22 🟢 Avisos de SonarQube Cloud en los Dockerfile** (los 4 de GitHub > Security > Code scanning; va con 3.8 de `ERRORES.md`)
  - **Problema:**
    - #1 `docker:S6505` y #2 `docker:S8543` (`frontend/Dockerfile:5`): `npm install` puede resolver versiones distintas de las de `package-lock.json` y ejecuta los scripts `preinstall`/`postinstall` de cualquier dependencia (vía típica de los ataques a la cadena de suministro).
    - #3 y #4 `docker:S6471` (`backend/Dockerfile:9` y `frontend/Dockerfile:11`): `eclipse-temurin` y `nginx` arrancan como `root`; quien consiga ejecutar código en la app tiene root dentro del contenedor.
  - **Solución:**
    - **A)** #1 y #2: `RUN npm ci --ignore-scripts`. `npm ci` instala exactamente lo que dice el lock (y falla si no cuadra con `package.json`). Hoy ninguna dependencia de `frontend/node_modules` declara scripts `install`/`postinstall`, así que no debería romper nada. Si algún día un paquete lo necesita, ejecuta `npm rebuild <paquete>` solo para ese.
    - **A)** #3: en la etapa final del backend, crea un usuario sin privilegios y cambia a él antes del `ENTRYPOINT` (`RUN groupadd --system app && useradd --system -g app app` + `USER app`). El 8080 es mayor que 1024, así que no hace falta root para abrirlo.
    - **A)** #4: `FROM nginxinc/nginx-unprivileged:alpine` en lugar de `nginx:alpine`. Ya corre como `nginx`, pero escucha en el **8080** (sin root no se pueden abrir puertos menores de 1024): `listen 8080;` en `nginx.conf`, `EXPOSE 8080` y `"8081:8080"` en `docker-compose.yml`. Encaja con el proxy de 5.20.
    - Comprobar: `docker compose build` sin errores; `docker compose exec backend whoami` → `app`; `docker compose exec frontend whoami` → `nginx`. Tras el push, SonarQube cierra los avisos en el siguiente análisis.

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

- [ ] **5.1 🟢 Filtrar `FACTOR_PASSWORD` del JWT** (Security 7)
  - **Problema:** `B/security/JwtUtils.java:29-31` emite `"roles":"ROLE_ORGANIZADOR,FACTOR_PASSWORD"`. Hoy no tiene efecto (nadie lee el claim).
  - **Solución:**
    - **A)** Filtrar las authorities que empiezan por `ROLE_`.
    - **B)** Quitar el claim `roles` (junto con `getRolesFromToken`, ver 4.10).

- [ ] **5.2 🟢 Quitar `spring.jpa.database-platform`** (Hibernate 7)
  - **Problema:** `application.properties:11`; Hibernate detecta MySQL solo y avisa `HHH90000025` al arrancar.
  - **Solución:**
    - **A)** Borrar la línea.

- [ ] **5.16 🟢 `spring-boot-starter-web` → `spring-boot-starter-webmvc`**
  - **Problema:** `pom.xml:39`; Boot 4 renombró el starter y el antiguo está obsoleto.
  - **Solución:**
    - **A)** Cambiar el `artifactId`.

- [ ] **5.17 🟢 Versiones mayores de npm pendientes**
  - **Vulnerabilidades (2026-10-31):** `npm audit` en `frontend/` da 17 (2 bajas + 15 moderadas), pero son solo 2 de origen y las demás son paquetes que dependen de ellas: `minimatch` <11.0.1 (DoS con globs; la traen `eslint` 9 y 10 y `typescript-eslint` 8.72) y `deepmerge` ≤4.3.1 (*prototype poisoning*; la trae la CLI de `shadcn` 4.23, y no hay versión corregida). Afectan solo a herramientas de desarrollo; al bundle solo llega `shadcn/tailwind.css`. **No usar `npm audit fix --force`**: baja `shadcn` a 1.0 y `typescript-eslint` a 8.14 y rompe build y lint. Volver a mirarlo cuando las versiones nuevas de esos paquetes usen `minimatch` 11.
  - **Problema:** las versiones menores ya están al día (`npm update` en la PR #8). Quedan las mayores, que pueden romper cosas: `eslint`/`@eslint/js` 9 → 10, `typescript` 6 → 7, `react-day-picker` 9 → 10, `@types/node` 24 → 26 (en `frontend/`) y `concurrently` 9 → 10 (en la raíz).
  - **Solución:**
    - **A)** Cada una en un commit aparte, leyendo sus notas de versión y comprobando con lint + build (y la app a mano en el caso de `react-day-picker`, que usa el calendario).

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

## 7️⃣ Rama `feature/auth-cookie` — guía para hacerlo tú

- [ ] **5.20 JWT en cookie `HttpOnly` con CSRF activado** (resuelve también 4.9)
  - **Problema:** `AuthService.login` guarda el JWT en `localStorage` (`authService.ts:12`, y otra vez `LoginPage.tsx:34`) y `apiService.ts:26` lo manda como `Authorization: Bearer`. Cualquier JavaScript que se ejecute en la página (un XSS, una dependencia de npm comprometida o una extensión) puede leerlo con `localStorage.getItem('token')` y usarlo desde otro equipo durante 24 h (`JwtUtils.java:18`), sin que se pueda revocar.
  - **Solución:**
    - **A)** El backend entrega el JWT en una cookie `HttpOnly` que JavaScript no puede leer y que el navegador envía solo. Como el navegador la envía también en peticiones provocadas por otras webs, se reactiva CSRF (`SecurityConfig.java:38`). Pasos abajo.

### Qué se gana y qué se pierde

| Se gana | Se pierde / hay que añadir |
|---|---|
| Un XSS ya no puede **robar** el token para usarlo fuera de la página. | Un XSS aún puede hacer peticiones **en nombre del usuario** mientras la pestaña esté abierta. `HttpOnly` reduce el daño, no evita el XSS. |
| El token no aparece en `localStorage`, en `document.cookie` ni en el código del front. | Aparece el riesgo de **CSRF**: otra web puede provocar peticiones que llevan tu cookie. Con `Bearer` no existía, por eso hoy `csrf.disable()` es correcto. |
| Un único sitio decide si hay sesión: el servidor. | El front ya no puede saber si hay sesión por su cuenta, así que hace falta `GET /api/auth/me`. |
| | El logout lo tiene que hacer el backend (`POST /api/auth/logout`), porque JS no puede borrar una cookie `HttpOnly`. |
| | Más configuración: CORS con credenciales, `SameSite`, `Secure` y diferencias entre localhost y producción. Swagger, Postman y curl tienen que manejar cookies y el token CSRF. |

Lo que **no** cambia: sigue sin sesión de Spring (`STATELESS`, `SecurityConfig.java:50-51`), y el JWT sigue sin poder invalidarse antes de caducar (1.7 B en `ERRORES.md`). El logout borra la cookie del navegador, pero una copia robada del token seguiría siendo válida.

### Flujo antes y después

```text
ANTES
LoginPage ──POST /api/auth/login {email,password}──────────► AuthController
          ◄── 200 {token, usuario} ─────────────────────────
localStorage.token = token ; localStorage.usuario = usuario
Cada petición: apiService añade  Authorization: Bearer <token>  ──► JwtAuthenticationFilter lee la cabecera
Recargar (F5): AppContext calcula isLogged = !!localStorage.token   (no pregunta al servidor)
Logout: Navbar borra localStorage                                    (el token sigue valiendo 24 h)

DESPUÉS
Arranque ──GET /api/auth/me──────────────────────────────────► 200 {usuario} | 401
          ◄── Set-Cookie: XSRF-TOKEN=abc   (sin HttpOnly: JS lo lee)
Login ──POST /api/auth/login   Cookie: XSRF-TOKEN=abc   X-XSRF-TOKEN: abc ──► CsrfFilter: ¿cabecera == cookie?
      ◄── 200 {usuario}   Set-Cookie: token=eyJ...; HttpOnly; SameSite=Lax; Path=/; Max-Age=86400
Cada petición: el navegador añade  Cookie: token=...  ──► JwtAuthenticationFilter lee la cookie
POST/PUT/DELETE llevan además  X-XSRF-TOKEN  (axios lo copia de la cookie)
F5: AuthInitializer llama a /me y rellena el contexto
Logout ──POST /api/auth/logout──► Set-Cookie: token=; Max-Age=0
```

¿Por qué funciona el CSRF así? Otra web puede hacer que tu navegador **envíe** la cookie, pero no puede **leerla** (política de mismo origen). Solo tu front puede copiar el valor de `XSRF-TOKEN` en la cabecera, así que una petición sin la cabecera, o con un valor distinto, no la ha hecho tu front.

### Pasos en el backend

**1. Propiedades** — `backend/src/main/resources/application.properties`
- **Qué:** añadir `app.cookie.secure=false` (en producción, `true` vía variable de entorno: `APP_COOKIE_SECURE=true` en `docker-compose.yml`).
- **Por qué:** con `Secure`, el navegador solo envía la cookie por HTTPS. Chrome y Firefox consideran `http://localhost` un contexto seguro y la aceptan igual, pero Safari no, y si accedes por una IP de la red (`http://192.168.x.x`) se descarta sin avisar. Con una propiedad, el mismo código sirve en los dos entornos.
- **Si no:** con `true` fijo, la cookie no se guarda en algunos entornos de desarrollo; con `false` fijo, en producción viajaría por HTTP en claro.
- **Comprobar:** el backend arranca sin `Could not resolve placeholder`.

**2. El login responde con cookie** — `B/controller/AuthController.java:35-54`
- **Qué:** quitar `response.put("token", token)` y enviar el token en la cabecera `Set-Cookie`. Pon la creación de la cookie en un método privado, porque el logout (paso 6) y Google (5.21) la reutilizan:
  ```java
  ResponseCookie cookie = ResponseCookie.from("token", token)
          .httpOnly(true).secure(cookieSecure).sameSite("Lax")
          .path("/").maxAge(Duration.ofHours(24))   // igual que JWT_EXPIRATION_MS
          .build();
  return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
  ```
  (`cookieSecure` es un campo con `@Value("${app.cookie.secure}")`.)
- **Por qué cada atributo:**
  - `HttpOnly`: es la mejora en sí. Sin él, `document.cookie` lo lee igual que `localStorage`.
  - `SameSite=Lax`: el navegador no envía la cookie en `POST`/`fetch` que vienen de **otro sitio**. Es la primera barrera contra CSRF. `Strict` también funciona con esta SPA; `None` solo hace falta en el caso de dominios distintos (ver más abajo).
  - `Path=/`: la cookie viaja a todas las rutas `/api/...`.
  - `Max-Age` igual a la caducidad del JWT: si no lo pones, es una cookie de sesión y se borra al cerrar el navegador; si es mayor, el navegador sigue enviando un JWT caducado y cada petición da 401.
- **Si no:** si dejas el token también en el body, un XSS lo lee de la respuesta y la mejora no sirve de nada.
- **Comprobar:** DevTools > Network > `login` > *Response Headers*: `Set-Cookie: token=eyJ...; Path=/; Max-Age=86400; ...; HttpOnly; SameSite=Lax`. En *Application > Cookies* aparece `token` con la columna *HttpOnly* marcada. Como las cookies no distinguen puertos, la cookie de `localhost:8080` también aparece bajo el origen del front (`localhost:5173`/`8081`). En *Console*, `document.cookie` **no** muestra `token`. Todavía no funcionará el resto de la app: falta el paso 3.

**3. El filtro lee la cookie** — `B/security/JwtAuthenticationFilter.java:33-47`
- **Qué:** obtener el token con `WebUtils.getCookie(request, "token")` (`org.springframework.web.util.WebUtils`, existe en tu `spring-web` 7.0.10) en lugar de, o además de, la cabecera `Authorization`. Si mantienes también la cabecera, Swagger y los tests pueden seguir usando `Bearer`. El resto del filtro (validar, `loadUserById`, `SecurityContextHolder`) no cambia.
- **Por qué:** el navegador envía `Cookie: token=...`, pero nadie añade ya `Authorization`.
- **Si no:** el login va bien, pero cada petición llega sin autenticar: 401, el interceptor manda a `/login` y parece que el login "no hace nada".
- **Comprobar:** tras el login, una petición protegida (`GET /api/perfil/dashboard`) da 200 y en *Request Headers* aparece `Cookie: token=...` sin `Authorization`.

**4. Reactivar CSRF y ajustar CORS** — `B/security/SecurityConfig.java:38` y `:77`
- **Qué:** cambiar `csrf.disable()` por `csrf(csrf -> csrf.spa())` y añadir `"X-XSRF-TOKEN"` a `setAllowedHeaders`. `setAllowCredentials(true)` y los orígenes explícitos (`:75`, `:78`) ya están bien; no los cambies a `"*"`.
- **Sobre `spa()`:** he comprobado con `javap` en tu `spring-security-config-7.1.2.jar` que `CsrfConfigurer.spa()` existe. Configura `CookieCsrfTokenRepository.withHttpOnlyFalse()` (cookie `XSRF-TOKEN` legible por JS, cabecera esperada `X-XSRF-TOKEN`) y un `SpaCsrfTokenRequestHandler` que acepta el token tal cual en la cabecera. **No** añade nada que cree la cookie por adelantado: desde Security 6 el token es *diferido* y la cookie solo se escribe cuando algo lo lee. Por eso el paso 5 lo lee a propósito. Para comprobarlo tú mismo, haz Ctrl+clic en `spa()` en el IDE y lee el código, o busca "Single-Page Applications" en la sección CSRF de la documentación de Spring Security 7.
- **Por qué no excluir `/api/auth/login`:** con `ignoringRequestMatchers` sería más fácil, pero permitiría el *login CSRF* (otra web te inicia sesión en la cuenta del atacante y lo que hagas queda en su cuenta). Como `/me` se llama al arrancar y crea la cookie, no hace falta excluirlo. El logout tampoco se excluye.
- **Si no:** sin la cabecera en CORS, el *preflight* falla (`Request header field x-xsrf-token is not allowed by Access-Control-Allow-Headers`). Con CSRF desactivado y la cookie, cualquier web podría hacer `POST` en nombre del usuario.
- **Tests:** los tests de integración que hacen `POST`/`PUT`/`DELETE` pasarán a dar 403. Hay que añadir `.with(csrf())` (`SecurityMockMvcRequestPostProcessors`, ya tienes `spring-boot-starter-security-test`). Ejecuta `cd backend; .\mvnw test`.
- **Swagger:** `springdoc.swagger-ui.csrf.enabled=true` para que "Try it out" envíe la cabecera.
- **Comprobar (PowerShell, `curl.exe` y no el alias `curl`):**
  ```powershell
  curl.exe -i -c c.txt -b c.txt http://localhost:8080/api/auth/me        # recibe XSRF-TOKEN (paso 5)
  curl.exe -i -b c.txt -H "Content-Type: application/json" --data-binary "@login.json" http://localhost:8080/api/auth/login
  # → 403 (sin cabecera). Repite con  -H "X-XSRF-TOKEN: <valor de XSRF-TOKEN en c.txt>"  → 200 + Set-Cookie token
  ```
  (`login.json` = `{"email":"carlos@gmail.com","password":"12345678"}`, del `DataSeeder`). Un 403 de CSRF tiene el body vacío y no llega al controlador. El 403 de `GlobalExceptionHandler` trae JSON, así que se distinguen.

**5. `GET /api/auth/me`** — `B/controller/AuthController.java`
- **Qué:** un endpoint que devuelve el mismo objeto `usuario` que el login (`name`, `email`, `rol`), o 401 si no hay sesión. Como `/api/auth/**` es público (`SecurityConfig.java:42`), la petición llega al controlador aunque no haya sesión. Allí decides:
  ```java
  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user, CsrfToken csrf) {
      csrf.getToken();                 // fuerza que se escriba la cookie XSRF-TOKEN
      if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      // user.getUsername() es el email (UserDetailsServiceImpl.java:39) → obtenerUsuarioPorEmail
  ```
- **Por qué ya no basta `localStorage`:** JS no puede leer la cookie `HttpOnly`, así que `!!localStorage.getItem("token")` (`AppContext.tsx:45`) ya no tiene nada que mirar. Además, un `usuario` guardado en `localStorage` puede no coincidir con la realidad: la cookie ha caducado, un admin te ha cambiado el rol (`UsuarioService.actualizarPerfil`), has cerrado sesión en otra pestaña o alguien ha editado a mano `"rol":"ROLE_ADMIN"` y ve el panel (el backend lo rechazaría, pero la interfaz mentiría). Solo el servidor sabe si la cookie es válida y quién es el usuario.
- **401 o 204:** 401 es más correcto y el interceptor ya no redirige con errores de `/auth/*` (`apiService.ts:42`, arreglo 2.11). El único inconveniente es una línea roja en la consola en cada visita anónima. 204 evita esa línea, pero entonces el front tiene que distinguir "sin body".
- **Si no:** cada F5 pierde la sesión en la interfaz aunque la cookie siga siendo válida.
- **Comprobar:** con sesión → 200 `{name,email,rol}`. En ventana de incógnito → 401 y `Set-Cookie: XSRF-TOKEN=...` en la respuesta. En *Application > Cookies*, `XSRF-TOKEN` **sin** *HttpOnly*.

**6. `POST /api/auth/logout`** — `B/controller/AuthController.java`
- **Qué:** responder con la cookie `token` vacía y `maxAge(0)`, usando el mismo método del paso 2 (mismo nombre, `Path` y, si algún día lo pones, `Domain`).
- **Por qué:** JS no puede borrar una cookie `HttpOnly`. El navegador identifica una cookie por nombre + dominio + path: si cambia el `Path`, crea una cookie nueva y la original sigue ahí.
- **Alternativa B:** el `LogoutFilter` de Spring (por defecto en `/logout`, no choca con tu ruta): `http.logout(l -> l.logoutUrl("/api/auth/logout").deleteCookies("token").logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))`. Sin el *handler*, redirige a `/login?logout`, que en una API no tiene sentido. La A te deja controlar los atributos de la cookie.
- **Comprobar:** tras el logout, `token` desaparece de *Application > Cookies* y `/me` da 401.

### Pasos en el frontend

**7. axios con credenciales** — `frontend/src/services/apiService.ts:17-36` y `:44-45`
- **Qué:** en `axios.create`, `withCredentials: true` y `withXSRFToken: true`. Borrar el interceptor de petición que lee `localStorage` y las dos líneas `localStorage.removeItem` del 401.
- **Por qué:**
  - `withCredentials`: `5173 → 8080` es una petición entre orígenes distintos. Sin él, el navegador ni envía cookies ni **guarda** el `Set-Cookie` del login.
  - `withXSRFToken`: axios solo copia la cookie `XSRF-TOKEN` a la cabecera `X-XSRF-TOKEN` automáticamente en peticiones al mismo origen. Entre orígenes distintos hay que pedírselo (opción de axios ≥ 1.6; tienes `^1.15.2`). Los nombres por defecto de axios (`xsrfCookieName`, `xsrfHeaderName`) coinciden con los de Spring.
  - En local, JS de `localhost:5173` puede leer la cookie que puso `localhost:8080` porque las cookies no distinguen puertos. Con dominios distintos en producción no podría (ver más abajo).
- **Si no:** login "correcto" sin cookie guardada, o 403 en todos los `POST`.
- **Comprobar:** Network > cualquier petición > *Request Headers*: `Cookie: token=...; XSRF-TOKEN=...` y, en los `POST`/`PUT`/`DELETE`, `X-XSRF-TOKEN: ...`. Ninguna lleva `Authorization`.

**8. Servicio y tipos** — `authService.ts`, `types/AuthResponse.ts`, `LoginPage.tsx:34`, `ProfileEditForm.tsx:44`
- **Qué:** `login` deja de escribir en `localStorage` y devuelve el `usuario`. Añadir `me()` (GET `/auth/me`) y `logout()` (POST `/auth/logout`). Quitar `token` de `AuthResponse`. El compilador te señalará `response.token` en `LoginPage.tsx:34`: bórralo (así se resuelve 4.9). En `ProfileEditForm.tsx:44` basta con `setUsuario`.
- **Por qué:** si queda algún `localStorage.setItem("usuario")`, vuelve a haber dos fuentes de verdad.
- **Comprobar:** `cd frontend; npm run build; npm run lint` sin errores, y `Get-ChildItem src -Recurse -Include *.ts,*.tsx | Select-String localStorage` solo debe encontrar el tema.

**9. Comprobar la sesión al arrancar** — `contexts/AppContext.tsx`, `components/AuthInitializer.tsx`, `components/ProtectedRoute.tsx`
- **Qué:**
  - `AppContext`: el estado inicial pasa a ser `usuario = null` e `isLogged = false`, sin leer `localStorage`, más un estado nuevo `cargando = true` que se expone en el contexto.
  - `AuthInitializer`: ya está montado en `App.tsx:18` y tiene un `useEffect`. Añade ahí `AuthService.me()`, que rellena `usuario`/`isLogged` y hace `setCargando(false)` en un `finally`.
  - `ProtectedRoute`: si `cargando`, devuelve `null` (o un `Skeleton`) **antes** de mirar `isLogged`.
- **Por qué:** en el primer render, `/me` aún no ha respondido. Sin `cargando`, `ProtectedRoute` ve `isLogged = false` y redirige a `/login` en cada F5 sobre una ruta protegida.
- **Ojo:** `StrictMode` (`main.tsx:9`) ejecuta el efecto dos veces en desarrollo y verás dos llamadas a `/me`. Es normal y no pasa en el build.
- **Comprobar:** inicia sesión, ve a `/dashboard` y pulsa F5: sigues dentro y en Network aparece `/auth/me` con 200.

**10. Logout de verdad** — `components/Navbar.tsx:11-17`
- **Qué:** `handleLogout` hace `await AuthService.logout()` y después limpia el contexto y navega.
- **Si no:** la interfaz parece cerrada, pero la cookie sigue ahí; al pulsar F5, `/me` te vuelve a meter.
- **Comprobar:** logout, F5 y `/me` responde 401.

### Localhost frente a producción

- **Origen** = esquema + host + **puerto** (lo usa CORS). **Sitio** = esquema + dominio registrable, **sin** puerto ni subdominio (lo usa `SameSite`). Las cookies se asocian al host e ignoran el puerto.
- **Hoy en local:** front en `localhost:5173` (Vite) o `localhost:8081` (nginx en Docker), back en `localhost:8080`. Son **orígenes distintos, pero el mismo sitio**: hace falta CORS con credenciales, `withCredentials` y `withXSRFToken`, y `SameSite=Lax` funciona.
- **Producción A (recomendada): mismo dominio con proxy en nginx.** En `frontend/nginx.conf`:
  ```nginx
  location /api/ {
      proxy_pass http://backend:8080;   # nombre del servicio en docker-compose.yml (misma red app-network)
      proxy_set_header Host $host;
      proxy_set_header X-Forwarded-Proto $scheme;
  }
  ```
  y `VITE_API_URL=/api` (relativa). El navegador solo ve un origen: no hay CORS, `withXSRFToken` deja de hacer falta (no molesta), puedes usar `SameSite=Strict` y el HTTPS se termina en nginx. Además, el backend ya no necesita publicar el puerto 8080.
  - Fíjate en la **barra** de `proxy_pass`: `http://backend:8080` conserva `/api/...`; con `http://backend:8080/`, nginx quita `/api/` y Spring recibe `/auth/login` → 404.
  - nginx no arranca si no resuelve `backend`, así que añade `depends_on: [backend]` al servicio `frontend`.
  - Lo puedes probar hoy en `http://localhost:8081`.
- **Producción B: dominios distintos.**
  - Subdominios del mismo sitio (`app.feria.es` + `api.feria.es`): `SameSite=Lax` sigue sirviendo, pero hace falta CORS con ese origen. Además, JS de `app.` no puede leer la cookie `XSRF-TOKEN` que pone `api.` si la cookie no lleva `Domain=feria.es`. Para eso, tu propio `CookieCsrfTokenRepository` con `setCookieCustomizer(c -> c.domain("feria.es"))` pasado con `csrfTokenRepository(...)`; comprueba leyendo `spa()` si al combinarlos se respeta tu repositorio.
  - Sitios distintos (`feria.vercel.app` + `api.onrender.com`): la cookie es de terceros, así que necesita `SameSite=None; Secure`, y Safari, Firefox y los modos de privacidad de Chrome pueden bloquearla igualmente. JS tampoco puede leer el `XSRF-TOKEN` del otro dominio, y habría que devolver el token CSRF en el body. **Evítalo**: usa el proxy o los *rewrites* del hosting.

| | Local actual | Prod A (proxy) | Prod B (subdominios) | Prod B (sitios distintos) |
|---|---|---|---|---|
| CORS con credenciales | Sí | No | Sí | Sí |
| `SameSite` | `Lax` | `Lax`/`Strict` | `Lax` | `None` + `Secure` |
| JS lee `XSRF-TOKEN` | Sí (mismo host) | Sí | Solo con `Domain=` | No |
| `VITE_API_URL` | `http://localhost:8080/api` | `/api` | `https://api.feria.es/api` | URL completa |

### Errores típicos

| Síntoma | Causa probable | Cómo detectarlo |
|---|---|---|
| `...must not be the wildcard '*' when the request's credentials mode is 'include'` | `allowedOrigins("*")` o `allowCredentials(false)` | Console; Network > petición `OPTIONS` > *Response Headers* (`Access-Control-Allow-Origin`, `-Allow-Credentials`) |
| `Request header field x-xsrf-token is not allowed` | Falta en `setAllowedHeaders` | Console; preflight `OPTIONS` fallido |
| Login 200 pero no aparece la cookie `token` | Falta `withCredentials`; `Secure` sobre HTTP que no es localhost; `SameSite=None` sin `Secure` | Network > `login` > pestaña *Cookies*: el `Set-Cookie` sale con un aviso amarillo y el motivo del bloqueo |
| La cookie existe pero no se envía | Otra instancia de axios o `fetch` sin credenciales; `Path` distinto; cookie de terceros bloqueada | *Request Headers* sin `Cookie`; en la pestaña *Cookies*, activar "show filtered out request cookies" |
| 403 en todos los `POST`/`PUT`/`DELETE` | Falta `X-XSRF-TOKEN` (`withXSRFToken`), aún no existe la cookie `XSRF-TOKEN` (no se llamó a `/me`) o la cookie es `HttpOnly` | `logging.level.org.springframework.security=DEBUG` muestra `Invalid CSRF token found`; body del 403 vacío |
| Los tests del backend dan 403 | MockMvc sin `.with(csrf())` | `.\mvnw test` |
| Cada F5 te manda a `/login` | `ProtectedRoute` no espera a `cargando` | Network: la redirección ocurre antes de que responda `/me` |
| Tras el logout, F5 te vuelve a meter | La cookie de borrado no tiene el mismo `Path`/`Domain` | *Application > Cookies*: siguen dos `token` o el original |
| Swagger "Try it out" da 403 | Swagger no envía el token CSRF | `springdoc.swagger-ui.csrf.enabled=true` |

### Preguntas de repaso

1. ¿Qué protege exactamente `HttpOnly` frente a un XSS y qué sigue pudiendo hacer el atacante?
2. ¿Por qué con `Authorization: Bearer` no hacía falta CSRF y con una cookie sí?
3. Si ya tienes `SameSite=Lax`, ¿por qué mantener además el token CSRF? Piensa en otras apps en `localhost:XXXX` y en subdominios.
4. ¿Qué diferencia hay entre *origen* y *sitio*, y cuál afecta a CORS y cuál a `SameSite`?
5. ¿Por qué la cookie `XSRF-TOKEN` **no** es `HttpOnly` y por qué eso no es un fallo de seguridad?
6. ¿Qué es un token CSRF *diferido* en Spring Security 6+ y qué problema te causa en una SPA?
7. ¿Por qué el frontend ya no puede decidir solo si el usuario está logueado y qué ventajas tiene preguntarlo con `/me`?
8. Si te roban la cookie `token`, ¿el logout lo arregla? ¿Qué harías para poder revocar JWT?
9. ¿Por qué no puedes usar `allowedOrigins("*")` con `allowCredentials(true)`?
10. ¿Qué ventajas tiene servir front y API bajo el mismo dominio con un proxy, frente a dominios separados?
11. ¿Por qué no excluir `/api/auth/login` de CSRF? ¿Qué es el *login CSRF*?

---

## 8️⃣ Rama `feature/google-login` — guía para hacerlo tú (después de 5.20)

- [ ] **5.21 "Iniciar sesión con Google"** (Google Identity Services + verificación del ID token en el backend)
  - **Problema:** solo hay login con email y contraseña, y el registro no verifica que el email sea de quien lo escribe.
  - **Solución:**
    - **A)** El front muestra el botón de Google (`@react-oauth/google`) y obtiene un **ID token**: un JWT firmado por Google con `sub`, `email`, `email_verified`, `name`, `aud` (tu Client ID), `iss` y `exp`. Lo envía al backend, que lo verifica con `GoogleIdTokenVerifier`, busca o crea el usuario y responde con **nuestro** JWT en la cookie de 5.20. Sin `oauth2Login` ni sesiones de Spring.
    - **B)** `oauth2Login()` de Spring: flujo con redirecciones en el servidor, `client secret` y sesión HTTP (`JSESSIONID`). Choca con `STATELESS` y con tener un único mecanismo (nuestro JWT). Descartada.

### Qué se gana y qué se pierde

| Se gana | Se pierde / riesgos |
|---|---|
| El usuario no crea ni recuerda otra contraseña. | Dependes de Google: proyecto en Google Cloud, Client ID, pantalla de consentimiento y orígenes autorizados. |
| Google ha verificado el email (`email_verified`), cosa que tu registro no hace. | Hay que decidir qué pasa si el email ya existe (vinculación de cuentas, ver paso 6). |
| Reutilizas todo 5.20: cookie, `/me`, logout y CSRF. | Cargas un script de `accounts.google.com` en tu página (a tener en cuenta si añades CSP). |
| | El usuario de Google no conoce su contraseña y no puede cambiarla desde el perfil (`UsuarioService.java:137` exige la actual). |

### Flujo

```text
LoginPage <GoogleLogin> ── popup de accounts.google.com ── el usuario elige cuenta
          ◄── credential = ID token (JWT firmado por Google, aud = tu Client ID, caduca en ~1 h)
POST /api/auth/google {credential}   + Cookie XSRF-TOKEN + X-XSRF-TOKEN   (CSRF de 5.20)
   AuthController
     → GoogleIdTokenVerifier.verify(): firma (claves públicas de Google, cacheadas), aud, iss, exp
     → ¿email_verified?  no → 401
     → UsuarioService: buscar por email | crear ROLE_VISITANTE con contraseña aleatoria
     → JwtUtils.generateToken(...) → Set-Cookie: token=... (igual que el login normal)
   ◄── 200 {usuario}
A partir de aquí, igual que con contraseña: /me, logout, CSRF.
```

El token de Google se usa **una vez** para saber quién es el usuario. No se guarda ni se usa como sesión.

### Pasos

**1. Client ID en Google Cloud Console**
- **Qué:** crear un proyecto y configurar la *pantalla de consentimiento de OAuth* (tipo externo, modo *Testing*, añade tus cuentas como *test users*). Después, *Credentials > Create credentials > OAuth client ID > Web application*. En **Authorized JavaScript origins** pon `http://localhost`, `http://localhost:5173` y `http://localhost:8081` (y el dominio de producción). Google pide `http://localhost` además de las variantes con puerto. No hace falta *redirect URI*, porque el flujo de popup no redirige.
- **Por qué:** Google solo entrega ID tokens a orígenes registrados para ese Client ID. El **Client ID es público**: acaba en el bundle de JS igualmente. El *client secret* no se usa en este flujo (la verificación usa las claves públicas de Google), así que no lo pongas en ningún sitio.
- **Si no:** `Error 400: origin_mismatch` o `[GSI_LOGGER]: The given origin is not allowed for the given client ID` en la consola. Los cambios de orígenes pueden tardar unos minutos en aplicarse. `127.0.0.1` y `localhost` son orígenes distintos.
- **Comprobar:** tienes un `xxxx.apps.googleusercontent.com`.

**2. Configuración en los dos lados** — `frontend/.env`, `frontend/src/vite-env.d.ts`, `application.properties`, `frontend/Dockerfile`, `docker-compose.yml`
- **Qué:**
  - Front: `VITE_GOOGLE_CLIENT_ID=...` en `frontend/.env`, y su tipo en `vite-env.d.ts` (`interface ImportMetaEnv { readonly VITE_GOOGLE_CLIENT_ID: string }`).
  - Back: `google.client-id=${GOOGLE_CLIENT_ID}` en `application.properties` y la variable en el servicio `backend` de `docker-compose.yml`.
  - Los dos con el **mismo** valor.
- **Por qué el backend lo necesita:** es la *audiencia* esperada. Sin comprobar `aud`, el backend aceptaría ID tokens emitidos para **otra** app. Cualquier web con "Login con Google" podría reutilizar los tokens de sus usuarios para entrar como ellos en Feria+ (sustitución de token).
- **`VITE_` en Docker:** Vite sustituye `import.meta.env.VITE_*` **al compilar** (`RUN npm run build`, `frontend/Dockerfile:7`), no al arrancar el contenedor. Hoy funciona por casualidad: no hay `.dockerignore` y `COPY . .` copia `frontend/.env`. Si haces 3.8 y excluyes `.env`, o la variable no está en ese archivo, el bundle tendrá `undefined`, y `environment:` en `docker-compose.yml` no sirve (es tiempo de ejecución de nginx). Solución: `ARG VITE_GOOGLE_CLIENT_ID` + `ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID` antes del `RUN npm run build`, y `build: { context: ./frontend, args: { VITE_GOOGLE_CLIENT_ID: ... } }` en compose. Lo mismo vale para `VITE_API_URL`.
- **Ojo:** `frontend/.env` está versionado en Git. El Client ID puede ir ahí; un *secret* nunca.
- **Comprobar:** tras `npm run build`, `Select-String -Path dist\assets\*.js -Pattern "apps.googleusercontent.com"` encuentra el ID. En dev, reinicia `npm run dev` después de editar `.env`.

**3. Dependencia** — `backend/pom.xml`
- **Qué:** añadir `com.google.api-client:google-api-client`. Spring Boot no gestiona su versión: pon la última estable de Maven Central. No te doy un número para no inventarlo; búscalo en central.sonatype.com o con `.\mvnw versions:display-dependency-updates`. Trae como transitivas `google-http-client` (`NetHttpTransport`) y `google-http-client-gson` (`GsonFactory`); compruébalo con `.\mvnw dependency:tree "-Dincludes=com.google.http-client"`. Si `GsonFactory` no aparece, añade `google-http-client-gson`.
- **Por qué esta librería y no jjwt a mano:** descarga y cachea las claves públicas de Google, que cambian periódicamente, y comprueba firma, `iss`, `aud` y `exp`. Hecho a mano es fácil olvidar una de esas comprobaciones.
- **Comprobar:** `.\mvnw -q compile`.

**4. Bean del verificador** — p. ej. `B/security/GoogleConfig.java` (o un `@Bean` en `SecurityConfig`)
- **Qué:**
  ```java
  @Bean
  GoogleIdTokenVerifier googleVerifier(@Value("${google.client-id}") String clientId) {
      return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
              .setAudience(List.of(clientId)).build();
  }
  ```
- **Por qué un bean (una sola instancia):** el verificador cachea las claves de Google. Si creas uno por petición, las descarga cada vez. El emisor (`accounts.google.com` / `https://accounts.google.com`) ya lo comprueba por defecto.
- **Si no pones `setAudience`:** vuelves al problema del paso 2.

**5. DTO** — `B/DTO/GoogleLoginDTO.java`
- **Qué:** un campo `@NotBlank String credential`, con Lombok como `LoginRequestDTO`.
- **Por qué:** con `@Valid`, un body vacío da 400 a través del handler existente (2.19), en vez de un `NullPointerException`.

**6. Buscar o crear el usuario** — `B/service/UsuarioService.java`
- **Qué:** un método `@Transactional` del tipo `obtenerOCrearUsuarioGoogle(email, nombre)`:
  ```java
  Usuario u = usuarioRepository.findByEmail(email).orElseGet(() -> {
      Usuario n = new Usuario();
      // nombre, email, rol = Rol.ROLE_VISITANTE (fijo, nunca del cliente)
      n.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
      return usuarioRepository.save(n);
  });
  ```
- **Por qué la contraseña aleatoria encriptada:** `password` es `@NotBlank` (`Usuario.java:37`) y Hibernate Validator la valida al persistir (`ConstraintViolationException` si va vacía). Como es aleatoria y nadie la conoce, nadie puede entrar en esa cuenta con contraseña. Una contraseña fija ("google") sería una puerta trasera, y una sin encriptar no casaría nunca con BCrypt.
- **Por qué `ROLE_VISITANTE` fijo:** si aceptaras el rol desde la petición, cualquiera podría pedir un rol distinto (lo que ya se evita en `mapToEntity`, `UsuarioService.java:199-203`).
- **`nombre` también es `@NotBlank`** y el claim `name` puede venir vacío: usa como alternativa la parte del email antes de la `@`.
- **Si ya existe una cuenta con ese email:**
  - Con el código de arriba, Google **entra en la cuenta existente**: conserva su rol (incluido `ROLE_ADMIN`, como `admin@gmail.com` de `DataSeeder.java:50`) y su contraseña sigue funcionando. Es una vinculación automática por email.
  - Riesgo, *pre-account hijacking*: tu registro no verifica emails. Un atacante puede registrar `victima@gmail.com` con su propia contraseña antes que la víctima; cuando ella entre con Google, usará una cuenta de la que el atacante también conoce la contraseña.
  - Otro caso: si el usuario cambia su email en el perfil (`PUT /perfil/me`), su próximo login con Google crea una cuenta **nueva**.
  - **A)** (proyecto académico) Vincular por email solo si `email_verified` es `true`, y dejar el riesgo documentado.
  - **B)** (robusta) Columna `googleSub` (única, nullable) en `Usuario`. Se busca primero por `sub`, que Google no cambia nunca, al contrario que el email. Si no hay coincidencia y existe una cuenta con ese email sin `googleSub`, se responde 409 ("ya existe una cuenta con ese email, inicia sesión con contraseña") y la vinculación se ofrece desde el perfil con la sesión iniciada. Con `ddl-auto=update` (3.3), Hibernate añade la columna solo.
  - Carrera: dos primeros logins simultáneos chocan con `unique` en `email`. `DataIntegrityViolationException` ya da 409 (`GlobalExceptionHandler.java:125`); es aceptable.
- **Comprobar:** tests de Mockito como los de 4.15: si el email existe, no se llama a `save`; si no existe, se guarda con `ROLE_VISITANTE` y una contraseña no vacía.

**7. `POST /api/auth/google`** — `B/controller/AuthController.java`
- **Qué:**
  ```java
  GoogleIdToken idToken = googleVerifier.verify(dto.getCredential());   // null si firma/aud/exp no valen
  if (idToken == null || !Boolean.TRUE.equals(idToken.getPayload().getEmailVerified()))
      throw new BadCredentialsException("Token de Google no válido");
  ```
  Luego `usuarioService.obtenerOCrearUsuarioGoogle(...)`. `jwtUtils.generateToken` pide un `Authentication` y aquí no hay `AuthenticationManager`: créalo con `userDetailsService.loadUserById(id)` y `new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities())`, que es el mismo patrón que `JwtAuthenticationFilter.java:52-57`. Saca a un método privado lo que comparte con `login` (cookie + mapa `usuario`), para que las dos respuestas sean idénticas.
- **Por qué `BadCredentialsException`:** es una `AuthenticationException` y tu handler ya responde 401 (2.1).
- **Errores de red:** `verify` lanza `IOException` si no puede descargar las claves (contenedor sin internet). Hoy acabaría en el 500 genérico "Error interno". Mira los logs.
- **Por qué devolver nuestro JWT y no el de Google:** el de Google caduca en una hora, su `aud` es el Client ID, tu filtro solo valida tokens firmados con tu clave, y el rol vive en tu base de datos.
- **CSRF:** es un `POST`, así que va protegido por 5.20. No lo excluyas, por el *login CSRF*.
- **Comprobar sin front:** con el flujo `curl.exe` de 5.20 y `{"credential":"abc"}` → 401.

**8. Proveedor en el front** — `frontend/package.json`, `frontend/src/main.tsx`
- **Qué:** `cd frontend; npm i @react-oauth/google` y envolver `<App />` con `<GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>`.
- **Por qué:** el proveedor carga una sola vez el script de GIS (`https://accounts.google.com/gsi/client`) y comparte el Client ID con los botones.
- **Comprobar:** Network > `gsi/client` con 200 y sin errores `[GSI_LOGGER]` en la consola.

**9. Botón y servicio** — `frontend/src/services/authService.ts`, `frontend/src/pages/LoginPage.tsx`
- **Qué:** `AuthService.loginGoogle(credential)` hace `POST /auth/google` y devuelve el `usuario`. En `LoginPage`, debajo del formulario: `<GoogleLogin onSuccess={(r) => ...r.credential...} onError={...} locale="es" />`. Saca a una función lo que hoy hace `handleLogin` tras el login (`LoginPage.tsx:35-41`: `setUsuario`, `setIsLogged`, `toast`, `navigate`) para no duplicarlo.
- **Por qué `GoogleLogin` y no `useGoogleLogin`:** `GoogleLogin` devuelve `credential`, el **ID token**, que es lo que comprueba `GoogleIdTokenVerifier`. `useGoogleLogin` usa el flujo OAuth2 de *access token* o *code*, pensado para llamar a APIs de Google, no para identificar al usuario ante tu backend.
- **Comprobar:**
  - Network > `POST /api/auth/google`: el *payload* es `{credential: "eyJ..."}` y la respuesta, 200 con `Set-Cookie: token=...`.
  - Para ver qué trae el token de Google sin pegarlo en webs externas, en la consola: `JSON.parse(atob(t.split('.')[1].replace(/-/g,'+').replace(/_/g,'/')))`. `aud` es tu Client ID y `email_verified` es `true`.
  - En la base de datos: `docker compose exec mysql mysql -uroot -prootpassword mydatabase -e "select id,email,rol,left(password,7) from usuarios"` muestra una fila nueva con `ROLE_VISITANTE` y la contraseña empezando por `$2a$`.
  - Entra con una cuenta de Google cuyo email ya exista: mismo `id` y mismo rol.

### Errores típicos

| Síntoma | Causa probable | Cómo detectarlo |
|---|---|---|
| `origin_mismatch` / `The given origin is not allowed` | Falta el origen exacto (con puerto, y `http://localhost` sin puerto) en la consola de Google | Console `[GSI_LOGGER]`; usar `localhost`, no `127.0.0.1` |
| El botón no aparece o sale `Missing required parameter: client_id` | `VITE_GOOGLE_CLIENT_ID` es `undefined`: no reiniciaste `npm run dev`, o en Docker no llegó al build (paso 2) | `Select-String` en `dist\assets\*.js` |
| `Access blocked` / `access_denied` al elegir cuenta | Pantalla de consentimiento en *Testing* y la cuenta no es *test user* | Mensaje en el popup |
| 401 en `/api/auth/google` con un login real | `google.client-id` distinto o vacío en el backend (falla `aud`), reloj del servidor desfasado (`exp`/`iat`) o `email_verified = false` | Decodificar el token en la consola y comparar `aud`; logs del backend |
| 403 en `/api/auth/google` | CSRF: no se llamó a `/me` antes o falta `withXSRFToken` | Igual que en 5.20 |
| 500 "Error interno" | `IOException` descargando las claves, o `nombre` vacío (`ConstraintViolationException`) | `docker compose logs backend` |
| El popup se cierra y no pasa nada; error de `Cross-Origin-Opener-Policy` | Una cabecera COOP `same-origin` en nginx bloquea la comunicación con el popup | Console; usa `same-origin-allow-popups` si añades COOP |
| Falla solo en un navegador | Bloqueador de anuncios sobre `accounts.google.com` o restricciones de cookies de terceros/FedCM | Console `[GSI_LOGGER]`; probar en una ventana sin extensiones |

### Preguntas de repaso

1. ¿Qué diferencia hay entre un **ID token** y un **access token** de OAuth2? ¿Cuál necesitas para autenticar al usuario en tu backend y por qué?
2. ¿Qué comprueba `GoogleIdTokenVerifier` y qué pasaría si no comprobaras `aud`?
3. ¿Por qué el Client ID puede ser público y el *client secret* no? ¿Por qué este flujo no necesita el secret?
4. ¿Por qué no usar directamente el ID token de Google como sesión en lugar de emitir tu propio JWT?
5. ¿Por qué descartar `oauth2Login()` en esta arquitectura? ¿Cuándo sería la mejor opción?
6. Si ya existe una cuenta con ese email, ¿la vincularías automáticamente? Explica el *pre-account hijacking*.
7. ¿Por qué identificar al usuario de Google por `sub` es más robusto que por `email`?
8. ¿Por qué se crea la contraseña aleatoria y encriptada en vez de dejarla vacía o fija?
9. ¿Por qué las variables `VITE_` no se pueden cambiar con `environment:` en Docker y cómo se pasan al build?
10. Spring Security trae `NimbusJwtDecoder` (resource server). ¿Podrías verificar el ID token con él en lugar de `GoogleIdTokenVerifier`? ¿Qué tendrías que configurar a mano?

---

## ➕ Opcionales para ampliar el TFG (sin rama)

- [ ] **5.6 Soporte de Docker Compose en desarrollo** (Boot 3.1) — opción A de 3.1.
- [ ] **5.9 Versionado de API nativo** (Spring 7) — útil si se cambia el contrato (p. ej. al quitar `organizadorId` en 1.4).
- [ ] **5.13 DTOs como `record`** (Java 16+) — los de solo lectura (`UsuarioResponseDTO`, `DashboardDTO`, `LoginRequestDTO`).
- [ ] **5.11 Hilos virtuales** (Boot 3.2, Java 21) — `spring.threads.virtual.enabled=true`.
- [ ] **5.12 Logs estructurados en JSON** (Boot 3.4) — `logging.structured.format.console=ecs`.
- [ ] **5.10 MFA / segundo factor** (Security 7) — `@EnableMultiFactorAuthentication` para `ROLE_ADMIN`.
