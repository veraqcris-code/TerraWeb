package com.inmobiliaria.proyecto.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inmobiliaria.proyecto.dto.usuario.PerfilUsuarioDto;
import com.inmobiliaria.proyecto.exception.CorreoEnUsoException;
import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;
import com.inmobiliaria.proyecto.service.ImagenStorageService;
import com.inmobiliaria.proyecto.service.UsuarioService;

@Controller
@RequestMapping("/mi-cuenta")
public class PerfilUsuarioController {

    private final UsuarioService usuarioService;
    private final ImagenStorageService imagenStorageService;
    private final SecurityContextRepository securityContextRepository;

    public PerfilUsuarioController(
            UsuarioService usuarioService,
            ImagenStorageService imagenStorageService,
            SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.imagenStorageService = imagenStorageService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping
    public String perfil(Authentication authentication, Model model) {
        PerfilUsuarioDto perfilActual = usuarioService.obtenerPerfil(authentication.getName());
        if (!model.containsAttribute("perfilForm")) {
            model.addAttribute("perfilForm", perfilActual);
        }
        model.addAttribute("fotoPerfilUrl", perfilActual.getFotoUrl());
        return "perfil";
    }

    @PostMapping
    public String actualizar(
            @Valid @ModelAttribute("perfilForm") PerfilUsuarioDto formulario,
            BindingResult resultado,
            Authentication authentication,
            @RequestParam(name = "foto", required = false) MultipartFile foto,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        PerfilUsuarioDto perfilActual = usuarioService.obtenerPerfil(authentication.getName());
        if (resultado.hasErrors()) {
            modelarFotoPerfil(model, perfilActual);
            return "perfil";
        }

        List<String> fotoGuardada;
        try {
            fotoGuardada = imagenStorageService.guardar(
                    foto == null ? List.of() : List.of(foto), 1);
        } catch (ImagenInvalidaException excepcion) {
            resultado.reject("foto.invalida", excepcion.getMessage());
            modelarFotoPerfil(model, perfilActual);
            return "perfil";
        }

        String nuevaFotoUrl = fotoGuardada.isEmpty() ? null : fotoGuardada.get(0);
        PerfilUsuarioDto perfilActualizado;
        try {
            perfilActualizado = usuarioService.actualizarPerfil(
                    authentication.getName(), formulario, nuevaFotoUrl);
        } catch (CorreoEnUsoException excepcion) {
            imagenStorageService.eliminar(fotoGuardada);
            resultado.rejectValue("correo", "correo.en.uso", excepcion.getMessage());
            modelarFotoPerfil(model, perfilActual);
            return "perfil";
        } catch (RuntimeException excepcion) {
            imagenStorageService.eliminar(fotoGuardada);
            throw excepcion;
        }

        actualizarIdentidadDeSesion(authentication, perfilActualizado.getCorreo(), request, response);
        if (nuevaFotoUrl != null) {
            imagenStorageService.eliminar(perfilActual.getFotoUrl() == null
                    ? List.of()
                    : List.of(perfilActual.getFotoUrl()));
        }

        redirectAttributes.addFlashAttribute("perfilGuardado", true);
        return "redirect:/mi-cuenta";
    }

    private void modelarFotoPerfil(Model model, PerfilUsuarioDto perfilActual) {
        model.addAttribute("fotoPerfilUrl", perfilActual.getFotoUrl());
    }

    private void actualizarIdentidadDeSesion(
            Authentication authentication,
            String correo,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authentication.getName().equalsIgnoreCase(correo)) {
            return;
        }
        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("La sesión autenticada no contiene detalles de usuario.");
        }

        UserDetails identidadActualizada = User.withUsername(correo)
                .password(userDetails.getPassword())
                .authorities(authentication.getAuthorities())
                .build();
        UsernamePasswordAuthenticationToken autenticacionActualizada =
                new UsernamePasswordAuthenticationToken(identidadActualizada, null, authentication.getAuthorities());
        autenticacionActualizada.setDetails(authentication.getDetails());

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacionActualizada);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
    }
}
