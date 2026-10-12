package com.inmobiliaria.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inmobiliaria.proyecto.service.PropiedadService;
import com.inmobiliaria.proyecto.service.TestimonioService;

@Controller
public class HomeController {

    private final PropiedadService propiedadService;
    private final TestimonioService testimonioService;

    public HomeController(PropiedadService propiedadService, TestimonioService testimonioService) {
        this.propiedadService = propiedadService;
        this.testimonioService = testimonioService;
    }

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        model.addAttribute("propiedades", propiedadService.listarRecientes(3));
        model.addAttribute("testimonios", testimonioService.listarDestacados(3));
        return "inicio";
    }
}
