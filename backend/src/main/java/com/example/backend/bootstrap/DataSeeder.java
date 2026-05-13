package com.example.backend.bootstrap;

import com.example.backend.model.Evento;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.model.Ubicacion;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.repository.UbicacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            UbicacionRepository ubicacionRepository,
            PasswordEncoder passwordEncoder) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos datos si la tabla de eventos está vacía
        if (eventoRepository.count() < 9 && usuarioRepository.count() < 4 && ubicacionRepository.count() < 9) {

            // 1. Crear un usuario organizador (Requisito por la relación @ManyToOne)
            Usuario organizador = new Usuario();
            organizador.setNombre("Ayuntamiento Test");
            organizador.setEmail("admin@feriaplus.com");
            organizador.setPassword(passwordEncoder.encode("123456"));
            organizador.setRol(Rol.ROLE_ORGANIZADOR);

            Usuario admin = new Usuario();
            admin.setNombre("Victor");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRol(Rol.ROLE_ADMIN);

            Usuario visitante = new Usuario();
            visitante.setNombre("Carlos Visitante");
            visitante.setEmail("carlos@gmail.com");
            visitante.setPassword(passwordEncoder.encode("123456"));
            visitante.setRol(Rol.ROLE_VISITANTE);

            Usuario expositor = new Usuario();
            expositor.setNombre("Artesanías Paco");
            expositor.setEmail("paco@artesania.com");
            expositor.setPassword(passwordEncoder.encode("123456"));
            expositor.setRol(Rol.ROLE_EXPOSITOR);

            usuarioRepository.save(visitante);
            usuarioRepository.save(expositor);
            usuarioRepository.save(organizador);
            usuarioRepository.save(admin);

            // 2. Crear Ubicaciones para los eventos
            Ubicacion u1 = new Ubicacion();
            u1.setNombre("Plaza Mayor");
            u1.setLatitud(40.415363);
            u1.setLongitud(-3.707398);
            ubicacionRepository.save(u1);

            Ubicacion u2 = new Ubicacion();
            u2.setNombre("Parque Central");
            u2.setLatitud(40.419);
            u2.setLongitud(-3.693);
            ubicacionRepository.save(u2);

            Ubicacion u3 = new Ubicacion();
            u3.setNombre("Paseo Marítimo");
            u3.setLatitud(39.462);
            u3.setLongitud(-0.323);
            ubicacionRepository.save(u3);

            Ubicacion u4 = new Ubicacion();
            u4.setNombre("Centro Cívico");
            u4.setLatitud(41.385);
            u4.setLongitud(2.173);
            ubicacionRepository.save(u4);

            Ubicacion u5 = new Ubicacion();
            u5.setNombre("Recinto Ferial");
            u5.setLatitud(37.382);
            u5.setLongitud(-5.973);
            ubicacionRepository.save(u5);

            Ubicacion u6 = new Ubicacion();
            u6.setNombre("Feria Valencia");
            u6.setLatitud(39.5039);
            u6.setLongitud(-0.4154);
            ubicacionRepository.save(u6);

            Ubicacion u7 = new Ubicacion();
            u7.setNombre("Jardines de Viveros");
            u7.setLatitud(39.4800);
            u7.setLongitud(-0.3660);
            ubicacionRepository.save(u7);

            Ubicacion u8 = new Ubicacion();
            u8.setNombre("La Marina de València");
            u8.setLatitud(39.4605);
            u8.setLongitud(-0.3294);
            ubicacionRepository.save(u8);

            Ubicacion u9 = new Ubicacion();
            u9.setNombre("Parque de Orriols");
            u9.setLatitud(39.4938);
            u9.setLongitud(-0.3643);
            ubicacionRepository.save(u9);

            // 3. Crear eventos ficticios con imágenes reales (de Unsplash) usando setters
            Evento e1 = new Evento();
            e1.setNombre("Feria de Artesanía Local");
            e1.setDescripcion(
                    "La mejor feria de la ciudad con productos 100% hechos a mano por artesanos de la comarca.");
            e1.setFecha(LocalDate.now().plusDays(5));
            e1.setImagenUrl(
                    "https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=800&q=80");
            e1.setOrganizador(organizador);
            e1.setUbicacion(u1);

            Evento e2 = new Evento();
            e2.setNombre("Mercado Gastronómico");
            e2.setDescripcion(
                    "Degusta los mejores platos típicos, quesos y vinos de la región en un ambiente familiar.");
            e2.setFecha(LocalDate.now().plusDays(12));
            e2.setImagenUrl(
                    "https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&w=800&q=80");
            e2.setOrganizador(organizador);
            e2.setUbicacion(u2);

            Evento e3 = new Evento();
            e3.setNombre("Feria del Libro Antiguo");
            e3.setDescripcion("Encuentra joyas literarias, primeras ediciones y cómics descatalogados.");
            e3.setFecha(LocalDate.now().plusDays(20));
            e3.setImagenUrl(
                    "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=80");
            e3.setOrganizador(organizador);
            e3.setUbicacion(u3);

            Evento e4 = new Evento();
            e4.setNombre("Encuentro de Tejedores");
            e4.setDescripcion("Talleres en vivo y venta exclusiva de prendas textiles sostenibles.");
            e4.setFecha(LocalDate.now().plusDays(25));
            e4.setImagenUrl(
                    "https://images.unsplash.com/photo-1605289982774-9a6fef564df8?auto=format&fit=crop&w=800&q=80");
            e4.setOrganizador(organizador);
            e4.setUbicacion(u4);

            Evento e5 = new Evento();
            e5.setNombre("Mercadillo de Segunda Mano");
            e5.setDescripcion("Dale una segunda vida a la ropa y accesorios. Sostenibilidad y economía circular.");
            e5.setFecha(LocalDate.now().plusDays(30));
            e5.setImagenUrl(
                    "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=800&q=80");
            e5.setOrganizador(organizador);
            e5.setUbicacion(u5);

            // e6 Actualizado: De un mercadillo a una feria tecnológica
            Evento e6 = new Evento();
            e6.setNombre("Tech & Startup Hub");
            e6.setDescripcion(
                    "Charlas sobre innovación, networking y presentaciones de productos tecnológicos de última generación.");
            e6.setFecha(LocalDate.now().plusDays(35));
            e6.setImagenUrl(
                    "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=800&q=80");
            e6.setOrganizador(organizador);
            e6.setUbicacion(u6);

            // Evento 7: Festival de Flores
            Evento e7 = new Evento();
            e7.setNombre("Festival de la Primavera y Flores");
            e7.setDescripcion("Exposición floral al aire libre, talleres de jardinería y venta de plantas exóticas.");
            e7.setFecha(LocalDate.now().plusDays(40));
            e7.setImagenUrl(
                    "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=800&q=80");
            e7.setOrganizador(organizador);
            e7.setUbicacion(u7);

            // Evento 8: Convención de Cómics y Cultura Pop
            Evento e8 = new Evento();
            e8.setNombre("Comic Con Local");
            e8.setDescripcion("El punto de encuentro para amantes del cómic, cosplay, cine y videojuegos.");
            e8.setFecha(LocalDate.now().plusDays(45));
            e8.setImagenUrl(
                    "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=800&q=80");
            e8.setOrganizador(organizador);
            e8.setUbicacion(u8);

            // Evento 9: Feria de Vehículos Eléctricos
            Evento e9 = new Evento();
            e9.setNombre("Eco-Mobility Expo");
            e9.setDescripcion("Prueba los últimos modelos de coches, patinetes y bicicletas eléctricas del mercado.");
            e9.setFecha(LocalDate.now().plusDays(50));
            e9.setImagenUrl(
                    "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80");
            e9.setOrganizador(organizador);
            e9.setUbicacion(u9);

            // Guardar todos los eventos de golpe
            eventoRepository.saveAll(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9));

            System.out.println(
                    "Base de datos poblada con éxito: 1 Organizador, 1 admin, 1 Expositor, 1 Visitante, 9 Ubicaciones y 9 Eventos de prueba.");
        } else {
            System.out.println("Base de datos ya estaba poblada.");
        }
    }
}
