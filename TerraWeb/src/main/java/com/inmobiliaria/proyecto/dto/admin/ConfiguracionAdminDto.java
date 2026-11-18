package com.inmobiliaria.proyecto.dto.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ConfiguracionAdminDto(
        @NotBlank @Size(max = 160) String nombreEmpresa,
        @NotBlank @Size(max = 25) String whatsapp,
        @NotBlank @Email @Size(max = 180) String correoContacto,
        @NotBlank @Size(max = 3) String monedaPrincipal,
        @NotNull @Positive BigDecimal tipoCambio,
        @NotBlank @Size(max = 10) String idioma,
        @NotBlank @Size(max = 64) String zonaHoraria,
        boolean notificarSolicitudes,
        boolean notificarEstados,
        boolean reporteSemanal,
        boolean asignacionAutomatica,
        boolean vencimientoReservas) {
}
