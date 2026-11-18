package com.inmobiliaria.proyecto.controller.api;

import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.service.FavoritoService;

@RestController
@RequestMapping("/api/v1/mi-cuenta/favoritos")
public class FavoritoRestController {

    private final FavoritoService favoritoService;

    public FavoritoRestController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public Set<Long> listar(@AuthenticationPrincipal UserDetails usuario) {
        return favoritoService.listar(usuario.getUsername());
    }

    @PutMapping("/{propiedadId}")
    public Set<Long> agregar(
            @AuthenticationPrincipal UserDetails usuario,
            @PathVariable Long propiedadId) {
        return favoritoService.agregar(usuario.getUsername(), propiedadId);
    }

    @DeleteMapping("/{propiedadId}")
    public Set<Long> quitar(
            @AuthenticationPrincipal UserDetails usuario,
            @PathVariable Long propiedadId) {
        return favoritoService.quitar(usuario.getUsername(), propiedadId);
    }
}
