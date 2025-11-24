package com.fragancefantasy.carrito_orden.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AjustarStockRequest {
    private Long productoId;
    private Integer cambioCantidad;
}
