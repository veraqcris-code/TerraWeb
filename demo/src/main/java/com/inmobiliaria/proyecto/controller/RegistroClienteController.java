package com.inmobiliaria.proyecto.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.dto.usuario.RegistroClienteDto;
import com.inmobiliaria.proyecto.exception.CorreoEnUsoException;
import com.inmobiliaria.proyecto.service.UsuarioService;

@Controller
public class RegistroClienteController {

    private final UsuarioService usuarioService;

    public RegistroClienteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("registroForm")) {
            model.addAttribute("registroForm", new RegistroClienteDto());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("registroForm") RegistroClienteDto datos,
            BindingResult resultado,
            RedirectAttributes redirectAttributes) {
        if (datos.getPassword() != null
                && datos.getConfirmarPassword() != null
                && !datos.getPassword().equals(datos.getConfirmarPassword())) {
            resultado.rejectValue("confirmarPassword", "password.no.coincide", "Las contraseñas no coinciden.");
        }
        if (resultado.hasErrors()) {
            return "registro";
        }
        try {
            usuarioService.registrarCliente(datos);
        } catch (CorreoEnUsoException excepcion) {
            resultado.rejectValue("correo", "correo.en.uso", excepcion.getMessage());
            return "registro";
        }
        redirectAttributes.addFlashAttribute("registroExitoso", true);
        return "redirect:/login";
    }
}
