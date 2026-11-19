package com.inmobiliaria.proyecto.dto.inmueble;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InmuebleRequestDto(
        @NotBlank @Size(max = 180) String titulo,
        @Positive Double precio,
        @Size(max = 4000) String descripcion) {
}
