package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaReserva = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expositor_id", nullable = false)
    private Usuario expositor;

    @OneToOne
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;
}

