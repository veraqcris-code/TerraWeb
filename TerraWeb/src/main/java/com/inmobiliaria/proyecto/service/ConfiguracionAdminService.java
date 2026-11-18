package com.inmobiliaria.proyecto.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.models.ConfiguracionAdmin;
import com.inmobiliaria.proyecto.repository.ConfiguracionAdminRepository;

@Service
public class ConfiguracionAdminService {

    private final ConfiguracionAdminRepository repository;

    public ConfiguracionAdminService(ConfiguracionAdminRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracionAdminDto obtener() {
        return repository.findFirstByOrderByIdAsc()
                .map(this::aDto)
                .orElseGet(this::valoresIniciales);
    }

    @Transactional
    public ConfiguracionAdminDto guardar(ConfiguracionAdminDto datos) {
        ConfiguracionAdmin configuracion = repository.findFirstByOrderByIdAsc()
                .orElseGet(ConfiguracionAdmin::new);
        configuracion.setNombreEmpresa(datos.nombreEmpresa().trim());
        configuracion.setWhatsapp(datos.whatsapp().trim());
        configuracion.setCorreoContacto(datos.correoContacto().trim().toLowerCase());
        configuracion.setMonedaPrincipal(datos.monedaPrincipal().trim().toUpperCase());
        configuracion.setTipoCambio(datos.tipoCambio());
        configuracion.setIdioma(datos.idioma());
        configuracion.setZonaHoraria(datos.zonaHoraria());
        configuracion.setNotificarSolicitudes(datos.notificarSolicitudes());
        configuracion.setNotificarEstados(datos.notificarEstados());
        configuracion.setReporteSemanal(datos.reporteSemanal());
        configuracion.setAsignacionAutomatica(datos.asignacionAutomatica());
        configuracion.setVencimientoReservas(datos.vencimientoReservas());
        return aDto(repository.save(configuracion));
    }

    private ConfiguracionAdminDto aDto(ConfiguracionAdmin configuracion) {
        return new ConfiguracionAdminDto(
                configuracion.getNombreEmpresa(),
                configuracion.getWhatsapp(),
                configuracion.getCorreoContacto(),
                configuracion.getMonedaPrincipal(),
                configuracion.getTipoCambio(),
                configuracion.getIdioma(),
                configuracion.getZonaHoraria(),
                configuracion.isNotificarSolicitudes(),
                configuracion.isNotificarEstados(),
                configuracion.isReporteSemanal(),
                configuracion.isAsignacionAutomatica(),
                configuracion.isVencimientoReservas());
    }

    private ConfiguracionAdminDto valoresIniciales() {
        return new ConfiguracionAdminDto(
                "P&D Real Estate Group",
                "",
                "",
                "PEN",
                new BigDecimal("3.75"),
                "es-PE",
                "America/Lima",
                true,
                true,
                true,
                true,
                true);
    }
}
