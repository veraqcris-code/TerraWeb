package com.inmobiliaria.proyecto.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contains the values required to change an administrator password.
 *
 * @param passwordActual the current password
 * @param passwordNueva the new password
 * @param confirmarPassword the repeated new password
 * @param cerrarOtrasSesiones whether to expire the other authenticated sessions
 */
public record CambiarPasswordAdminDto(
        @NotBlank String passwordActual,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,72}$",
                message = "La contraseña debe incluir una mayúscula y un número")
        String passwordNueva,
        @NotBlank String confirmarPassword,
        boolean cerrarOtrasSesiones) {
}
