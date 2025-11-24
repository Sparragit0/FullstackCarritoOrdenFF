package com.fragancefantasy.carrito_orden.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponse {
    private Long id;
    private String usuarioId;
    private String estado;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private List<OrdenItemResponse> items;
}
