package com.inmobiliaria.proyecto.dto.admin;

public record AgenteAdminResumenDto(
        Long id,
        String nombre,
        String correo,
        String cargo,
        String especialidad,
        String estado,
        String fotoUrl,
        String telefono,
        boolean activo,
        long propiedadesAsignadas) {
}
