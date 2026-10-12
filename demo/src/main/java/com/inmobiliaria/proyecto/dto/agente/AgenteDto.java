package com.inmobiliaria.proyecto.dto.agente;

public record AgenteDto(
        Long id,
        String nombre,
        String cargo,
        String fotoUrl,
        String telefono,
        String whatsapp) {
}
