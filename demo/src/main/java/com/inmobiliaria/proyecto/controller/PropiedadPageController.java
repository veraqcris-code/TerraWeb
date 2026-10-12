package com.inmobiliaria.proyecto.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/propiedades/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("propiedad", propiedadService.obtenerDetalle(id));
        if (!model.containsAttribute("visitaForm")) {
            SolicitudVisitaDto formulario = new SolicitudVisitaDto();
            formulario.setPropiedadId(id);
            model.addAttribute("visitaForm", formulario);
        }
        return "detallePropiedad";
    }

    @PostMapping("/propiedades/{id}/visitas")
    public String solicitarVisita(
            @PathVariable Long id,
            @Valid @ModelAttribute("visitaForm") SolicitudVisitaDto datos,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            datos.setPropiedadId(id);
            model.addAttribute("propiedad", propiedadService.obtenerDetalle(id));
            return "detallePropiedad";
        }

        solicitudVisitaService.registrar(id, datos);
        redirectAttributes.addFlashAttribute("visitaSolicitada", true);
        return "redirect:/propiedades/" + id;
    }
}
