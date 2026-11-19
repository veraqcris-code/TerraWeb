package com.inmobiliaria.proyecto.dto.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfiguracionAdminDto(
        @NotBlank @Size(max = 160) String nombreEmpresa,
        @NotBlank @Pattern(regexp = "^(?:\\+?51[ .-]?)?9(?:[ .-]?\\d){8}$",
                message = "Ingrese un celular válido de Perú") String whatsapp,
        @NotBlank @Email @Size(max = 180) String correoContacto,
        @NotBlank @Pattern(regexp = "PEN|USD|EUR") String monedaPrincipal,
        @NotNull @Positive BigDecimal tipoCambio,
        @NotBlank @Size(max = 10) String idioma,
        @NotBlank @Size(max = 64) String zonaHoraria,
        boolean notificarSolicitudes,
        boolean notificarEstados,
        boolean reporteSemanal,
        boolean asignacionAutomatica,
        boolean vencimientoReservas) {
}
