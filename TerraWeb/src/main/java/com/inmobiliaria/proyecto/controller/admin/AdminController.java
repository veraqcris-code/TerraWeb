package com.inmobiliaria.proyecto.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.service.AdminDashboardService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService dashboardService;

    public AdminController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String panel(Model model, Authentication authentication) {
        model.addAttribute("dashboard", dashboardService.obtenerResumen());
        model.addAttribute("adminCorreo", authentication.getName());
        model.addAttribute("agenteForm", new AgenteAdminFormDto());
        return "admin/resumen";
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return "admin/configuracion";
    }
}
