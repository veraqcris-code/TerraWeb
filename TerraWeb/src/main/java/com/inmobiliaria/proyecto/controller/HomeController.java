package com.inmobiliaria.proyecto.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.service.ConfiguracionAdminService;
import com.inmobiliaria.proyecto.service.PropiedadService;
import com.inmobiliaria.proyecto.service.TestimonioService;

@Controller
public class HomeController {

    private final PropiedadService propiedadService;
    private final TestimonioService testimonioService;
    private final ConfiguracionAdminService configuracionAdminService;

    /**
     * Creates the controller for the public home page.
     *
     * @param propiedadService service for public property data
     * @param testimonioService service for featured testimonials
     * @param configuracionAdminService service for public contact settings
     */
    public HomeController(
            PropiedadService propiedadService,
            TestimonioService testimonioService,
            ConfiguracionAdminService configuracionAdminService) {
        this.propiedadService = propiedadService;
        this.testimonioService = testimonioService;
        this.configuracionAdminService = configuracionAdminService;
    }

    /**
     * Loads the current public listings, districts, testimonials, and contact link.
     *
     * @param model view model populated for the home template
     * @return the home template name
     */
    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        List<Propiedad> propiedadesPublicadas = propiedadService.listarTodas().stream()
                .filter(Propiedad::isPublicada)
                .filter(propiedad -> !"RETIRADA".equalsIgnoreCase(
                        Objects.toString(propiedad.getEstado(), "")))
                .toList();
        ConfiguracionAdminDto configuracion = configuracionAdminService.obtener();

        model.addAttribute("propiedades", propiedadesPublicadas.stream().limit(3).toList());
        model.addAttribute("distritos", propiedadesPublicadas.stream()
                .map(Propiedad::getDistrito)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(distrito -> !distrito.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        model.addAttribute("testimonios", testimonioService.listarDestacados(3));
        model.addAttribute("contactoUrl", crearUrlContacto(configuracion));
        return "inicio";
    }

    /**
     * Builds a WhatsApp or email link from the saved public contact settings.
     *
     * @param configuracion saved administrator contact settings
     * @return a contact URI, or {@code null} when no contact method is configured
     */
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
