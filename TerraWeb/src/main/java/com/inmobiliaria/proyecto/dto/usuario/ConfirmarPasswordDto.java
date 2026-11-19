package com.inmobiliaria.proyecto.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarPasswordDto(@NotBlank String passwordActual) {
}
