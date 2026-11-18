package com.inmobiliaria.proyecto.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.service.PropiedadService;
import com.inmobiliaria.proyecto.service.SolicitudVisitaService;

@Controller
public class PropiedadPageController {

    private final PropiedadService propiedadService;
    private final SolicitudVisitaService solicitudVisitaService;

    public PropiedadPageController(
            PropiedadService propiedadService,
            SolicitudVisitaService solicitudVisitaService) {
        this.propiedadService = propiedadService;
        this.solicitudVisitaService = solicitudVisitaService;
    }

    /**
     * Displays a published property's complete details and booking form.
     *
     * @param id property identifier
     * @param model view model for the detail page
     * @return the property detail template name
     */
    @GetMapping("/propiedades/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        agregarDatosDetalle(propiedadService.obtenerDetalle(id), model);
        if (!model.containsAttribute("visitaForm")) {
            SolicitudVisitaDto formulario = new SolicitudVisitaDto();
            formulario.setPropiedadId(id);
            model.addAttribute("visitaForm", formulario);
        }
        return "detallePropiedad";
    }

    /**
     * Registers a visit request and returns the visitor to the property details.
     *
     * @param id property identifier
     * @param datos submitted visit request
     * @param resultado validation result for the submitted request
     * @param model view model used when validation fails
     * @param redirectAttributes flash attributes used after a successful submission
     * @return detail page or redirect to the property
     */
    @PostMapping("/propiedades/{id}/visitas")
    public String solicitarVisita(
            @PathVariable Long id,
            @Valid @ModelAttribute("visitaForm") SolicitudVisitaDto datos,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            datos.setPropiedadId(id);
            agregarDatosDetalle(propiedadService.obtenerDetalle(id), model);
            return "detallePropiedad";
        }

        solicitudVisitaService.registrar(id, datos);
        redirectAttributes.addFlashAttribute("visitaSolicitada", true);
        return "redirect:/propiedades/" + id;
    }

    private void agregarDatosDetalle(PropiedadDetalleDto propiedad, Model model) {
        model.addAttribute("propiedad", propiedad);
        model.addAttribute("codigoReferencia", String.format(Locale.ROOT, "PROP-%03d", propiedad.id()));
        model.addAttribute("estadoEtiqueta", etiquetaEstado(propiedad.estado()));
        model.addAttribute("mapaUrl", crearUrlMapa(propiedad));
        model.addAttribute("agenteWhatsappUrl", crearUrlWhatsapp(propiedad));
    }

    private String etiquetaEstado(String estado) {
        String normalizado = Objects.toString(estado, "").trim().replace('_', ' ').toLowerCase(Locale.ROOT);
        if (normalizado.isBlank()) {
            return "";
        }
        return Character.toUpperCase(normalizado.charAt(0)) + normalizado.substring(1);
    }

    private String crearUrlMapa(PropiedadDetalleDto propiedad) {
        String ubicacion = Stream.of(
                        propiedad.direccion(),
                        propiedad.distrito(),
                        propiedad.ciudad(),
                        "Peru")
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
        return "https://www.google.com/maps?q="
                + URLEncoder.encode(ubicacion, StandardCharsets.UTF_8)
                + "&t=&z=15&ie=UTF8&iwloc=&output=embed";
    }

    private String crearUrlWhatsapp(PropiedadDetalleDto propiedad) {
        if (propiedad.agente() == null) {
            return null;
        }
        String telefono = Objects.toString(
                        propiedad.agente().whatsapp() == null || propiedad.agente().whatsapp().isBlank()
                                ? propiedad.agente().telefono()
                                : propiedad.agente().whatsapp(),
                        "")
                .replaceAll("\\D", "");
        if (telefono.length() == 9 && telefono.startsWith("9")) {
            telefono = "51" + telefono;
        }
        if (telefono.length() < 7 || telefono.length() > 15) {
            return null;
        }
        String mensaje = URLEncoder.encode(
                "Hola " + propiedad.agente().nombre() + ", quisiera consultar por la propiedad "
                        + propiedad.titulo() + ".",
                StandardCharsets.UTF_8);
        return "https://wa.me/" + telefono + "?text=" + mensaje;
    }
}
