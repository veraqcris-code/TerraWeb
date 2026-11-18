package com.inmobiliaria.proyecto.dto.testimonio;

public record TestimonioDto(
        Long id,
        String nombre,
        String ubicacion,
        String titulo,
        String contenido,
        String fotoUrl,
        int puntuacion) {
}
