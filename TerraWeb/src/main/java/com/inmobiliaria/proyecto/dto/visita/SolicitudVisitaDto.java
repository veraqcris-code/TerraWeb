package com.inmobiliaria.proyecto.dto.visita;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudVisitaDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^(?:\\+?51[ .-]?)?9(?:[ .-]?\\d){8}$", message = "Ingrese un celular válido de Perú")
    private String telefono;

    @NotBlank
    @Email
    @Size(max = 180)
    private String correo;

    @NotNull
    @FutureOrPresent
    private LocalDate fechaEstimada;

    @NotNull
    @Positive
    private Long propiedadId;

    @Positive
    private Long agenteId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public LocalDate getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDate fechaEstimada) { this.fechaEstimada = fechaEstimada; }
    public Long getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Long propiedadId) { this.propiedadId = propiedadId; }
    public Long getAgenteId() { return agenteId; }
    public void setAgenteId(Long agenteId) { this.agenteId = agenteId; }
}
