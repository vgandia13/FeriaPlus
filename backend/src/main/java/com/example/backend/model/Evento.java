package com.example.backend.model;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import java.util.List;

import jakarta.persistence.Table;

@Entity
@Table(name = "eventos")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private LocalDate fecha;
    private String imagenUrl;

    @ManyToOne
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "organizador_id")
    private Usuario organizador;

    @ManyToMany
    @JoinTable(
        name="asistencias",
        joinColumns = @JoinColumn(name="evento_id"),
        inverseJoinColumns = @JoinColumn(name="usuario_id")
    )
    private Set<Usuario> asistentes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<Resena> reseñas;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<Puesto> puestos;
}
