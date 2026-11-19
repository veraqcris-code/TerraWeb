package com.inmobiliaria.proyecto.controller.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.inmobiliaria.proyecto.dto.usuario.RegistroClienteDto;
import com.inmobiliaria.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class RegistroClienteRestController {

    private final UsuarioService usuarioService;

    public RegistroClienteRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroClienteDto datos) {
        usuarioService.registrarCliente(datos);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/login")
                .build()
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
