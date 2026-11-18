package com.inmobiliaria.proyecto.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inmobiliaria.proyecto.dto.agente.AgentePublicoDto;
import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.ConfiguracionAdminService;
import com.inmobiliaria.proyecto.service.PropiedadService;

/**
 * Renders the public company information page.
 */
@Controller
public class NosotrosController {

    private static final long PROPIEDADES_GESTIONADAS_MINIMAS = 150;

    private final AgenteService agenteService;
    private final PropiedadService propiedadService;
    private final ConfiguracionAdminService configuracionAdminService;

    public NosotrosController(
            AgenteService agenteService,
            PropiedadService propiedadService,
            ConfiguracionAdminService configuracionAdminService) {
        this.agenteService = agenteService;
        this.propiedadService = propiedadService;
        this.configuracionAdminService = configuracionAdminService;
    }

    /**
     * Displays company information and current public leadership data.
     *
     * @param model view model for the company page
     * @return the company information template name
     */
    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        List<AgentePublicoDto> liderazgo = agenteService.listarActivosPublicos().stream()
                .sorted(Comparator.comparingInt(this::prioridadLiderazgo))
                .limit(2)
                .toList();
        ConfiguracionAdminDto configuracion = configuracionAdminService.obtener();

        List<Propiedad> propiedadesConFoto = propiedadService.listarTodas().stream()
                .filter(Propiedad::isPublicada)
                .filter(p -> p.getImagenes() != null && !p.getImagenes().isEmpty())
                .peek(p -> {
                    if (p.getDistrito() == null || p.getDistrito().isBlank()) {
                        p.setDistrito("PIURA");
                    }
                    if (p.getCiudad() == null || p.getCiudad().isBlank()) {
                        p.setCiudad("PERÚ");
                    }
                })
                .limit(2)
                .toList();

        model.addAttribute("liderazgo", liderazgo);
        model.addAttribute(
                "propiedadesGestionadas",
                Math.max(PROPIEDADES_GESTIONADAS_MINIMAS, propiedadService.contarRegistradas()));
        model.addAttribute("contactoUrl", crearUrlContacto(configuracion));
        model.addAttribute("propiedadesHero", propiedadesConFoto);
        return "nosotros";
    }

    private int prioridadLiderazgo(AgentePublicoDto agente) {
        String cargo = Objects.toString(agente.cargo(), "").toLowerCase(Locale.ROOT);
        return cargo.contains("senior") || cargo.contains("director") ? 0 : 1;
    }

    private String crearUrlContacto(ConfiguracionAdminDto configuracion) {
        String whatsapp = Objects.toString(configuracion.whatsapp(), "").replaceAll("\\D", "");
        if (whatsapp.length() == 9 && whatsapp.startsWith("9")) {
            whatsapp = "51" + whatsapp;
        }
        if (whatsapp.length() >= 7 && whatsapp.length() <= 15) {
            String mensaje = URLEncoder.encode(
                    "Hola, quisiera agendar una reunión con P&D Real Estate.",
                    StandardCharsets.UTF_8);
            return "https://wa.me/" + whatsapp + "?text=" + mensaje;
        }

        String correo = Objects.toString(configuracion.correoContacto(), "").trim();
        return correo.isBlank() ? null : "mailto:" + correo;
    }
}
