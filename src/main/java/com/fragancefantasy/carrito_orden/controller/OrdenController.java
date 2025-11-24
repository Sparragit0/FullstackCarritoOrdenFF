package com.fragancefantasy.carrito_orden.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fragancefantasy.carrito_orden.model.dto.CrearOrdenRequest;
import com.fragancefantasy.carrito_orden.model.dto.OrdenResponse;
import com.fragancefantasy.carrito_orden.service.OrdenService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("")
public class OrdenController {
    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService){
        this.ordenService = ordenService;
    }

    @PostMapping("path")
    public ResponseEntity<OrdenResponse> crearOrden(@Valid @RequestBody CrearOrdenRequest request) {
        OrdenResponse response = ordenService.crearOrden(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}]")
    public ResponseEntity<OrdenResponse> obtenerOrdenPorId(@PathVariable Long id) {
        OrdenResponse response = ordenService.obtenerOrdenPorId(id);
        return ResponseEntity.ok(response);
    }
    
    


}
