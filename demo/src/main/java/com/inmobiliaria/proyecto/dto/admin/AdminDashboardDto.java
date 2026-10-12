package com.inmobiliaria.proyecto.dto.admin;

import java.util.List;

import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;

public record AdminDashboardDto(
        List<Propiedad> propiedades,
        List<Agente> agentes,
        List<SolicitudVisitaAdminDto> solicitudesActivas,
        List<SolicitudVisitaAdminDto> solicitudesArchivadas,
        long propiedadesPublicadas,
        long agentesActivos,
        long solicitudesPendientes) {
}
