package com.inmobiliaria.proyecto.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.service.SolicitudVisitaService;

@Controller
@RequestMapping("/admin/solicitudes")
public class AdminSolicitudController {

    private final SolicitudVisitaService solicitudVisitaService;

    public AdminSolicitudController(SolicitudVisitaService solicitudVisitaService) {
        this.solicitudVisitaService = solicitudVisitaService;
    }

    @PostMapping("/{id}/archivar")
    public String archivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        solicitudVisitaService.archivar(id, true);
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud archivada.");
        return "redirect:/admin#solicitudes";
    }

    @PostMapping("/{id}/restaurar")
    public String restaurar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        solicitudVisitaService.archivar(id, false);
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud restaurada.");
        return "redirect:/admin#solicitudes";
    }
}
