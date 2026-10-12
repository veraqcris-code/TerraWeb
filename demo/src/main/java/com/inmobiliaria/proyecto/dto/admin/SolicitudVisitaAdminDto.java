package com.inmobiliaria.proyecto.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudVisitaAdminDto(
        Long id,
        String nombre,
        String telefono,
        String correo,
        String tituloPropiedad,
        LocalDate fechaEstimada,
        LocalDateTime fechaSolicitud,
        boolean archivada) {
}
