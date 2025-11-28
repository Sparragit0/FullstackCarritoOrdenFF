package com.fragancefantasy.carrito_orden.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.fragancefantasy.carrito_orden.model.dto.PerfumeResumenDTO;
import com.fragancefantasy.carrito_orden.repository.OrdenRepository;

import jakarta.transaction.Transactional;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final RestTemplate restTemplate;

    // OJO: ajusta puertos/hosts si cambian en tus microservicios
    private static final String URL_INVENTARIO = "http://localhost:8081/api/inventario/ajustar-stock";
    private static final String URL_CATALOGO   = "http://localhost:8083/api/perfumes";

    public OrdenService(OrdenRepository ordenRepository, RestTemplate restTemplate) {
        this.ordenRepository = ordenRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrdenResponse crearOrden(CrearOrdenRequest request, String usuarioId) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe contener al menos un item");
        }

        Orden orden = new Orden();
        orden.setUsuarioId(usuarioId);
        orden.setEstado(EstadoOrden.CREADA);

        List<OrdenItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrdenItemRequest itemReq : request.getItems()) {

            // 1) Obtener información y precio real desde Catálogo
            PerfumeResumenDTO perfume;
            try {
                perfume = restTemplate.getForObject(
                        URL_CATALOGO + "/" + itemReq.getProductoId(),
                        PerfumeResumenDTO.class
                );
            } catch (RestClientException e) {
                throw new RuntimeException(
                        "No se pudo obtener información del producto " + itemReq.getProductoId(), e
                );
            }

            if (perfume == null || perfume.getPrecio() == null) {
                throw new IllegalArgumentException(
                        "El producto " + itemReq.getProductoId()
                        + " no existe en el catálogo o no tiene precio definido"
                );
            }

            BigDecimal precioUnitario = perfume.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(itemReq.getCantidad())
            );

            // 2) Ajustar stock en Inventario (salida = cantidad negativa)
            AjustarStockRequest ajustarStockRequest = new AjustarStockRequest();
            ajustarStockRequest.setProductoId(itemReq.getProductoId());
            ajustarStockRequest.setCambioCantidad(-itemReq.getCantidad());

            try {
                restTemplate.postForEntity(
                        URL_INVENTARIO,
                        ajustarStockRequest,
                        Void.class
                );
            } catch (RestClientException e) {
                throw new RuntimeException(
                        "No se pudo ajustar el stock para el producto " + itemReq.getProductoId(), e
                );
            }

            // 3) Construir el item de la orden usando el precio de Catálogo
            OrdenItem item = new OrdenItem();
            item.setOrden(orden);
            item.setProductoId(itemReq.getProductoId());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            item.setSubtotal(subtotal);

            items.add(item);
            total = total.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(total);

        Orden guardada = ordenRepository.save(orden);
        return convertirAOrdenResponse(guardada);
    }

    @Transactional
    public OrdenResponse obtenerOrdenPorId(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        return convertirAOrdenResponse(orden);
    }

    /**
     * Listar todas las órdenes (para AdminVentas).
     */
    public List<OrdenResponse> obtenerTodas() {
        return ordenRepository.findAll()
                .stream()
                .map(this::convertirAOrdenResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapeo entidad -> DTO de respuesta.
     */
    private OrdenResponse convertirAOrdenResponse(Orden orden) {
        List<OrdenItemResponse> itemsResponse = new ArrayList<>();

        for (OrdenItem item : orden.getItems()) {
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

    @Transactional
    public List<OrdenResponse> obtenerOrdenesPorUsuario(String usuarioId) {
        List<Orden> ordenes = ordenRepository.findByUsuarioId(usuarioId);
        List<OrdenResponse> respuesta = new ArrayList<>();

        for (Orden orden : ordenes) {
            respuesta.add(convertirAOrdenResponse(orden));
        }
        return respuesta;
    }

    @Transactional
    public OrdenResponse completarOrden(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id " + id));

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new IllegalStateException("No se puede completar una orden cancelada");
        }
        if (orden.getEstado() == EstadoOrden.COMPLETADA) {
            throw new IllegalStateException("La orden ya está completada");
        }

        orden.setEstado(EstadoOrden.COMPLETADA);
        Orden guardada = ordenRepository.save(orden);

        return convertirAOrdenResponse(guardada);
    }

    @Transactional
    public OrdenResponse cancelarOrden(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id " + id));

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new IllegalStateException("La orden ya está cancelada");
        }
        if (orden.getEstado() == EstadoOrden.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una orden completada");
        }

        // Devolver stock al inventario por cada item
        for (OrdenItem item : orden.getItems()) {
            AjustarStockRequest ajustarStockRequest = new AjustarStockRequest();
            ajustarStockRequest.setProductoId(item.getProductoId());
            // cantidad positiva = sumamos al stock
            ajustarStockRequest.setCambioCantidad(item.getCantidad());

            try {
                restTemplate.postForEntity(
                        URL_INVENTARIO,
                        ajustarStockRequest,
                        Void.class
                );
            } catch (RestClientException e) {
                throw new RuntimeException(
                        "No se pudo devolver el stock para el producto " + item.getProductoId(), e
                );
            }
        }

        orden.setEstado(EstadoOrden.CANCELADA);
        Orden guardada = ordenRepository.save(orden);

        return convertirAOrdenResponse(guardada);
    }
}
