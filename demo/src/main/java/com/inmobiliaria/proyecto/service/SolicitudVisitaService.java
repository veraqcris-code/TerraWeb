package com.inmobiliaria.proyecto.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaDto;
import com.inmobiliaria.proyecto.dto.visita.SolicitudVisitaRespuestaDto;
import com.inmobiliaria.proyecto.dto.admin.SolicitudVisitaAdminDto;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.models.SolicitudVisita;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import com.inmobiliaria.proyecto.repository.SolicitudVisitaRepository;

@Service
public class SolicitudVisitaService {

    private final SolicitudVisitaRepository solicitudRepository;
    private final PropiedadRepository propiedadRepository;

    public SolicitudVisitaService(
            SolicitudVisitaRepository solicitudRepository,
            PropiedadRepository propiedadRepository) {
        this.solicitudRepository = solicitudRepository;
        this.propiedadRepository = propiedadRepository;
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
                .map(solicitud -> new SolicitudVisitaAdminDto(
                        solicitud.getId(),
                        solicitud.getNombre(),
                        solicitud.getTelefono(),
                        solicitud.getCorreo(),
                        solicitud.getPropiedad() == null
                                ? solicitud.getTituloPropiedad()
                                : solicitud.getPropiedad().getTitulo(),
                        solicitud.getFechaEstimada(),
                        solicitud.getFechaSolicitud(),
                        solicitud.isArchivada()))
                .toList();
    }

    @Transactional
    public void archivar(Long id, boolean archivada) {
        SolicitudVisita solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud " + id));
        solicitud.setArchivada(archivada);
        solicitudRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return solicitudRepository.countByArchivadaFalse();
    }
}
