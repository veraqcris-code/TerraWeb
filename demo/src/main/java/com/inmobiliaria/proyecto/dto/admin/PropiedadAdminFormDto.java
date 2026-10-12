package com.inmobiliaria.proyecto.dto.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PropiedadAdminFormDto {

    @NotBlank
    @Size(max = 180)
    private String titulo;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal precio;
    @NotBlank
    private String moneda = "PEN";
    @NotBlank
    private String modalidad = "Venta";
    @Size(max = 255)
    private String direccion;
    @Size(max = 120)
    private String distrito;
    @Size(max = 120)
    private String ciudad;
    private String descripcion;
    @PositiveOrZero
    private Integer habitaciones;
    @PositiveOrZero
    private Integer banos;
    @DecimalMin("0.0")
    private BigDecimal areaConstruida;
    @DecimalMin("0.0")
    private BigDecimal areaTotal;
    @PositiveOrZero
    private Integer cocheras;
    private String imagenPrincipalUrl;
    private String textoAlternativo;
    private String etiquetaImagen;
    private boolean publicada = true;
    @NotBlank
    @Pattern(regexp = "DISPONIBLE|RESERVADA|EN_REVISION|VENDIDA|RETIRADA")
    private String estado = "DISPONIBLE";
    private Long agenteId;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getHabitaciones() { return habitaciones; }
    public void setHabitaciones(Integer habitaciones) { this.habitaciones = habitaciones; }
    public Integer getBanos() { return banos; }
    public void setBanos(Integer banos) { this.banos = banos; }
    public BigDecimal getAreaConstruida() { return areaConstruida; }
    public void setAreaConstruida(BigDecimal areaConstruida) { this.areaConstruida = areaConstruida; }
    public BigDecimal getAreaTotal() { return areaTotal; }
    public void setAreaTotal(BigDecimal areaTotal) { this.areaTotal = areaTotal; }
    public Integer getCocheras() { return cocheras; }
    public void setCocheras(Integer cocheras) { this.cocheras = cocheras; }
    public String getImagenPrincipalUrl() { return imagenPrincipalUrl; }
    public void setImagenPrincipalUrl(String imagenPrincipalUrl) { this.imagenPrincipalUrl = imagenPrincipalUrl; }
    public String getTextoAlternativo() { return textoAlternativo; }
    public void setTextoAlternativo(String textoAlternativo) { this.textoAlternativo = textoAlternativo; }
    public String getEtiquetaImagen() { return etiquetaImagen; }
    public void setEtiquetaImagen(String etiquetaImagen) { this.etiquetaImagen = etiquetaImagen; }
    public boolean isPublicada() { return publicada; }
    public void setPublicada(boolean publicada) { this.publicada = publicada; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getAgenteId() { return agenteId; }
    public void setAgenteId(Long agenteId) { this.agenteId = agenteId; }
}
