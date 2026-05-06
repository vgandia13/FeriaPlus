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
        if (eventoRepository.count() == 0) {
            
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

            // 3. Crear eventos ficticios con imágenes reales (de Unsplash) usando setters
            Evento e1 = new Evento();
            e1.setNombre("Feria de Artesanía Local");
            e1.setDescripcion("La mejor feria de la ciudad con productos 100% hechos a mano por artesanos de la comarca.");
            e1.setFecha(LocalDate.now().plusDays(5));
            e1.setImagenUrl("https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=800&q=80");
            e1.setOrganizador(organizador);
            e1.setUbicacion(u1);

            Evento e2 = new Evento();
            e2.setNombre("Mercado Gastronómico");
            e2.setDescripcion("Degusta los mejores platos típicos, quesos y vinos de la región en un ambiente familiar.");
            e2.setFecha(LocalDate.now().plusDays(12));
            e2.setImagenUrl("https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&w=800&q=80");
            e2.setOrganizador(organizador);
            e2.setUbicacion(u2);

            Evento e3 = new Evento();
            e3.setNombre("Feria del Libro Antiguo");
            e3.setDescripcion("Encuentra joyas literarias, primeras ediciones y cómics descatalogados.");
            e3.setFecha(LocalDate.now().plusDays(20));
            e3.setImagenUrl("https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=80");
            e3.setOrganizador(organizador);
            e3.setUbicacion(u3);

            Evento e4 = new Evento();
            e4.setNombre("Encuentro de Tejedores");
            e4.setDescripcion("Talleres en vivo y venta exclusiva de prendas textiles sostenibles.");
            e4.setFecha(LocalDate.now().plusDays(25));
            e4.setImagenUrl("https://images.unsplash.com/photo-1605289982774-9a6fef564df8?auto=format&fit=crop&w=800&q=80");
            e4.setOrganizador(organizador);
            e4.setUbicacion(u4);

            Evento e5 = new Evento();
            e5.setNombre("Mercadillo de Segunda Mano");
            e5.setDescripcion("Dale una segunda vida a la ropa y accesorios. Sostenibilidad y economía circular.");
            e5.setFecha(LocalDate.now().plusDays(30));
            e5.setImagenUrl("https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=800&q=80");
            e5.setOrganizador(organizador);
            e5.setUbicacion(u5);

            // Guardar todos los eventos de golpe
            eventoRepository.saveAll(Arrays.asList(e1, e2, e3, e4, e5));

            System.out.println("✅ Base de datos poblada con éxito: 1 Organizador, 5 Ubicaciones y 5 Eventos de prueba.");
        }
    }
}
