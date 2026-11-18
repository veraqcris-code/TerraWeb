package com.inmobiliaria.proyecto.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroClienteDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;
    @NotBlank
    @Email
    @Size(max = 180)
    private String correo;
    @NotBlank
    @Pattern(regexp = "^[+0-9() .-]{7,25}$", message = "Ingrese un teléfono válido")
    private String telefono;
    @NotBlank
    @Size(min = 12, max = 72)
    private String password;
    @NotBlank
    private String confirmarPassword;
    @AssertTrue(message = "Debes aceptar los términos para crear una cuenta.")
    private boolean aceptaTerminos;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
    public boolean isAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
}
