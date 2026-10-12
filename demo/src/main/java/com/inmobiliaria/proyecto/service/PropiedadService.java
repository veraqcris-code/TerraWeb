package com.inmobiliaria.proyecto.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadResumenDto;
import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminFormDto;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.mapper.PropiedadMapper;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.ImagenPropiedad;
import com.inmobiliaria.proyecto.models.Propiedad;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;

@Service
@Transactional(readOnly = true)
public class PropiedadService {

    private final PropiedadRepository repository;
    private final PropiedadMapper mapper;
    private final AgenteRepository agenteRepository;

    public PropiedadService(
            PropiedadRepository repository,
            PropiedadMapper mapper,
            AgenteRepository agenteRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.agenteRepository = agenteRepository;
    }

    public List<PropiedadResumenDto> listarRecientes(int limite) {
        int cantidad = Math.max(1, Math.min(limite, 100));
        return repository.findByPublicadaTrueOrderByFechaPublicacionDesc(PageRequest.of(0, cantidad))
                .stream()
                .map(mapper::aResumen)
                .toList();
    }

    public PropiedadDetalleDto obtenerDetalle(Long id) {
        return repository.findByIdAndPublicadaTrue(id)
                .map(mapper::aDetalle)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la propiedad " + id));
    }

    public List<Propiedad> listarTodas() {
        return repository.findAllByOrderByFechaPublicacionDesc();
    }

    public Propiedad obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la propiedad " + id));
    }

    public PropiedadAdminFormDto cargarFormulario(Long id) {
        Propiedad propiedad = obtenerEntidad(id);
        PropiedadAdminFormDto formulario = new PropiedadAdminFormDto();
        formulario.setTitulo(propiedad.getTitulo());
        formulario.setPrecio(propiedad.getPrecio());
        formulario.setMoneda(propiedad.getMoneda());
        formulario.setModalidad(propiedad.getModalidad());
        formulario.setDireccion(propiedad.getDireccion());
        formulario.setDistrito(propiedad.getDistrito());
        formulario.setCiudad(propiedad.getCiudad());
        formulario.setDescripcion(propiedad.getDescripcion());
        formulario.setHabitaciones(propiedad.getHabitaciones());
        formulario.setBanos(propiedad.getBanos());
        formulario.setAreaConstruida(propiedad.getAreaConstruida());
        formulario.setAreaTotal(propiedad.getAreaTotal());
        formulario.setCocheras(propiedad.getCocheras());
        formulario.setPublicada(propiedad.isPublicada());
        formulario.setEstado(propiedad.getEstado());
        formulario.setAgenteId(propiedad.getAgente() == null ? null : propiedad.getAgente().getId());
        propiedad.getImagenes().stream()
                .min(java.util.Comparator.comparingInt(ImagenPropiedad::getOrden))
                .ifPresent(imagen -> {
                    formulario.setImagenPrincipalUrl(imagen.getUrl());
                    formulario.setTextoAlternativo(imagen.getTextoAlternativo());
                    formulario.setEtiquetaImagen(imagen.getEtiqueta());
                });
        return formulario;
    }

    @Transactional
    public Propiedad guardar(Long id, PropiedadAdminFormDto datos) {
        Propiedad propiedad = id == null ? new Propiedad() : obtenerEntidad(id);
        propiedad.setTitulo(datos.getTitulo().trim());
        propiedad.setPrecio(datos.getPrecio());
        propiedad.setMoneda(datos.getMoneda().trim().toUpperCase());
        propiedad.setModalidad(datos.getModalidad().trim());
        propiedad.setDireccion(datos.getDireccion());
        propiedad.setDistrito(datos.getDistrito());
        propiedad.setCiudad(datos.getCiudad());
        propiedad.setDescripcion(datos.getDescripcion());
        propiedad.setHabitaciones(datos.getHabitaciones());
        propiedad.setBanos(datos.getBanos());
        propiedad.setAreaConstruida(datos.getAreaConstruida());
        propiedad.setAreaTotal(datos.getAreaTotal());
        propiedad.setPublicada(datos.isPublicada());
        propiedad.setEstado(datos.getEstado());
        propiedad.setAgente(datos.getAgenteId() == null ? null : agenteRepository.findById(datos.getAgenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el agente " + datos.getAgenteId())));

        if (datos.getCocheras() != null) {
            propiedad.setCocheras(datos.getCocheras());
        }

        if (datos.getImagenPrincipalUrl() != null && !datos.getImagenPrincipalUrl().isBlank()) {
            ImagenPropiedad imagen = propiedad.getImagenes().stream()
                    .min(java.util.Comparator.comparingInt(ImagenPropiedad::getOrden))
                    .orElseGet(() -> {
                        ImagenPropiedad nueva = new ImagenPropiedad();
                        nueva.setOrden(0);
                        propiedad.agregarImagen(nueva);
                        return nueva;
                    });
            imagen.setUrl(datos.getImagenPrincipalUrl().trim());
            imagen.setTextoAlternativo(datos.getTextoAlternativo());
            imagen.setEtiqueta(datos.getEtiquetaImagen());
        }
        return repository.save(propiedad);
    }

    @Transactional
    public void eliminar(Long id) {
        Propiedad propiedad = obtenerEntidad(id);
        propiedad.setPublicada(false);
        propiedad.setEstado("RETIRADA");
        repository.save(propiedad);
    }

    public long contarPublicadas() {
        return repository.countByPublicadaTrue();
    }
}
