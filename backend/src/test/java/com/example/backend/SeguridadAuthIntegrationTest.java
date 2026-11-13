package com.example.backend;

import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.model.Evento;
import com.example.backend.model.Ubicacion;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.repository.UbicacionRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest 
@AutoConfigureMockMvc 
@ActiveProfiles("test")
@Transactional
class SeguridadAuthIntegrationTest {
    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired 
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private CategoriaRepository categoriaRepository;

    private Long idCategoria(String nombre) {
        return categoriaRepository.findAll().stream()
                .filter(c -> c.getNombre().equals(nombre))
                .findFirst().orElseThrow().getId();
    }

    private String login(String email) throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\""+ email + "\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private Long idDe(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow().getId();
    }

    // Todos los eventos del seeder son de organizador@gmail.com
    private Long idEventoDelSeeder() {
        return eventoRepository.findAll().get(0).getId();
    }

    private String eventoJson(String nombre, String fecha, Long ubicacionId) {
        return "{\"nombre\":\"" + nombre + "\",\"fecha\":\"" + fecha + "\","
            + "\"ubicacion\":{\"id\":" + ubicacionId + "}}";
    }

    private String eventoJson(String nombre) {
        Long ubicacionId = eventoRepository.findAll().get(0).getUbicacion().getId();
        return eventoJson(nombre, "2026-12-01", ubicacionId);
    }

    @Test
    void loginConContrasenaIncorrectaDa401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@gmail.com\",\"password\":\"malmalmal\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas."));
    }

    @Test
    void organizadorNoEntraEnAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer "+login("organizador@gmail.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSeBorraUnUsuarioConEventos() throws Exception {
        mockMvc.perform(delete("/api/admin/usuarios/" + idDe("organizador@gmail.com"))
                        .header("Authorization", "Bearer " + login("admin@gmail.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void sePuedeEntrarEnEventosConTokenFalso() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .header("Authorization", "Bearer tokenfalso"))
                .andExpect(status().isOk());
    }

    @Test
    void tokenFalsoNoEntraEnAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer tokenfalso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinCabeceraNoEntraEnAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malEndpointDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/api/eventos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parámetro inválido"));
    }

    @Test
    void parametroDeSortInvalidoDaBadRequest() throws Exception {
        mockMvc.perform(get("/api/eventos?sort=campoInexistente"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noSeOrdenaPorPassword() throws Exception {
        mockMvc.perform(get("/api/eventos?sort=organizador.password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laListaBlancaNoBloqueaLoValido() throws Exception {
        mockMvc.perform(get("/api/eventos?sort=fecha,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void loginConContenidoRotoDaBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{roto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noPermiteRegistrarComoAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juanito\",\"email\":\"exploiter@gmail.com\",\"password\":\"malmalmal\",\"rol\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNoEditaUsuarioSinRol() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios/" + idDe("carlos@gmail.com"))
                        .header("Authorization", "Bearer " + login("admin@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juanito\",\"email\":\"carlos@gmail.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noSeBorraAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/usuarios/" + idDe("admin@gmail.com"))
                        .header("Authorization", "Bearer " + login("admin@gmail.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaContrasenaMalaAlActualizar() throws Exception {
        mockMvc.perform(put("/api/perfil/me")
                        .header("Authorization", "Bearer " + login("carlos@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Carlos\",\"email\":\"carlos@gmail.com\",\"oldPassword\":\"contrasenaMala\",\"newPassword\":\"contrasenaNueva\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void visitanteNoEditaEventos() throws Exception {
        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                        .header("Authorization", "Bearer " + login("carlos@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Hackeado")))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizadorNoEditaEventoDeOtro() throws Exception {
        Usuario otro = new Usuario();
        otro.setNombre("Otro Organizador");
        otro.setEmail("otro@gmail.com");
        otro.setPassword(passwordEncoder.encode("12345678"));
        otro.setRol(Rol.ROLE_ORGANIZADOR);
        usuarioRepository.save(otro);

        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                        .header("Authorization", "Bearer " + login("otro@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Hackeado")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No puedes editar este evento"));
    }

    @Test
    void organizadorEditaSuEvento() throws Exception {
        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Nombre editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre editado"));
    }

    @Test
    void organizadorNoUsaOtroUsuarioComoOrganizador() throws Exception {
        Long ubicacionId = eventoRepository.findAll().get(0).getUbicacion().getId();
        mockMvc.perform(post("/api/eventos")
                    .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\":\"EventoMalicioso\",\"fecha\":\"2026-12-01\","
                            + "\"ubicacion\":{\"id\":\"" + ubicacionId + "\"},"
                            + "\"organizadorId\":" + idDe("carlos@gmail.com") + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizadorId").value(idDe("organizador@gmail.com")));
    }   

    @Test 
    void seRechazaUbicacionNoExistenteAlCrearEvento() throws Exception {
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"EventoTest\",\"fecha\":\"2026-12-01\","
                            + "\"ubicacion\":{\"id\":\"" + 999 + "\"},"
                            + "\"organizadorId\":" + idDe("organizador@gmail.com") + "}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void healthEsPublicoYSinDetalles() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void noSeCambiaEmailSinContrasena() throws Exception {
        mockMvc.perform(put("/api/perfil/me")
                        .header("Authorization", "Bearer " + login("carlos@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Carlos\",\"email\":\"carlos_nuevo@gmail.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test 
    void putCambiaFechaYUbicacion() throws Exception {
        Evento evento = eventoRepository.findAll().get(0);
        Long otraUbicacionId = ubicacionRepository.findAll().stream()
            .map(Ubicacion::getId)
            .filter(uid -> !uid.equals(evento.getUbicacion().getId()))
            .findFirst().orElseThrow();

        mockMvc.perform(put("/api/eventos/" + evento.getId())
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Evento editado", "2030-01-01", otraUbicacionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2030-01-01"))
                .andExpect(jsonPath("$.ubicacion.id").value(otraUbicacionId));
    }

    @Test
    void putConUbicacionInexistenteDa404() throws Exception {
        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Evento", "2030-01-01", 999999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void putSinIdDeUbicacionDa404() throws Exception {
        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                    .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\":\"Evento\",\"fecha\":\"2030-01-01\",\"ubicacion\":{}}"))
            .andExpect(status().isNotFound());
    }
    
    private Long crearEventoSinOrganizador() {
        Evento huerfano = new Evento();
        huerfano.setNombre("Evento huérfano");
        huerfano.setFecha(java.time.LocalDate.now().plusDays(10));
        huerfano.setUbicacion(eventoRepository.findAll().get(0).getUbicacion());
        return eventoRepository.save(huerfano).getId();
    }

    @Test
    void organizadorNoEditaEventoSinOrganizador() throws Exception {
        mockMvc.perform(put("/api/eventos/" + crearEventoSinOrganizador())
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Intento")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No puedes editar este evento"));
    }

    @Test
    void adminSiEditaEventoSinOrganizador() throws Exception {
        mockMvc.perform(put("/api/eventos/" + crearEventoSinOrganizador())
                        .header("Authorization", "Bearer " + login("admin@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventoJson("Editado por admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editado por admin"));
    }

    @Test
    void lasCategoriasSonPublicasYVienenOrdenadas() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].nombre").value("Artesanía"));
    }

    @Test
    void losEventosDelSeederTienenCategoria() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoriaId").isNumber());
    }

    @Test
    void filtrarPorCategoriaDevuelveSoloSusEventos() throws Exception {
        mockMvc.perform(get("/api/eventos").param("categoriaId", idCategoria("Artesanía").toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void nombreYCategoriaSeCombinan() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .param("nombre", "tejedores")
                        .param("categoriaId", idCategoria("Artesanía").toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Encuentro de Tejedores"));
    }

    @Test
    void elPorcentajeNoEsUnComodin() throws Exception {
        mockMvc.perform(get("/api/eventos").param("nombre", "%"))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void crearEventoConCategoriaInexistenteDa404() throws Exception {
        Long ubicacionId = eventoRepository.findAll().get(0).getUbicacion().getId();
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Con categoria falsa\",\"fecha\":\"2026-12-01\","
                                + "\"ubicacion\":{\"id\":" + ubicacionId + "},\"categoriaId\":9999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void editarEventoCambiaLaCategoria() throws Exception {
        Long ubicacionId = eventoRepository.findAll().get(0).getUbicacion().getId();
        Long cultura = idCategoria("Cultura");
        mockMvc.perform(put("/api/eventos/" + idEventoDelSeeder())
                        .header("Authorization", "Bearer " + login("organizador@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Editado\",\"fecha\":\"2026-12-01\","
                                + "\"ubicacion\":{\"id\":" + ubicacionId + "},\"categoriaId\":" + cultura + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaId").value(cultura));
    }

    // Puerta del Sol (Madrid). En el seeder hay 2 eventos en Madrid y el resto
    // en Valencia (~300 km), Barcelona (~500 km) y Sevilla (~390 km)
    @Test
    void cercaDeMadridSoloDevuelveLosDeMadrid() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .param("lat", "40.4168").param("lng", "-3.7038").param("radioKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void elPropioPuntoDelEventoEntraEnElRadio() throws Exception {
        // Coordenadas exactas de "Recinto Ferial" (Sevilla): distancia 0
        mockMvc.perform(get("/api/eventos")
                        .param("lat", "37.382").param("lng", "-5.973").param("radioKm", "1"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void cercaniaSeCombinaConCategoria() throws Exception {
        // Valencia, 20 km: 5 eventos; de ellos, Tecnología solo "Tech & Startup Hub" y "Eco-Mobility Expo"
        mockMvc.perform(get("/api/eventos")
                        .param("lat", "39.4699").param("lng", "-0.3763").param("radioKm", "20")
                        .param("categoriaId", idCategoria("Tecnología").toString()))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void cercaniaIncompletaDa400() throws Exception {
        mockMvc.perform(get("/api/eventos").param("lat", "40.4").param("lng", "-3.7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void latitudFueraDeRangoDa400() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .param("lat", "100").param("lng", "-3.7").param("radioKm", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void radioDemasiadoGrandeDa400() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .param("lat", "40.4").param("lng", "-3.7").param("radioKm", "5000"))
                .andExpect(status().isBadRequest());
    }

}
