package com.inmobiliaria.proyecto.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.ImagenStorageService;

@Controller
@RequestMapping("/admin/agentes")
public class AdminAgenteController {

    private final AgenteService agenteService;
    private final ImagenStorageService imagenStorageService;

    public AdminAgenteController(AgenteService agenteService, ImagenStorageService imagenStorageService) {
        this.agenteService = agenteService;
        this.imagenStorageService = imagenStorageService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("agenteForm", new AgenteAdminFormDto());
        return "admin/agente";
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
        formulario.setCorreo(agente.getCorreo() == null ? "" : agente.getCorreo());
        formulario.setCargo(agente.getCargo());
        formulario.setEspecialidad(agente.getEspecialidad() == null
                ? "Casas exclusivas y residencias" : agente.getEspecialidad());
        formulario.setEstado(agente.getEstado() == null ? "DISPONIBLE" : agente.getEstado());
        formulario.setFotoUrl(agente.getFotoUrl());
        formulario.setTelefono(agente.getTelefono() == null ? "" : agente.getTelefono());
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
            @RequestParam(name = "foto", required = false) MultipartFile foto,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("formAction", "/admin/agentes");
            return "admin/agente-form";
        }
        try {
            List<String> fotoGuardada = imagenStorageService.guardar(
                    foto == null ? List.of() : List.of(foto), 1);
            if (!fotoGuardada.isEmpty()) {
                formulario.setFotoUrl(fotoGuardada.get(0));
            }
        } catch (ImagenInvalidaException excepcion) {
            resultado.reject("foto.invalida", excepcion.getMessage());
            model.addAttribute("formAction", "/admin/agentes");
            return "admin/agente-form";
        }
        agenteService.guardar(null, formulario);
        redirectAttributes.addFlashAttribute("mensaje", "Agente creado correctamente.");
        return "redirect:/admin/agentes";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("agenteForm") AgenteAdminFormDto formulario,
            BindingResult resultado,
            @RequestParam(name = "foto", required = false) MultipartFile foto,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("agenteId", id);
            model.addAttribute("formAction", "/admin/agentes/" + id);
            return "admin/agente-form";
        }
        String fotoAnterior = agenteService.obtenerEntidad(id).getFotoUrl();
        try {
            List<String> fotoGuardada = imagenStorageService.guardar(
                    foto == null ? List.of() : List.of(foto), 1);
            if (!fotoGuardada.isEmpty()) {
                formulario.setFotoUrl(fotoGuardada.get(0));
            }
        } catch (ImagenInvalidaException excepcion) {
            resultado.reject("foto.invalida", excepcion.getMessage());
            model.addAttribute("agenteId", id);
            model.addAttribute("formAction", "/admin/agentes/" + id);
            return "admin/agente-form";
        }
        agenteService.guardar(id, formulario);
        if (foto != null && !foto.isEmpty()) {
            imagenStorageService.eliminar(
                    fotoAnterior == null ? List.of() : List.of(fotoAnterior));
        }
        redirectAttributes.addFlashAttribute("mensaje", "Agente actualizado correctamente.");
        return "redirect:/admin/agentes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        agenteService.eliminar(id);
        redirectAttributes.addFlashAttribute(
                "mensaje", "Agente eliminado; sus propiedades conservan sus datos.");
        return "redirect:/admin/agentes";
    }
}
