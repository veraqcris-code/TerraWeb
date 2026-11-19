package com.inmobiliaria.proyecto.controller.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inmobiliaria.proyecto.dto.usuario.PerfilUsuarioDto;
import com.inmobiliaria.proyecto.dto.usuario.ConfirmarPasswordDto;
import com.inmobiliaria.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/mi-cuenta")
public class PerfilUsuarioRestController {

    private final UsuarioService usuarioService;
    private final SecurityContextRepository securityContextRepository;

    public PerfilUsuarioRestController(
            UsuarioService usuarioService,
            SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping
    public PerfilUsuarioDto obtener(Authentication authentication) {
        return usuarioService.obtenerPerfil(authentication.getName());
    }

    @PutMapping
    public PerfilUsuarioDto actualizar(
            @Valid @RequestBody PerfilUsuarioDto datos,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        PerfilUsuarioDto actualizado = usuarioService.actualizarPerfil(authentication.getName(), datos);
        actualizarIdentidad(authentication, actualizado.getCorreo(), request, response);
        return actualizado;
    }

    @DeleteMapping
    public void desactivar(
            @Valid @RequestBody ConfirmarPasswordDto datos,
            Authentication authentication,
            HttpServletResponse response) {
        if (usuarioService.desactivarCuenta(authentication.getName(), datos.passwordActual())) {
            response.setStatus(204);
        } else {
            try {
                writeJsonError(response, 401, "La contraseña actual no es correcta",
                        Map.of("passwordActual", "La contraseña actual no es correcta"));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void writeJsonError(HttpServletResponse response, int status, String mensaje, Map<String, String> errores) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String fechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fecha\":\"").append(fechaIso).append("\",");
        sb.append("\"estado\":").append(status).append(",");
        sb.append("\"mensaje\":\"").append(escapeJson(mensaje)).append("\",");
        sb.append("\"errores\":{");
        boolean first = true;
        for (Map.Entry<String, String> entry : errores.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}}");
        response.getWriter().write(sb.toString());
    }

    private void actualizarIdentidad(
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
