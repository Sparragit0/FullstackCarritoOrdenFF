package com.fragancefantasy.carrito_orden.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fragancefantasy.carrito_orden.model.dto.CrearOrdenRequest;
import com.fragancefantasy.carrito_orden.model.dto.OrdenResponse;
import com.fragancefantasy.carrito_orden.service.JwtService;
import com.fragancefantasy.carrito_orden.service.OrdenService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "http://localhost:5173")
public class OrdenController {
    private final OrdenService ordenService;
    private final JwtService jwtService;

    public OrdenController(OrdenService ordenService, JwtService jwtService){
        this.ordenService = ordenService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<OrdenResponse> crearOrden(
        @Valid @RequestBody CrearOrdenRequest request,@RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String usuarioId = null;

        // 1) Intentar obtener el usuario desde el token (si viene)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!jwtService.isTokenExpired(token)) {
                usuarioId = jwtService.getEmailFromToken(token); // subject = email
            }
        }

        // 2) Si no hay token o es inválido, usar el usuarioId del body (para pruebas)
        if (usuarioId == null || usuarioId.isBlank()) {
            usuarioId = request.getUsuarioId();
        }

        // 3) Validar que tengamos algún usuarioId
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("No se pudo determinar el usuario de la orden");
        }

        OrdenResponse response = ordenService.crearOrden(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtenerOrdenPorId(@PathVariable Long id) {
        OrdenResponse response = ordenService.obtenerOrdenPorId(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<OrdenResponse>> obtenerOrdenesPorUsuario(@PathVariable String usuarioId) {
        List<OrdenResponse> ordenes = ordenService.obtenerOrdenesPorUsuario(usuarioId);
        return ResponseEntity.ok(ordenes);
    }
    
    @PutMapping("/{id}/completar")
    public ResponseEntity<OrdenResponse> completarOrden(@PathVariable Long id) {
        OrdenResponse response = ordenService.completarOrden(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<OrdenResponse> cancelarOrden(@PathVariable Long id) {
        OrdenResponse response = ordenService.cancelarOrden(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listarTodas() {
        List<OrdenResponse> ordenes = ordenService.obtenerTodas();
        return ResponseEntity.ok(ordenes);
    }

}
