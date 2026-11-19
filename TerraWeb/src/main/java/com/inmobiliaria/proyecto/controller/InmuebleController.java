package com.inmobiliaria.proyecto.controller;

import com.inmobiliaria.proyecto.dto.inmueble.InmuebleRequestDto;
import com.inmobiliaria.proyecto.dto.inmueble.InmuebleResponseDto;
import com.inmobiliaria.proyecto.models.Inmueble;
import com.inmobiliaria.proyecto.service.InmuebleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inmuebles", "/api/inmuebles"})
public class InmuebleController {

    private final InmuebleService service;

    // Tema 2: Dependency Injection
    public InmuebleController(InmuebleService service) {
        this.service = service;
    }

    @GetMapping
    public List<InmuebleResponseDto> listarInmuebles() {
        return service.obtenerTodos().stream().map(InmuebleController::aDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InmuebleResponseDto> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(inmueble -> ResponseEntity.ok(aDto(inmueble)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InmuebleResponseDto crearInmueble(@Valid @RequestBody InmuebleRequestDto datos) {
        Inmueble inmueble = new Inmueble(datos.titulo(), datos.precio(), datos.descripcion());
        return aDto(service.guardar(inmueble));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InmuebleResponseDto> actualizarInmueble(
            @PathVariable Long id,
            @Valid @RequestBody InmuebleRequestDto datos) {
        return service.obtenerPorId(id).map(inmueble -> {
            inmueble.setTitulo(datos.titulo());
            inmueble.setPrecio(datos.precio());
            inmueble.setDescripcion(datos.descripcion());
            return ResponseEntity.ok(aDto(service.guardar(inmueble)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInmueble(@PathVariable Long id) {
        if (service.obtenerPorId(id).isPresent()) {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private static InmuebleResponseDto aDto(Inmueble inmueble) {
        return new InmuebleResponseDto(
                inmueble.getId(),
                inmueble.getTitulo(),
                inmueble.getPrecio(),
                inmueble.getDescripcion());
    }
}