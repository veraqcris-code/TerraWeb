package com.inmobiliaria.proyecto.models;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_admin")
public class ConfiguracionAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreEmpresa;
    private String whatsapp;
    private String correoContacto;
    private String monedaPrincipal;
    private BigDecimal tipoCambio;
    private String idioma;
    private String zonaHoraria;
    private boolean notificarSolicitudes;
    private boolean notificarEstados;
    private boolean reporteSemanal;
    private boolean asignacionAutomatica;
    private boolean vencimientoReservas;

    public Long getId() { return id; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }
    public String getMonedaPrincipal() { return monedaPrincipal; }
    public void setMonedaPrincipal(String monedaPrincipal) { this.monedaPrincipal = monedaPrincipal; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }
    public boolean isNotificarSolicitudes() { return notificarSolicitudes; }
    public void setNotificarSolicitudes(boolean notificarSolicitudes) { this.notificarSolicitudes = notificarSolicitudes; }
    public boolean isNotificarEstados() { return notificarEstados; }
    public void setNotificarEstados(boolean notificarEstados) { this.notificarEstados = notificarEstados; }
    public boolean isReporteSemanal() { return reporteSemanal; }
    public void setReporteSemanal(boolean reporteSemanal) { this.reporteSemanal = reporteSemanal; }
    public boolean isAsignacionAutomatica() { return asignacionAutomatica; }
    public void setAsignacionAutomatica(boolean asignacionAutomatica) { this.asignacionAutomatica = asignacionAutomatica; }
    public boolean isVencimientoReservas() { return vencimientoReservas; }
    public void setVencimientoReservas(boolean vencimientoReservas) { this.vencimientoReservas = vencimientoReservas; }
}
