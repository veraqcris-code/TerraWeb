package com.inmobiliaria.proyecto.controller;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inmobiliaria.proyecto.dto.agente.AgentePublicoDto;
import com.inmobiliaria.proyecto.dto.admin.ConfiguracionAdminDto;
import com.inmobiliaria.proyecto.service.AgenteService;
import com.inmobiliaria.proyecto.service.ConfiguracionAdminService;

/**
 * Renders the public real estate agents page.
 */
@Controller
public class AgentesController {

    private final AgenteService agenteService;
    private final ConfiguracionAdminService configuracionAdminService;

    public AgentesController(
            AgenteService agenteService,
            ConfiguracionAdminService configuracionAdminService) {
        this.agenteService = agenteService;
        this.configuracionAdminService = configuracionAdminService;
    }

    /**
     * Displays active agents and the contact information configured by the administrator.
     *
     * @return the agents template name
     */
    @GetMapping("/agentes")
    public String agentes(Model model) {
        List<AgentePublicoDto> agentes = agenteService.listarActivosPublicos();
        model.addAttribute("agentes", agentes);
        model.addAttribute("categoriasEspecialidad", categoriasEspecialidad(agentes));
        model.addAttribute("contactoUrl", crearUrlContacto(configuracionAdminService.obtener()));
        return "agente";
    }

    private List<CategoriaEspecialidad> categoriasEspecialidad(List<AgentePublicoDto> agentes) {
        return List.of(
                        new CategoriaEspecialidad("Casas", "casas", "casa"),
                        new CategoriaEspecialidad("Departamentos", "departamentos", "departamento"),
                        new CategoriaEspecialidad("Terrenos", "terrenos", "terreno"),
                        new CategoriaEspecialidad("Alquileres", "alquileres", "alquiler"))
                .stream()
                .filter(categoria -> agentes.stream()
                        .map(AgentePublicoDto::especialidad)
                        .filter(Objects::nonNull)
                        .map(especialidad -> especialidad.toLowerCase(Locale.ROOT))
                        .anyMatch(especialidad -> especialidad.contains(categoria.palabraClave())))
                .toList();
    }

    private String crearUrlContacto(ConfiguracionAdminDto configuracion) {
        String whatsapp = Objects.toString(configuracion.whatsapp(), "").replaceAll("\\D", "");
        if (whatsapp.length() == 9 && whatsapp.startsWith("9")) {
            whatsapp = "51" + whatsapp;
        }
        if (whatsapp.length() >= 7 && whatsapp.length() <= 15) {
            return "https://wa.me/" + whatsapp;
        }

        String correo = Objects.toString(configuracion.correoContacto(), "").trim();
        return correo.isBlank() ? null : "mailto:" + correo;
    }

    public record CategoriaEspecialidad(String nombre, String valor, String palabraClave) {
    }
}
