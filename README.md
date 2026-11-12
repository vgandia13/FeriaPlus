# Feria+ | Proyecto Intermodular - TFG DAW

[![CI Status](https://github.com/vgandia13/FeriaPlus/actions/workflows/ci.yml/badge.svg)](https://github.com/vgandia13/FeriaPlus/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL--3.0--only-blue?logo=gnu&logoColor=white)](./LICENSE)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)

**Feria+** es una plataforma integral para la gestión y participación en eventos feriales, desarrollada como Proyecto Fin de Grado para el Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Web (DAW). La aplicación permite a organizadores, expositores y visitantes interactuar en un ecosistema digital centralizado para la promoción y asistencia a eventos.

---

## Tecnologías Utilizadas

El proyecto se basa en una arquitectura desacoplada entre cliente y servidor mediante una API REST.

### Backend

- **Java 21** como lenguaje principal.
- **Spring Boot 4.1.2** para el desarrollo del núcleo de la API.
- **Spring Security & JWT (JJWT 0.13.0)**: Autenticación stateless mediante tokens.
- **Spring Data JPA + Hibernate 7**: Gestión de la persistencia de datos.
- **MySQL 8.0**: Base de datos relacional para producción.
- **H2 Database**: Base de datos en memoria para entornos de test.
- **MapStruct 1.6.3**: Mapeo entre entidades y DTOs.
- **Lombok**: Reducción de código repetitivo.
- **SpringDoc OpenAPI 3.2.0**: Documentación automática de la API (Swagger UI).
- **Maven**: Gestor de dependencias y construcción.

### Frontend

- **React 19** con **Vite 8** para una experiencia de usuario rápida y moderna.
- **TypeScript**: Tipado estático para un desarrollo más robusto.
- **Tailwind CSS 4**: Framework de utilidades CSS para el diseño.
- **Shadcn UI + Radix UI**: Componentes de interfaz accesibles y personalizables.
- **React Router 7**: Gestión de navegación Single Page Application (SPA).
- **Axios**: Cliente HTTP para el consumo de la API REST.
- **React Leaflet + OpenStreetMap**: Mapas interactivos sin coste de licencia.
- **Sonner**: Sistema de notificaciones reactivas.

### Infraestructura

- **Docker + Docker Compose**: Contenedorización y orquestación de los tres servicios.
- **Nginx**: Servidor web para el frontend en producción.

---

## Características Principales

- **Sistema de autenticación**: Registro e inicio de sesión con contraseñas encriptadas (BCrypt) y JWT.
- **Gestión de roles**: Permisos diferenciados por tipo de usuario:
  - `ROLE_VISITANTE`: Explora eventos y se inscribe como asistente.
  - `ROLE_ORGANIZADOR`: Crea y gestiona sus propios eventos.
  - `ROLE_EXPOSITOR`: Reserva puestos en los eventos.
  - `ROLE_ADMIN`: Gestión completa de usuarios y contenido.
- **Gestión de eventos**: Listado paginado, búsqueda por nombre, detalle de evento con mapa de ubicación y sistema de inscripción.
- **Dashboard personal**: Vista de reservas, reseñas y eventos a los que el usuario asiste.
- **Interfaz adaptable**: Diseño responsive y soporte nativo para modo oscuro/claro.
- **Datos de prueba automáticos**: El backend puebla la base de datos en el primer arranque con 4 usuarios, 9 ubicaciones y 9 eventos de ejemplo.

---

## Estructura del Proyecto

```text
├── backend/                        # API REST con Spring Boot
│   ├── src/main/java/com/example/backend/
│   │   ├── controller/             # Endpoints REST
│   │   ├── service/                # Lógica de negocio
│   │   ├── repository/             # Acceso a datos (Spring Data JPA)
│   │   ├── model/                  # Entidades JPA
│   │   ├── DTO/                    # Objetos de transferencia de datos
│   │   ├── security/               # JWT, filtros y configuración de Spring Security
│   │   ├── mapper/                 # MapStruct (Ubicacion ↔ UbicacionDTO)
│   │   ├── bootstrap/              # DataSeeder (datos iniciales)
│   │   └── exception/              # Excepciones personalizadas y handler global
│   ├── src/main/resources/
│   │   └── application.properties  # Configuración principal
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                       # SPA con React + TypeScript
│   ├── src/
│   │   ├── components/             # Componentes reutilizables (Navbar, ProtectedRoute…)
│   │   ├── pages/                  # Vistas principales
│   │   ├── services/               # Comunicación con el backend (Axios)
│   │   ├── types/                  # Interfaces TypeScript
│   │   └── contexts/               # Estado global (AppContext)
│   ├── nginx.conf                  # Configuración de Nginx para SPA
│   └── Dockerfile
└── docker-compose.yml              # Orquestación de los tres servicios
```

---

## Despliegue con Docker (recomendado)

Esta es la forma más rápida de ejecutar el proyecto completo sin instalar Java, Maven ni Node.js en la máquina host.

### Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose) — versión 24 o superior.
- Conexión a Internet en el primer arranque (para descargar las imágenes base).

### Pasos

**1. Clona o descomprime el proyecto** y sitúate en la carpeta raíz (donde está el `docker-compose.yml`):

```bash
cd FeriaPlus
```

**2. Crea el fichero `.env` con los secretos** (no se sube a Git):

```powershell
Copy-Item .env.example .env     # Windows (PowerShell)
cp .env.example .env            # Linux y macOS
```

Rellena `JWT_SECRET` con un valor aleatorio de al menos 32 caracteres y `MYSQL_ROOT_PASSWORD` con la contraseña de MySQL. Para generar el secreto en PowerShell:

```powershell
$b = New-Object byte[] 64; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

MySQL solo aplica `MYSQL_ROOT_PASSWORD` al crear el volumen `mysql_data`: si la cambias después, recrea el volumen con `docker compose down -v` (borra los datos).

**3. Construye y levanta los tres contenedores:**

```bash
docker compose up --build
```

El proceso tarda varios minutos la primera vez porque compila el backend con Maven y el frontend con Node. Las siguientes ejecuciones son mucho más rápidas al usar caché.

**4. Espera a que aparezca este mensaje en los logs:**

```
Base de datos poblada con éxito: 1 Organizador, 1 admin, 1 Expositor, 1 Visitante, 9 Ubicaciones y 9 Eventos de prueba.
```

**5. Accede a la aplicación:**

| Servicio           | URL                                         |
| ------------------ | ------------------------------------------- |
| Frontend (React)   | http://localhost:8081                       |
| Backend (API REST) | http://localhost:8080                       |
| Swagger UI         | desactivado en Docker; solo en local (`mvnw spring-boot:run`): http://localhost:8080/swagger-ui/index.html |
| Health check       | http://localhost:8080/actuator/health       |

### Usuarios de prueba

Todos los usuarios del seeder tienen la contraseña `12345678`.

| Email                 | Rol              |
| --------------------- | ---------------- |
| admin@gmail.com       | ROLE_ADMIN       |
| organizador@gmail.com | ROLE_ORGANIZADOR |
| paco@artesania.com    | ROLE_EXPOSITOR   |
| carlos@gmail.com      | ROLE_VISITANTE   |

### Detener la aplicación

```bash
# Detener los contenedores (conserva los datos en el volumen mysql_data)
docker compose down

# Detener y eliminar también el volumen de MySQL (reinicia la DB desde cero)
docker compose down -v
```

---

## Instalación en local (sin Docker)

### Requisitos

- Java 21 (Maven no hace falta: el proyecto trae el wrapper `mvnw`)
- Node.js 22+
- Docker, solo para la base de datos (o un MySQL 8.0 propio)

### Base de datos

Crea antes el `.env` de la raíz (paso 2 de la instalación con Docker): el backend lo lee al arrancar (`spring.config.import`) para obtener `JWT_SECRET` y `MYSQL_ROOT_PASSWORD`, y Docker Compose lo usa para el contenedor de MySQL.

El backend se conecta por defecto a `localhost:3307`, que es el puerto que publica el contenedor de MySQL. Levanta solo ese servicio:

```bash
docker compose up -d mysql
```

Si prefieres un MySQL instalado en tu equipo, crea la base de datos `mydatabase` y sobrescribe la conexión con la variable de entorno `SPRING_DATASOURCE_URL` (y `SPRING_DATASOURCE_USERNAME` si no usas `root`); la contraseña se toma de `MYSQL_ROOT_PASSWORD` en el `.env`.

### Backend y frontend juntos

Desde la raíz del proyecto, instala las dependencias una vez y lanza los dos servidores:

```bash
npm install
npm install --prefix frontend

npm run dev            # Linux y macOS
npm run dev:windows    # Windows
```

Por separado: `npm run dev:backend` (o `dev:backend-windows`) y `npm run dev:frontend`. Los scripts sin `-windows` usan `./mvnw`, que no funciona en el `cmd` con el que npm ejecuta los scripts en Windows.

La aplicación estará disponible en http://localhost:5173 y apuntará al backend en http://localhost:8080.

## Licencia

Este proyecto está licenciado bajo los términos de la [GNU Affero General Public License v3.0](LICENSE).

Para acuerdos de licencia comercial privativa o implementaciones a medida sin obligaciones AGPL, contactar directamente con el autor.

Copyright (C) 2026 Víctor Gandía Blázquez. Licenciado bajo la [GNU AGPL v3.0 (solo esa versión)](LICENSE).
