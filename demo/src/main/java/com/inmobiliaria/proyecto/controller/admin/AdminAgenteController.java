package com.inmobiliaria.proyecto.controller.admin;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.service.AgenteService;

@Controller
@RequestMapping("/admin/agentes")
public class AdminAgenteController {

    private final AgenteService agenteService;

    public AdminAgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("agenteForm", new AgenteAdminFormDto());
        model.addAttribute("formAction", "/admin/agentes");
        return "admin/agente-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Agente agente = agenteService.obtenerEntidad(id);
        AgenteAdminFormDto formulario = new AgenteAdminFormDto();
        formulario.setNombre(agente.getNombre());
        formulario.setCargo(agente.getCargo());
        formulario.setFotoUrl(agente.getFotoUrl());
        formulario.setTelefono(agente.getTelefono());
        formulario.setWhatsapp(agente.getWhatsapp());
        formulario.setActivo(agente.isActivo());
        model.addAttribute("agenteForm", formulario);
        model.addAttribute("agenteId", id);
        model.addAttribute("formAction", "/admin/agentes/" + id);
        return "admin/agente-form";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("agenteForm") AgenteAdminFormDto formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("formAction", "/admin/agentes");
            return "admin/agente-form";
        }
        agenteService.guardar(null, formulario);
        redirectAttributes.addFlashAttribute("mensaje", "Agente creado correctamente.");
        return "redirect:/admin";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("agenteForm") AgenteAdminFormDto formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("agenteId", id);
            model.addAttribute("formAction", "/admin/agentes/" + id);
            return "admin/agente-form";
        }
        agenteService.guardar(id, formulario);
        redirectAttributes.addFlashAttribute("mensaje", "Agente actualizado correctamente.");
        return "redirect:/admin";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        agenteService.eliminar(id);
        redirectAttributes.addFlashAttribute(
                "mensaje", "Agente eliminado; sus propiedades conservan sus datos.");
        return "redirect:/admin";
    }
}
