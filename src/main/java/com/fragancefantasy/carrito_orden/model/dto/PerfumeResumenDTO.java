package com.fragancefantasy.carrito_orden.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfumeResumenDTO {
    private long productoId;
    private String nombre;
    private String marca;
    private BigDecimal precio;
}
