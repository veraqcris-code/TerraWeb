package com.inmobiliaria.proyecto.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titulo;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precio;
    @NotBlank
    private String moneda = "PEN";
    @NotBlank
    private String modalidad;
    private String direccion;
    private String distrito;
    private String ciudad;
    private String descripcion;
    private String tituloConcepto;
    private String conceptoParrafoUno;
    private String conceptoParrafoDos;
    private String arquitecto;
    private String ubicacionMapa;
    private String etiquetaMapa;
    private String imagenMapaUrl;
    private Integer habitaciones;
    private Integer banos;
    private BigDecimal areaConstruida;
    private BigDecimal areaTotal;
    private Integer cocheras;
    private Integer anioEntrega;
    private BigDecimal mantenimiento;
    private String monedaMantenimiento = "PEN";
    private String zonificacion;
    private String deposito;
    private String orientacion;
    private String instalaciones;
    private String politicaMascotas;
    private String descripcionEdificio;
    private String dossierUrl;
    private boolean publicada = true;
    private LocalDate fechaPublicacion = LocalDate.now();
    private String estado = "DISPONIBLE";

    @ManyToOne(fetch = FetchType.LAZY)
    private Agente agente;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ImagenPropiedad> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CaracteristicaPropiedad> caracteristicas = new ArrayList<>();

    public Propiedad() {
    }

    public void agregarImagen(ImagenPropiedad imagen) {
        imagenes.add(imagen);
        imagen.setPropiedad(this);
    }

    public void agregarCaracteristica(CaracteristicaPropiedad caracteristica) {
        caracteristicas.add(caracteristica);
        caracteristica.setPropiedad(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getTituloConcepto() { return tituloConcepto; }
    public void setTituloConcepto(String tituloConcepto) { this.tituloConcepto = tituloConcepto; }
    public String getConceptoParrafoUno() { return conceptoParrafoUno; }
    public void setConceptoParrafoUno(String conceptoParrafoUno) { this.conceptoParrafoUno = conceptoParrafoUno; }
    public String getConceptoParrafoDos() { return conceptoParrafoDos; }
    public void setConceptoParrafoDos(String conceptoParrafoDos) { this.conceptoParrafoDos = conceptoParrafoDos; }
    public String getArquitecto() { return arquitecto; }
    public void setArquitecto(String arquitecto) { this.arquitecto = arquitecto; }
    public String getUbicacionMapa() { return ubicacionMapa; }
    public void setUbicacionMapa(String ubicacionMapa) { this.ubicacionMapa = ubicacionMapa; }
    public String getEtiquetaMapa() { return etiquetaMapa; }
    public void setEtiquetaMapa(String etiquetaMapa) { this.etiquetaMapa = etiquetaMapa; }
    public String getImagenMapaUrl() { return imagenMapaUrl; }
    public void setImagenMapaUrl(String imagenMapaUrl) { this.imagenMapaUrl = imagenMapaUrl; }
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
    public Integer getAnioEntrega() { return anioEntrega; }
    public void setAnioEntrega(Integer anioEntrega) { this.anioEntrega = anioEntrega; }
    public BigDecimal getMantenimiento() { return mantenimiento; }
    public void setMantenimiento(BigDecimal mantenimiento) { this.mantenimiento = mantenimiento; }
    public String getMonedaMantenimiento() { return monedaMantenimiento; }
    public void setMonedaMantenimiento(String monedaMantenimiento) { this.monedaMantenimiento = monedaMantenimiento; }
    public String getZonificacion() { return zonificacion; }
    public void setZonificacion(String zonificacion) { this.zonificacion = zonificacion; }
    public String getDeposito() { return deposito; }
    public void setDeposito(String deposito) { this.deposito = deposito; }
    public String getOrientacion() { return orientacion; }
    public void setOrientacion(String orientacion) { this.orientacion = orientacion; }
    public String getInstalaciones() { return instalaciones; }
    public void setInstalaciones(String instalaciones) { this.instalaciones = instalaciones; }
    public String getPoliticaMascotas() { return politicaMascotas; }
    public void setPoliticaMascotas(String politicaMascotas) { this.politicaMascotas = politicaMascotas; }
    public String getDescripcionEdificio() { return descripcionEdificio; }
    public void setDescripcionEdificio(String descripcionEdificio) { this.descripcionEdificio = descripcionEdificio; }
    public String getDossierUrl() { return dossierUrl; }
    public void setDossierUrl(String dossierUrl) { this.dossierUrl = dossierUrl; }
    public boolean isPublicada() { return publicada; }
    public void setPublicada(boolean publicada) { this.publicada = publicada; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Agente getAgente() { return agente; }
    public void setAgente(Agente agente) { this.agente = agente; }
    public List<ImagenPropiedad> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenPropiedad> imagenes) { this.imagenes = imagenes; }
    public List<CaracteristicaPropiedad> getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(List<CaracteristicaPropiedad> caracteristicas) { this.caracteristicas = caracteristicas; }
}
