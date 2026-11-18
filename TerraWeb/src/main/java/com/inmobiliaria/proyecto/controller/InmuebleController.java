package com.inmobiliaria.proyecto.controller;

import com.inmobiliaria.proyecto.models.Inmueble;
import com.inmobiliaria.proyecto.service.InmuebleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inmuebles") // Ruta principal de tu API REST
public class InmuebleController {

    private final InmuebleService service;

    // Tema 2: Dependency Injection
    public InmuebleController(InmuebleService service) {
        this.service = service;
    }

    // Tema 4: Método HTTP GET (Obtener todos para el Catálogo)
    @GetMapping
    public List<Inmueble> listarInmuebles() {
        return service.obtenerTodos();
    }

    // Tema 4: Método HTTP GET (Obtener por ID para Detalle de Propiedad)
    @GetMapping("/{id}")
    public ResponseEntity<Inmueble> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tema 4: Método HTTP POST (Guardar nueva propiedad)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inmueble crearInmueble(@RequestBody Inmueble inmueble) {
        return service.guardar(inmueble);
    }

    // Tema 4: Método HTTP PUT (Editar propiedad)
    @PutMapping("/{id}")
    public ResponseEntity<Inmueble> actualizarInmueble(@PathVariable Long id, @RequestBody Inmueble detalles) {
        return service.obtenerPorId(id).map(inmueble -> {
            inmueble.setTitulo(detalles.getTitulo());
            inmueble.setPrecio(detalles.getPrecio());
            inmueble.setDescripcion(detalles.getDescripcion());
            return ResponseEntity.ok(service.guardar(inmueble));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Tema 4: Método HTTP DELETE (Borrar propiedad)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInmueble(@PathVariable Long id) {
        if (service.obtenerPorId(id).isPresent()) {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}