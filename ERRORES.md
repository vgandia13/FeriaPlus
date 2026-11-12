# Errores a corregir — Feria+

> ✅ **0 errores pendientes** (2026-11-03). Todos los fallos de las auditorías del 2026-10-26, 2026-11-02 y 2026-11-03 están resueltos; el detalle de cada arreglo está en el historial de Git (PR #7-#15 y rama `errores-auditoria`).
> Solo **fallos** de la aplicación (incluidos los latentes). Las recomendaciones, la deuda técnica, los tests y las funcionalidades nuevas están en `MEJORAS.md`.
> Ruta backend abreviada: `B/` = `backend/src/main/java/com/example/backend/`

## Cómo leer este archivo

- Prioridad: 🔴 **P1** seguridad / pérdida de datos · 🟠 **P2** rompe un flujo visible · 🟡 **P3** bug menor de UI/UX · 🟢 **P4** configuración / deuda.
- Los números (1.x seguridad, 2.x bugs, 3.x config, 4.x calidad, 5.x mejoras) son comunes con `MEJORAS.md`. Último usado: 1.10, 2.29, 3.13, 4.17, 5.25.
- Cada punto tiene **Problema** y **Solución**. Si hay varias opciones, la **A** es la recomendada.

### Comandos para verificar cada arreglo (PowerShell)

```powershell
cd backend;  .\mvnw.cmd verify;  cd ..
cd frontend; npm run lint; npm run build; cd ..
docker compose up --build   # prueba integrada con MySQL
```

### Estado del entorno (2026-11-03)

| Comprobación                                                              | Resultado                                                                                                                                                                                                                         |
| ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Java 21 (Temurin 21.0.12.1) + `JAVA_HOME`                                 | ✅                                                                                                                                                                                                                                |
| Spring Boot 4.1.2 + springdoc 3.2.0 (Hibernate 7.4, Spring 7, Security 7) | ✅ en `main`                                                                                                                                                                                                                      |
| `.\mvnw.cmd verify` (backend)                                             | ✅ 40 tests (27 de integración + 13 unitarios), comprobado el 2026-11-12                                                                                                                                                          |
| `npm run lint` / `npm run build` (frontend)                               | ✅ 0 errores (bundle inicial de 402 kB tras 4.6); `npx vitest run`: 8 tests                                                                                                                                                                  |
| `npm audit` raíz                                                          | ✅ 0 vulnerabilidades                                                                                                                                                                                                             |
| `npm audit` frontend                                                      | ⚠️ 12 (2 bajas + 10 moderadas, 2026-11-12), solo en herramientas de desarrollo y sin arreglo publicado (ver 5.17 en `MEJORAS.md`). **No usar `npm audit fix --force`**: baja `shadcn` a 1.0 y `typescript-eslint` a 8.14 y rompe build y lint |

---

## Descartados (no son fallos o se decidió no arreglarlos)

- **1.2, 1.3, 1.5 y 3.2**: proyecto académico; el rol de organizador sigue siendo autoasignable.
- **`PUT /api/eventos/{id}` sin `ubicacion.id` da 404 y no 400**: decisión de 2.5 (404 "si falta o no existe").
- **`PUT /api/perfil/me` dice si un email existe sin pedir la contraseña**: `POST /api/auth/register` ya lo revela a cualquiera.
- **3.3 `ddl-auto=update`**: `validate` rompería el primer `docker compose up` con un volumen nuevo (no crea las tablas); la alternativa, Flyway, no compensa en un proyecto académico sin datos reales.
- **Aviso "'jwt.secret' is an unknown property" en VS Code**: es una propiedad propia leída con `@Value`; funciona aunque el editor no la conozca.
