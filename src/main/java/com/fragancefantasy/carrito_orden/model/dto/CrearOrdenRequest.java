package com.fragancefantasy.carrito_orden.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearOrdenRequest {
    private String usuarioId;

    private List<OrdenItemRequest> items;
}
