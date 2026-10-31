package com.example.backend;

import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.UsuarioRepository;
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
    private PasswordEncoder passwordEncoder;

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

    private String eventoJson(String nombre) {
        return "{\"nombre\":\"" + nombre + "\",\"fecha\":\"2026-12-01\",\"ubicacion\":{},\"organizadorId\":1}";
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
}
