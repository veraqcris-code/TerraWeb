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
        boolean archivada,
        String estado,
        Long agenteId,
        String agenteNombre) {

    public String iniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] palabras = nombre.trim().split("\\s+");
        if (palabras.length == 1) {
            return palabras[0].substring(0, 1).toUpperCase();
        }
        return (palabras[0].substring(0, 1) + palabras[palabras.length - 1].substring(0, 1))
                .toUpperCase();
    }
}
