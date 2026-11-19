package com.inmobiliaria.proyecto.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.inmobiliaria.proyecto.models.Usuario;

public class PerfilUsuarioDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 180)
    private String correo;

    @NotBlank
    @Pattern(regexp = "^(?:\\+?51[ .-]?)?9(?:[ .-]?\\d){8}$", message = "Ingrese un celular válido de Perú")
    private String telefono;

    @Pattern(regexp = "^(?:|\\d{8})$", message = "El DNI debe contener 8 dígitos")
    private String documentoIdentidad;

    @Size(max = 160)
    private String residencia;

    @NotBlank
    @Pattern(regexp = "PEN|USD|EUR", message = "Seleccione una moneda válida")
    private String monedaPreferida = "PEN";

    @Size(max = 500)
    private String fotoUrl;

    public static PerfilUsuarioDto desde(Usuario usuario) {
        PerfilUsuarioDto perfil = new PerfilUsuarioDto();
        perfil.setNombre(usuario.getNombre());
        perfil.setCorreo(usuario.getCorreo());
        perfil.setTelefono(usuario.getTelefono());
        perfil.setDocumentoIdentidad(usuario.getDocumentoIdentidad());
        perfil.setResidencia(usuario.getResidencia());
        perfil.setMonedaPreferida(usuario.getMonedaPreferida() == null ? "PEN" : usuario.getMonedaPreferida());
        perfil.setFotoUrl(usuario.getFotoPerfilUrl());
        return perfil;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }
    public String getResidencia() { return residencia; }
    public void setResidencia(String residencia) { this.residencia = residencia; }
    public String getMonedaPreferida() { return monedaPreferida; }
    public void setMonedaPreferida(String monedaPreferida) { this.monedaPreferida = monedaPreferida; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
