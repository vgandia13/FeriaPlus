# Feria+ | Proyecto Intermodular - TFG DAW

**Feria+** es una plataforma integral para la gestión y participación en eventos feriales, desarrollada como Proyecto Fin de Grado para el Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Web (DAW). La aplicación permite a organizadores, expositores y visitantes interactuar en un ecosistema digital centralizado para la promoción y asistencia a eventos.

---

## Tecnologías Utilizadas

El proyecto se basa en una arquitectura desacoplada entre cliente y servidor mediante una API REST.

### Backend

- **Java 21** como lenguaje principal.
- **Spring Boot 3.4.1** para el desarrollo del núcleo de la API.
- **Spring Security & JWT (JJWT 0.11.5)**: Autenticación stateless mediante tokens.
- **Spring Data JPA + Hibernate 6**: Gestión de la persistencia de datos.
- **MySQL 8.0**: Base de datos relacional para producción.
- **H2 Database**: Base de datos en memoria para entornos de test.
- **MapStruct 1.5.5**: Mapeo entre entidades y DTOs.
- **Lombok**: Reducción de código repetitivo.
- **SpringDoc OpenAPI 2.3.0**: Documentación automática de la API (Swagger UI).
- **Maven**: Gestor de dependencias y construcción.

### Frontend

- **React 19** con **Vite 8** para una experiencia de usuario rápida y moderna.
- **TypeScript**: Tipado estático para un desarrollo más robusto.
- **Tailwind CSS 4.0**: Framework de utilidades CSS para el diseño.
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
cd Proyecto-Intermodular-2-DAW
```

**2. Construye y levanta los tres contenedores:**

```bash
docker compose up --build
```

El proceso tarda varios minutos la primera vez porque compila el backend con Maven y el frontend con Node. Las siguientes ejecuciones son mucho más rápidas al usar caché.

**3. Espera a que aparezca este mensaje en los logs:**

```
Base de datos poblada con éxito: 1 Organizador, 1 admin, 1 Expositor, 1 Visitante, 9 Ubicaciones y 9 Eventos de prueba.
```

**4. Accede a la aplicación:**

| Servicio           | URL                                         |
| ------------------ | ------------------------------------------- |
| Frontend (React)   | http://localhost:8081                       |
| Backend (API REST) | http://localhost:8080                       |
| Swagger UI         | http://localhost:8080/swagger-ui/index.html |
| Health check       | http://localhost:8080/actuator/health       |

### Usuarios de prueba

Todos los usuarios del seeder tienen la contraseña `123456`.

| Email               | Rol              |
| ------------------- | ---------------- |
| admin@gmail.com     | ROLE_ADMIN       |
| admin@feriaplus.com | ROLE_ORGANIZADOR |
| paco@artesania.com  | ROLE_EXPOSITOR   |
| carlos@gmail.com    | ROLE_VISITANTE   |

### Detener la aplicación

```bash
# Detener los contenedores (conserva los datos de la base de datos)
docker compose down

# Detener y eliminar también el volumen de MySQL (reinicia la DB desde cero)
docker compose down -v
```

---

## Instalación en local (sin Docker)

### Requisitos

- Java 21
- Maven 3.9+
- Node.js 22+
- MySQL 8.0

### Backend

1. Crea una base de datos MySQL llamada `mydatabase`.
2. Edita `backend/src/main/resources/application.properties` con tus credenciales locales:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

3. Desde la carpeta `backend/`, ejecuta:

```bash
./mvnw spring-boot:run
```

### Frontend

1. Desde la carpeta `frontend/`, instala las dependencias:

```bash
npm install
```

2. Lanza el servidor de desarrollo:

```bash
npm run dev
```

La aplicación estará disponible en http://localhost:5173 y apuntará al backend en http://localhost:8080.
