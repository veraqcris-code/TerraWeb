package com.inmobiliaria.proyecto.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AgenteAdminFormDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;
    @NotBlank
    @Email
    @Size(max = 180)
    private String correo;
    @NotBlank
    @Size(max = 160)
    private String cargo;
    @NotBlank
    @Size(max = 120)
    private String especialidad;
    @NotBlank
    @Pattern(regexp = "DISPONIBLE|INDUCCION|PAUSA")
    private String estado = "DISPONIBLE";
    @Size(max = 500)
    private String fotoUrl;
    @NotBlank
    @Pattern(regexp = "^(?:\\+?51[ .-]?)?9(?:[ .-]?\\d){8}$", message = "Ingrese un celular válido de Perú")
    private String telefono;
    @Pattern(regexp = "^(?:|(?:\\+?51[ .-]?)?9(?:[ .-]?\\d){8})$", message = "Ingrese un WhatsApp válido")
    private String whatsapp;
    private boolean activo = true;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
