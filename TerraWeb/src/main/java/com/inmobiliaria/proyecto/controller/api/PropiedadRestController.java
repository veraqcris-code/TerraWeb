package com.inmobiliaria.proyecto.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadResumenDto;
import com.inmobiliaria.proyecto.service.PropiedadService;

@RestController
@RequestMapping("/api/v1/propiedades")
public class PropiedadRestController {

    private final PropiedadService propiedadService;

    public PropiedadRestController(PropiedadService propiedadService) {
        this.propiedadService = propiedadService;
    }

    @GetMapping
    public List<PropiedadResumenDto> listar(
            @RequestParam(defaultValue = "12") int limite) {
        return propiedadService.listarRecientes(limite);
    }

    @GetMapping("/{id}")
    public PropiedadDetalleDto obtenerPorId(@PathVariable Long id) {
        return propiedadService.obtenerDetalle(id);
    }
}
