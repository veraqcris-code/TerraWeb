package com.inmobiliaria.proyecto.dto.visita;

import java.time.LocalDate;

public record SolicitudVisitaRespuestaDto(
        Long id,
        Long propiedadId,
        String mensaje,
        LocalDate fechaEstimada) {
}
