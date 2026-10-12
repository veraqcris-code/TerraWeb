package com.inmobiliaria.proyecto.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.admin.AdminDashboardDto;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final PropiedadService propiedadService;
    private final AgenteService agenteService;
    private final SolicitudVisitaService solicitudVisitaService;

    public AdminDashboardService(
            PropiedadService propiedadService,
            AgenteService agenteService,
            SolicitudVisitaService solicitudVisitaService) {
        this.propiedadService = propiedadService;
        this.agenteService = agenteService;
        this.solicitudVisitaService = solicitudVisitaService;
    }

    public AdminDashboardDto obtenerResumen() {
        return new AdminDashboardDto(
                propiedadService.listarTodas(),
                agenteService.listarTodos(),
                solicitudVisitaService.listar(false, 20),
                solicitudVisitaService.listar(true, 20),
                propiedadService.contarPublicadas(),
                agenteService.contarActivos(),
                solicitudVisitaService.contarPendientes());
    }
}
