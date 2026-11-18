package com.inmobiliaria.proyecto.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaRespuestaDto;
import com.inmobiliaria.proyecto.dto.admin.SolicitudVisitaAdminDto;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.SolicitudVisita;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;

@Service
public class SolicitudVisitaService {

    private final SolicitudVisitaRepository solicitudRepository;
    private final PropiedadRepository propiedadRepository;
    private final AgenteService agenteService;

    public SolicitudVisitaService(
            SolicitudVisitaRepository solicitudRepository,
            PropiedadRepository propiedadRepository,
            AgenteService agenteService) {
        this.solicitudRepository = solicitudRepository;
        this.propiedadRepository = propiedadRepository;
        this.agenteService = agenteService;
    }

    @Transactional
    public SolicitudVisitaRespuestaDto registrar(Long propiedadId, SolicitudVisitaDto datos) {
        Propiedad propiedad = propiedadRepository.findByIdAndPublicadaTrue(propiedadId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la propiedad " + propiedadId));
        SolicitudVisita solicitud = new SolicitudVisita();
        solicitud.setNombre(datos.getNombre().trim());
        solicitud.setTelefono(datos.getTelefono().trim());
        solicitud.setCorreo(datos.getCorreo().trim());
        solicitud.setFechaEstimada(datos.getFechaEstimada());
        solicitud.setPropiedad(propiedad);
        solicitud.setTituloPropiedad(propiedad.getTitulo());
        solicitud.setEstado("NUEVA");

        if (datos.getAgenteId() != null) {
            try {
                solicitud.setAgente(agenteService.obtenerEntidad(datos.getAgenteId()));
            } catch (Exception ignored) {
                // Agente no encontrado, se deja sin asignar
            }
        }

        SolicitudVisita guardada = solicitudRepository.save(solicitud);
        return new SolicitudVisitaRespuestaDto(
                guardada.getId(),
                propiedad.getId(),
                "Gracias por su interés. Nos comunicaremos con usted.",
                guardada.getFechaEstimada());
    }

    @Transactional(readOnly = true)
    public List<SolicitudVisitaAdminDto> listar(boolean archivadas, int limite) {
        int cantidad = Math.max(1, Math.min(limite, 100));
        return solicitudRepository.findByArchivadaOrderByFechaSolicitudDesc(
                        archivadas, PageRequest.of(0, cantidad))
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudVisitaAdminDto> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc().stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional
    public void archivar(Long id, boolean archivada) {
        SolicitudVisita solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud " + id));
        solicitud.setArchivada(archivada);
        solicitud.setEstado(archivada ? "CERRADA" : "NUEVA");
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void actualizarEstado(Long id, String estado) {
        SolicitudVisita solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud " + id));
        solicitud.setEstado(estado);
        solicitud.setArchivada("CERRADA".equals(estado));
        solicitudRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return solicitudRepository.countByArchivadaFalse();
    }

    private SolicitudVisitaAdminDto aDto(SolicitudVisita solicitud) {
        Propiedad propiedad = solicitud.getPropiedad();
        Agente agente = solicitud.getAgente();
        return new SolicitudVisitaAdminDto(
                solicitud.getId(),
                solicitud.getNombre(),
                solicitud.getTelefono(),
                solicitud.getCorreo(),
                propiedad == null
                        ? solicitud.getTituloPropiedad()
                        : propiedad.getTitulo(),
                solicitud.getFechaEstimada(),
                solicitud.getFechaSolicitud(),
                solicitud.isArchivada(),
                solicitud.isArchivada()
                        ? "CERRADA"
                        : solicitud.getEstado() == null ? "NUEVA" : solicitud.getEstado(),
                agente == null ? null : agente.getId(),
                agente == null ? null : agente.getNombre());
    }
}
