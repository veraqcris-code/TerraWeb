package com.inmobiliaria.proyecto.controller;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.service.ConfiguracionAdminService;
import com.inmobiliaria.proyecto.service.PropiedadService;

/**
 * Renders the public catalog with real listings and server-side filters.
 */
@Controller
public class BusquedaController {

    private static final int PROPIEDADES_POR_PAGINA = 6;

    private final PropiedadService propiedadService;
    private final ConfiguracionAdminService configuracionAdminService;

    public BusquedaController(
            PropiedadService propiedadService,
            ConfiguracionAdminService configuracionAdminService) {
        this.propiedadService = propiedadService;
        this.configuracionAdminService = configuracionAdminService;
    }

    @GetMapping({"/busqueda", "/buscar"})
    public String busqueda(
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String operacion,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer habitaciones,
            @RequestParam(required = false) Long agenteId,
            @RequestParam(defaultValue = "recientes") String orden,
            @RequestParam(name = "pagina", defaultValue = "1") int pagina,
            @AuthenticationPrincipal UserDetails usuarioAutenticado,
            Model model,
            Principal principal) {
        String operacionFiltro = normalizarOperacion(
                modalidad == null || modalidad.isBlank() ? operacion : modalidad);
        String tipoFiltro = normalizarTipo(tipo);
        String distritoFiltro = distrito == null ? "" : distrito.trim();
        Integer habitacionesFiltro = habitaciones != null && habitaciones > 0 ? habitaciones : null;
        Long agenteIdFiltro = agenteId != null && agenteId > 0 ? agenteId : null;
        String ordenFiltro = normalizarOrden(orden);

        List<Propiedad> propiedadesFiltradas = propiedadService.buscarCatalogoPublico(
                operacionFiltro, distritoFiltro, tipoFiltro, habitacionesFiltro, ordenFiltro, agenteIdFiltro);
        int totalPropiedades = propiedadesFiltradas.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) totalPropiedades / PROPIEDADES_POR_PAGINA));
        int paginaActual = Math.min(Math.max(pagina, 1), totalPaginas);
        int desde = (paginaActual - 1) * PROPIEDADES_POR_PAGINA;
        int hasta = Math.min(desde + PROPIEDADES_POR_PAGINA, totalPropiedades);
        List<Propiedad> propiedadesPagina = propiedadesFiltradas.subList(desde, hasta);
        ConfiguracionAdminDto configuracion = configuracionAdminService.obtener();

        model.addAttribute("propiedades", propiedadesPagina);
        model.addAttribute("distritos", propiedadService.listarDistritosPublicos());
        model.addAttribute("totalPropiedades", totalPropiedades);
        model.addAttribute("desde", totalPropiedades == 0 ? 0 : desde + 1);
        model.addAttribute("hasta", hasta);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("modalidadFiltro", operacionFiltro);
        model.addAttribute("distritoFiltro", distritoFiltro);
        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("habitacionesFiltro", habitacionesFiltro);
        model.addAttribute("ordenFiltro", ordenFiltro);
        model.addAttribute("agenteIdFiltro", agenteIdFiltro);
        model.addAttribute("contactoUrl", crearUrlContacto(configuracion));
        model.addAttribute("favoritosPersistentes", usuarioAutenticado != null && principal != null);
        return "busqueda";
    }

    private String normalizarOperacion(String valor) {
        if ("venta".equalsIgnoreCase(Objects.toString(valor, "").trim())) {
            return "Venta";
        }
        if ("alquiler".equalsIgnoreCase(Objects.toString(valor, "").trim())) {
            return "Alquiler";
        }
        return "";
    }

    private String normalizarTipo(String valor) {
        String tipo = Objects.toString(valor, "").trim();
        return switch (tipo.replaceAll("[\\s_-]+", "").toUpperCase(java.util.Locale.ROOT)) {
            case "CASA" -> "Casa";
            case "DEPARTAMENTO" -> "Departamento";
            case "TERRENO" -> "Terreno";
            case "LOCALCOMERCIAL" -> "Local Comercial";
            default -> "";
        };
    }

    private String normalizarOrden(String valor) {
        return switch (Objects.toString(valor, "").trim().toLowerCase(java.util.Locale.ROOT)) {
            case "menor-precio" -> "menor-precio";
            case "mayor-precio" -> "mayor-precio";
            default -> "recientes";
        };
    }

    private String crearUrlContacto(ConfiguracionAdminDto configuracion) {
        String whatsapp = Objects.toString(configuracion.whatsapp(), "").replaceAll("\\D", "");
        if (whatsapp.length() == 9 && whatsapp.startsWith("9")) {
            whatsapp = "51" + whatsapp;
        }
        if (whatsapp.length() >= 7 && whatsapp.length() <= 15) {
            return "https://wa.me/" + whatsapp;
        }

        String correo = Objects.toString(configuracion.correoContacto(), "").trim();
        return correo.isBlank() ? null : "mailto:" + correo;
    }
}
