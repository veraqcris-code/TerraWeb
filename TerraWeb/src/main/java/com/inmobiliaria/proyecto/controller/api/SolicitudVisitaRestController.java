package com.inmobiliaria.proyecto.controller.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaRespuestaDto;
import com.inmobiliaria.proyecto.service.SolicitudVisitaService;

@RestController
@RequestMapping("/api/v1/solicitudes-visita")
public class SolicitudVisitaRestController {

    private final SolicitudVisitaService solicitudVisitaService;

    public SolicitudVisitaRestController(SolicitudVisitaService solicitudVisitaService) {
        this.solicitudVisitaService = solicitudVisitaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudVisitaRespuestaDto registrar(
            @Valid @RequestBody SolicitudVisitaDto datos) {
        return solicitudVisitaService.registrar(datos.getPropiedadId(), datos);
    }
}
