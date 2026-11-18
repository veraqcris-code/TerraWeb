package com.inmobiliaria.proyecto.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitudEstadoAdminDto(
        @NotBlank @Pattern(regexp = "NUEVA|VISITA_AGENDADA|NEGOCIACION|CERRADA") String estado) {
}
