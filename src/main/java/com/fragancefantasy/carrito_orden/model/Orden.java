package com.fragancefantasy.carrito_orden.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuarioId;

    private LocalDateTime fechaCreacion;

    private EstadoOrden estado;

    private BigDecimal total;

    private List<OrdenItem> items;

    @PrePersist
    public void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoOrden.CREADA;
    }
}
