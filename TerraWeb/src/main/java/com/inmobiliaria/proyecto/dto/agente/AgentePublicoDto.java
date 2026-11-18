package com.inmobiliaria.proyecto.dto.agente;

/**
 * Public profile data and property summary for an active real estate agent.
 *
 * @param id agent identifier
 * @param nombre agent's full name
 * @param cargo agent's role
 * @param especialidad agent's specialty
 * @param estado agent's availability status
 * @param fotoUrl agent's profile image URL
 * @param zona districts where the agent has public listings
 * @param iniciales fallback avatar initials
 * @param propiedadesDisponibles number of available public listings
 * @param whatsappUrl agent contact URL, or {@code null} when no phone is available
 */
public record AgentePublicoDto(
        Long id,
        String nombre,
        String cargo,
        String especialidad,
        String estado,
        String fotoUrl,
        String zona,
        String iniciales,
        long propiedadesDisponibles,
        String whatsappUrl) {
}
