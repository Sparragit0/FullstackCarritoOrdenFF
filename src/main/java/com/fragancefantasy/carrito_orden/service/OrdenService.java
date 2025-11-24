package com.fragancefantasy.carrito_orden.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fragancefantasy.carrito_orden.model.EstadoOrden;
import com.fragancefantasy.carrito_orden.model.Orden;
import com.fragancefantasy.carrito_orden.model.OrdenItem;
import com.fragancefantasy.carrito_orden.model.dto.AjustarStockRequest;
import com.fragancefantasy.carrito_orden.model.dto.CrearOrdenRequest;
import com.fragancefantasy.carrito_orden.model.dto.OrdenItemRequest;
import com.fragancefantasy.carrito_orden.model.dto.OrdenItemResponse;
import com.fragancefantasy.carrito_orden.model.dto.OrdenResponse;
import com.fragancefantasy.carrito_orden.repository.OrdenRepository;

import jakarta.transaction.Transactional;

@Service
public class OrdenService {
    private final OrdenRepository ordenRepository;
    private final RestTemplate restTemplate;

    private static final String URL_INVENTARIO = "";

    public OrdenService(OrdenRepository ordenRepository, RestTemplate restTemplate) {
        this.ordenRepository = ordenRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrdenResponse crearOrden(CrearOrdenRequest request) {
        Orden orden = new Orden();
        orden.setUsuarioId(request.getUsuarioId());
        orden.setEstado(EstadoOrden.CREADA);

        List<OrdenItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrdenItemRequest itemReq: request.getItems()){
            AjustarStockRequest ajustarStockRequest = new AjustarStockRequest();
            ajustarStockRequest.setProductoId(itemReq.getProductoId());

            ajustarStockRequest.setCambioCantidad(-itemReq.getCantidad());

            try {
                restTemplate.postForEntity(
                    URL_INVENTARIO,
                    ajustarStockRequest, 
                    Void.class);
            } catch (RestClientException e) {
                throw new RuntimeException("No se pudo ajustar el stock para el producto con id: "
                    + itemReq.getProductoId(), e);
            }
            
            BigDecimal subtotal= itemReq.getPrecioUnitario().multiply(BigDecimal.valueOf(itemReq.getCantidad()));

            OrdenItem item = new OrdenItem();
            item.setOrden(orden);
            item.setProductoId(itemReq.getProductoId());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(itemReq.getPrecioUnitario());
            item.setSubtotal(subtotal);

            items.add(item);
            total = total.add(subtotal);
        }
        // asignamos a la orden los items y el total
        orden.setItems(items);
        orden.setTotal(total);

        Orden guardada = ordenRepository.save(orden);

        return convertirAOrdenResponse(guardada);
    }

    @Transactional
    public OrdenResponse obtenerOrdenPorId(Long id) {
        Orden orden = ordenRepository.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        return convertirAOrdenResponse(orden);
    }
    
    private OrdenResponse convertirAOrdenResponse(Orden orden) {
        // lista donde se guardan los items de respuesta
        List<OrdenItemResponse> itemsResponse = new ArrayList<>();

        for (OrdenItem item: orden.getItems()){
            OrdenItemResponse itemResp = new OrdenItemResponse();
            itemResp.setProductoId(item.getProductoId());
            itemResp.setCantidad(item.getCantidad());
            itemResp.setPrecioUnitario(item.getPrecioUnitario());
            itemResp.setSubtotal(item.getSubtotal());

            itemsResponse.add(itemResp);
        }
        OrdenResponse response = new OrdenResponse();
        response.setId(orden.getId());
        response.setUsuarioId(orden.getUsuarioId());
        response.setFechaCreacion(orden.getFechaCreacion());
        response.setEstado(orden.getEstado().name());
        response.setTotal(orden.getTotal());
        response.setItems(itemsResponse);

        return response;
    }
}
