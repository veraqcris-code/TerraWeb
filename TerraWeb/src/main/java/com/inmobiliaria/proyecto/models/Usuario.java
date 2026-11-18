package com.inmobiliaria.proyecto.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "correo"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String correo;
    private String passwordHash;
    private String telefono;
    private String documentoIdentidad;
    private String residencia;
    private String monedaPreferida = "PEN";
    private String fotoPerfilUrl;

    @ElementCollection
    @CollectionTable(name = "usuarios_propiedades_favoritas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "propiedad_id")
    private Set<Long> propiedadesFavoritas = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;
    private boolean activo = true;

    public Usuario() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }
    public String getResidencia() { return residencia; }
    public void setResidencia(String residencia) { this.residencia = residencia; }
    public String getMonedaPreferida() { return monedaPreferida; }
    public void setMonedaPreferida(String monedaPreferida) { this.monedaPreferida = monedaPreferida; }
    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
    public Set<Long> getPropiedadesFavoritas() { return propiedadesFavoritas; }
    public void setPropiedadesFavoritas(Set<Long> propiedadesFavoritas) { this.propiedadesFavoritas = propiedadesFavoritas; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
