package com.fragancefantasy.carrito_orden.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fragancefantasy.carrito_orden.model.EstadoOrden;
import com.fragancefantasy.carrito_orden.model.Orden;

public interface OrdenRepository extends JpaRepository<Orden,Long>{
    // Obtener historial de un usuario
    List<Orden> findByUsuarioIdOrderByFechaCreacion(String usuarioId);

    List<Orden> findByUsuarioId(String usuarioId);
    // Filtrar x estado
    List<Orden> findByEstado(EstadoOrden estado);
}
