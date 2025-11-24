package com.fragancefantasy.carrito_orden.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fragancefantasy.carrito_orden.model.OrdenItem;

public interface OrdenItemRepository extends JpaRepository<OrdenItem, Long>{
    
}
