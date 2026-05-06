# Feria+ | Proyecto Intermodular - TFG DAW

**Feria+** es una plataforma integral para la gestión y participación en eventos feriales, desarrollada como Proyecto Fin de Grado para el Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Web (DAW). La aplicación permite a organizadores, expositores y visitantes interactuar en un ecosistema digital centralizado para la promoción y asistencia a eventos.

## Tecnologías Utilizadas

El proyecto se basa en una arquitectura de desacoplamiento entre el cliente y el servidor mediante una API REST.

### Backend

* **Java 21** como lenguaje principal.
* **Spring Boot 4.0.6** para el desarrollo del núcleo de la API.
* **Spring Security & JWT**: Implementación de seguridad basada en tokens sin estado (stateless).
* **Spring Data JPA**: Gestión de la persistencia de datos.
* **MySQL**: Base de datos relacional para producción.
* **H2 Database**: Base de datos en memoria para entornos de test.
* **Lombok**: Reducción de código repetitivo.
* **Maven**: Gestor de dependencias y construcción.

### Frontend

* **React 19** con **Vite 8** para una experiencia de usuario rápida y moderna.
* **TypeScript**: Tipado estático para un desarrollo más robusto.
* **Tailwind CSS 4.0**: Framework de utilidades CSS para el diseño.
* **Shadcn UI**: Componentes de interfaz accesibles y personalizables.
* **React Router 7**: Gestión de navegación Single Page Application (SPA).
* **Axios**: Cliente HTTP para el consumo de la API REST.
* **Sonner**: Sistema de notificaciones reactivas.

## Características Principales

* **Sistema de Autenticación Robusto**: Registro e inicio de sesión de usuarios con contraseñas encriptadas (BCrypt) y gestión de sesiones mediante JWT.
* **Gestión de Roles**: Permisos granulares para diferentes tipos de usuarios:
* `VISITANTE`: Puede explorar eventos e inscribirse en ellos.
* `ORGANIZADOR`: Capacidad para crear, editar y gestionar sus propios eventos.
* `EXPOSITOR` y `ADMIN`: Roles preparados para futuras expansiones de gestión administrativa.
* **Gestión de Eventos**: CRUD completo de eventos, categorización y sistema de inscripción para asistentes.
* **Interfaz Adaptable**: Diseño *mobile-first* totalmente responsivo y soporte nativo para **Modo Oscuro/Claro**.

## 📁 Estructura del Proyecto

```text
├── backend/            # API REST desarrollada con Spring Boot
│   ├── src/main/java   # Código fuente (Controllers, Services, Models, Security)
│   └── pom.xml         # Configuración de Maven y dependencias
├── frontend/           # Aplicación cliente desarrollada con React
│   ├── src/components  # Componentes reutilizables y UI (Shadcn)
│   ├── src/pages       # Vistas principales de la aplicación
│   ├── src/services    # Lógica de comunicación con el backend
│   └── package.json    # Scripts y dependencias de NPM
```

## Instrucciones de instalación

### Requisitos

* Java 17 o superior
* Maven
* Node.js 18+
* MySQL/MariaDB

### Instalación Backend

1. Navega a la carpeta `/backend`.
2. Configura las propiedades de conexión a la base de datos en `src/main/resources/application.properties`.
3. Ejecuta `./mvnw spring-boot:run`.

### Instalación Frontend

1. Navega a la carpeta `/frontend`.
2. Ejecuta `npm install`.
3. Ejecuta `npm run dev`.
