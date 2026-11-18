package com.inmobiliaria.proyecto.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.agente.AgentePublicoDto;
import com.inmobiliaria.proyecto.dto.agente.AgenteDto;
import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.mapper.PropiedadMapper;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;

@Service
@Transactional(readOnly = true)
public class AgenteService {

    private final AgenteRepository repository;
    private final PropiedadMapper mapper;
    private final PropiedadRepository propiedadRepository;

    public AgenteService(
            AgenteRepository repository,
            PropiedadMapper mapper,
            PropiedadRepository propiedadRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.propiedadRepository = propiedadRepository;
    }

    public AgenteDto obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::aDto)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el agente " + id));
    }

    public List<Agente> listarTodos() {
        return repository.findAll();
    }

    /**
     * Lists active agents with their public listing counts and contact details.
     *
     * @return active agents prepared for the public directory
     */
    public List<AgentePublicoDto> listarActivosPublicos() {
        List<com.inmobiliaria.proyecto.models.Propiedad> propiedades =
                propiedadRepository.findAllByOrderByFechaPublicacionDesc();
        return repository.findByActivoTrueOrderByNombreAsc().stream()
                .map(agente -> {
                    List<com.inmobiliaria.proyecto.models.Propiedad> propiedadesAgente = propiedades.stream()
                            .filter(propiedad -> propiedad.isPublicada()
                                    && propiedad.getAgente() != null
                                    && Objects.equals(propiedad.getAgente().getId(), agente.getId())
                                    && esEstadoPublico(propiedad.getEstado()))
                            .toList();
                    String zona = propiedadesAgente.stream()
                            .map(com.inmobiliaria.proyecto.models.Propiedad::getDistrito)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(distrito -> !distrito.isEmpty())
                            .distinct()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.joining(", "));
                    long disponibles = propiedadesAgente.stream()
                            .filter(propiedad -> "DISPONIBLE".equalsIgnoreCase(propiedad.getEstado()))
                            .count();
                    return new AgentePublicoDto(
                            agente.getId(),
                            agente.getNombre(),
                            agente.getCargo(),
                            agente.getEspecialidad(),
                            agente.getEstado(),
                            agente.getFotoUrl(),
                            zona.isBlank() ? "Zona por confirmar" : zona,
                            iniciales(agente.getNombre()),
                            disponibles,
                            crearUrlWhatsapp(agente));
                })
                .toList();
    }

    public Agente obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el agente " + id));
    }

    @Transactional
    public Agente guardar(Long id, AgenteAdminFormDto datos) {
        Agente agente = id == null ? new Agente() : obtenerEntidad(id);
        agente.setNombre(datos.getNombre().trim());
        agente.setCorreo(datos.getCorreo().trim().toLowerCase());
        agente.setCargo(datos.getCargo().trim());
        agente.setEspecialidad(datos.getEspecialidad().trim());
        agente.setEstado(datos.getEstado());
        agente.setFotoUrl(datos.getFotoUrl());
        agente.setTelefono(datos.getTelefono().trim());
        agente.setWhatsapp(datos.getWhatsapp() == null || datos.getWhatsapp().isBlank()
                ? null : datos.getWhatsapp().trim());
        agente.setActivo(datos.isActivo());
        return repository.save(agente);
    }

    @Transactional
    public void eliminar(Long id) {
        Agente agente = obtenerEntidad(id);
        List<com.inmobiliaria.proyecto.models.Propiedad> propiedades = propiedadRepository.findByAgente_Id(id);
        propiedades.forEach(propiedad -> propiedad.setAgente(null));
        propiedadRepository.saveAll(propiedades);
        repository.delete(agente);
    }

    public long contarActivos() {
        return repository.countByActivoTrue();
    }

    private boolean esEstadoPublico(String estado) {
        return "DISPONIBLE".equalsIgnoreCase(Objects.toString(estado, ""))
                || "RESERVADA".equalsIgnoreCase(Objects.toString(estado, ""));
    }

    private String iniciales(String nombre) {
        String limpio = Objects.toString(nombre, "").trim();
        if (limpio.isEmpty()) {
            return "?";
        }
        String[] partes = limpio.split("\\s+");
        return partes.length == 1
                ? partes[0].substring(0, 1).toUpperCase(Locale.ROOT)
                : (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1))
                        .toUpperCase(Locale.ROOT);
    }

    private String crearUrlWhatsapp(Agente agente) {
        String telefono = Objects.toString(
                agente.getWhatsapp() == null || agente.getWhatsapp().isBlank()
                        ? agente.getTelefono()
                        : agente.getWhatsapp(),
                "").replaceAll("\\D", "");
        if (telefono.length() == 9 && telefono.startsWith("9")) {
            telefono = "51" + telefono;
        }
        if (telefono.length() < 7 || telefono.length() > 15) {
            return null;
        }
        String mensaje = "Hola " + Objects.toString(agente.getNombre(), "")
                + ", vi tu perfil en P&D Real Estate y quisiera recibir asesoría.";
        return "https://wa.me/" + telefono + "?text="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
    }
}
