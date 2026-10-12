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

import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminFormDto;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.PropiedadService;

@Controller
@RequestMapping("/admin/propiedades")
public class AdminPropiedadController {

    private final PropiedadService propiedadService;
    private final AgenteService agenteService;

    public AdminPropiedadController(PropiedadService propiedadService, AgenteService agenteService) {
        this.propiedadService = propiedadService;
        this.agenteService = agenteService;
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("propiedadForm", new PropiedadAdminFormDto());
        model.addAttribute("formAction", "/admin/propiedades");
        cargarAgentes(model);
        return "admin/propiedad-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("propiedadForm", propiedadService.cargarFormulario(id));
        model.addAttribute("propiedadId", id);
        model.addAttribute("formAction", "/admin/propiedades/" + id);
        cargarAgentes(model);
        return "admin/propiedad-form";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("propiedadForm") PropiedadAdminFormDto formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("formAction", "/admin/propiedades");
            cargarAgentes(model);
            return "admin/propiedad-form";
        }
        propiedadService.guardar(null, formulario);
        redirectAttributes.addFlashAttribute("mensaje", "Propiedad creada correctamente.");
        return "redirect:/admin";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("propiedadForm") PropiedadAdminFormDto formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("propiedadId", id);
            model.addAttribute("formAction", "/admin/propiedades/" + id);
            cargarAgentes(model);
            return "admin/propiedad-form";
        }
        propiedadService.guardar(id, formulario);
        redirectAttributes.addFlashAttribute("mensaje", "Propiedad actualizada correctamente.");
        return "redirect:/admin";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propiedadService.eliminar(id);
        redirectAttributes.addFlashAttribute(
                "mensaje", "Propiedad retirada del catálogo; se conserva para el historial.");
        return "redirect:/admin";
    }

    private void cargarAgentes(Model model) {
        model.addAttribute("agentes", agenteService.listarTodos());
    }
}
