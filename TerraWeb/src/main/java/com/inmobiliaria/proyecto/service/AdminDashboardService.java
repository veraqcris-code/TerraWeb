package com.inmobiliaria.proyecto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminResumenDto;
import com.inmobiliaria.proyecto.dto.admin.AdminDashboardDto;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;

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
        List<Propiedad> propiedades = propiedadService.listarTodas();
        List<AgenteAdminResumenDto> agentes = agenteService.listarTodos().stream()
                .map(agente -> resumirAgente(agente, propiedades))
                .toList();
        return new AdminDashboardDto(
                propiedades,
                propiedades.stream().limit(5).toList(),
                agentes,
                solicitudVisitaService.listar(false, 4),
                solicitudVisitaService.listar(true, 20),
                propiedadService.contarPublicadas(),
                agenteService.contarActivos(),
                solicitudVisitaService.contarPendientes());
    }

    private AgenteAdminResumenDto resumirAgente(Agente agente, List<Propiedad> propiedades) {
        long asignadas = propiedades.stream()
                .filter(propiedad -> propiedad.getAgente() != null
                        && propiedad.getAgente().getId().equals(agente.getId()))
                .count();
        return new AgenteAdminResumenDto(
                agente.getId(),
                agente.getNombre(),
                agente.getCorreo(),
                agente.getCargo(),
                agente.getEspecialidad(),
                agente.getEstado(),
                agente.getFotoUrl(),
                agente.getTelefono(),
                agente.isActivo(),
                asignadas);
    }
}
