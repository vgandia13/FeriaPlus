package com.example.backend;

import com.example.backend.repository.EventoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sin @Transactional a propósito: si la clase abriera una transacción, la sesión de
// Hibernate seguiría abierta al serializar y no se detectaría una LazyInitializationException
// (justo lo que open-in-view=false puede provocar). Solo hace GET, así que no ensucia la BD.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SinOpenInViewIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired EventoRepository eventoRepository;

    @Test
    void listarEventosCargaLaUbicacion() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ubicacion.nombre").exists());
    }

    @Test
    void verUnEventoCargaLaUbicacion() throws Exception {
        Long id = eventoRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/eventos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion.nombre").exists());
    }
}
