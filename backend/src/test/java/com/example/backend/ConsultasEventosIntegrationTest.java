package com.example.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManagerFactory;

// Sin @Transactional: con una transacción abierta en el test, Hibernate reutilizaría
// las entidades ya cargadas y contaría menos consultas de las reales.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsultasEventosIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void listarEventosNoHaceUnaConsultaPorEvento() throws Exception {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        mockMvc.perform(get("/api/eventos")).andExpect(status().isOk());

        // Con los 9 eventos del DataSeeder: 1 consulta de la página (ubicación,
        // categoría y
        // organizador por JOIN) + 1 de reseñas en lote. Margen de 1 para el count si
        // hay más páginas.
        // Medido: sin nada 26, solo batch 5, solo @EntityGraph 10.
        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(3);
    }
}