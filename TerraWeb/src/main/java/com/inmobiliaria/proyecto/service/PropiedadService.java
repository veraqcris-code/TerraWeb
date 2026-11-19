package com.inmobiliaria.proyecto.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadResumenDto;
import com.inmobiliaria.proyecto.dto.admin.PropiedadAdminFormDto;
import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;
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

    public static final int MAX_IMAGENES = 10;

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

    @Transactional(readOnly = true)
    public PropiedadDetalleDto obtenerDetalleAdmin(Long id) {
        return mapper.aDetalle(obtenerEntidad(id));
    }

    @Transactional
    public PropiedadDetalleDto guardarDetalle(
            Long id,
            PropiedadAdminFormDto datos,
            List<String> imagenes) {
        return mapper.aDetalle(guardar(id, datos, imagenes));
    }

    public List<Propiedad> listarTodas() {
        return repository.findAllByOrderByFechaPublicacionDesc();
    }

    @Transactional(readOnly = true)
    public List<PropiedadDetalleDto> listarDetallesAdmin() {
        return repository.findAllByOrderByFechaPublicacionDesc().stream()
                .map(mapper::aDetalle)
                .toList();
    }

    /**
     * Counts all registered properties, regardless of publication status.
     *
     * @return total number of registered properties
     */
    public long contarRegistradas() {
        return repository.count();
    }

    public List<Propiedad> buscarCatalogoPublico(
            String modalidad,
            String distrito,
            String tipo,
            Integer habitacionesMinimas,
            String orden) {
        return buscarCatalogoPublico(modalidad, distrito, tipo, habitacionesMinimas, orden, null);
    }

    /**
     * Searches public listings, optionally restricting results to one assigned agent.
     *
     * @param modalidad operation type to match
     * @param distrito district to match
     * @param tipo property type to match
     * @param habitacionesMinimas minimum number of bedrooms
     * @param orden requested result ordering
     * @param agenteId assigned agent identifier, or {@code null} for all agents
     * @return matching public listings in the requested order
     */
    public List<Propiedad> buscarCatalogoPublico(
            String modalidad,
            String distrito,
            String tipo,
            Integer habitacionesMinimas,
            String orden,
            Long agenteId) {
        Comparator<Propiedad> ordenReciente = Comparator
                .comparing(Propiedad::getFechaPublicacion, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Propiedad::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        Comparator<Propiedad> ordenPrecio = Comparator
                .comparing(Propiedad::getPrecio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ordenReciente);
        Comparator<Propiedad> ordenMayorPrecio = Comparator
                .comparing(Propiedad::getPrecio, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ordenReciente);

        Comparator<Propiedad> comparador = switch (Objects.toString(orden, "").toLowerCase(Locale.ROOT)) {
            case "menor-precio" -> ordenPrecio;
            case "mayor-precio" -> ordenMayorPrecio;
            default -> ordenReciente;
        };

        return repository.findAllByOrderByFechaPublicacionDesc().stream()
                .filter(Propiedad::isPublicada)
                .filter(propiedad -> esEstadoPublico(propiedad.getEstado()))
                .filter(propiedad -> coincide(propiedad.getModalidad(), modalidad))
                .filter(propiedad -> coincide(propiedad.getDistrito(), distrito))
                .filter(propiedad -> coincideTipo(propiedad.getTipo(), tipo))
                .filter(propiedad -> agenteId == null
                        || (propiedad.getAgente() != null
                                && Objects.equals(propiedad.getAgente().getId(), agenteId)))
                .filter(propiedad -> habitacionesMinimas == null
                        || (propiedad.getHabitaciones() != null
                                && propiedad.getHabitaciones() >= habitacionesMinimas))
                .sorted(comparador)
                .toList();
    }

    public List<String> listarDistritosPublicos() {
        return repository.findAllByOrderByFechaPublicacionDesc().stream()
                .filter(Propiedad::isPublicada)
                .filter(propiedad -> esEstadoPublico(propiedad.getEstado()))
                .map(Propiedad::getDistrito)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(distrito -> !distrito.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private boolean coincide(String valorPropiedad, String filtro) {
        return filtro == null || filtro.isBlank()
                || (valorPropiedad != null && valorPropiedad.trim().equalsIgnoreCase(filtro.trim()));
    }

    private boolean coincideTipo(String tipoPropiedad, String filtro) {
        return filtro == null || filtro.isBlank()
                || (tipoPropiedad != null && normalizarTipo(tipoPropiedad).equals(normalizarTipo(filtro)));
    }

    private String normalizarTipo(String tipo) {
        return tipo.trim().replaceAll("[\\s_-]+", "").toUpperCase(Locale.ROOT);
    }

    private boolean esEstadoPublico(String estado) {
        return "DISPONIBLE".equalsIgnoreCase(Objects.toString(estado, ""))
                || "RESERVADA".equalsIgnoreCase(Objects.toString(estado, ""));
    }

    public Propiedad obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la propiedad " + id));
    }

    public List<ImagenPropiedad> listarImagenes(Long id) {
        return new ArrayList<>(obtenerEntidad(id).getImagenes());
    }

    public PropiedadAdminFormDto cargarFormulario(Long id) {
        Propiedad propiedad = obtenerEntidad(id);
        PropiedadAdminFormDto formulario = new PropiedadAdminFormDto();
        formulario.setTitulo(propiedad.getTitulo());
        formulario.setPrecio(propiedad.getPrecio());
        formulario.setMoneda(propiedad.getMoneda());
        formulario.setModalidad(propiedad.getModalidad());
        formulario.setTipo(propiedad.getTipo() == null ? "CASA" : propiedad.getTipo());
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
        return formulario;
    }

    @Transactional
    public Propiedad guardar(Long id, PropiedadAdminFormDto datos) {
        return guardar(id, datos, List.of());
    }

    @Transactional
    public Propiedad guardar(Long id, PropiedadAdminFormDto datos, List<String> imagenes) {
        List<Long> imagenesConservar = id == null
                ? List.of()
                : obtenerEntidad(id).getImagenes().stream()
                        .map(ImagenPropiedad::getId)
                        .toList();
        return guardar(id, datos, imagenes, imagenesConservar);
    }

    @Transactional
    public Propiedad guardar(
            Long id,
            PropiedadAdminFormDto datos,
            List<String> imagenes,
            List<Long> idsImagenesConservar) {
        Propiedad propiedad = id == null ? new Propiedad() : obtenerEntidad(id);
        propiedad.setTitulo(datos.getTitulo().trim());
        propiedad.setPrecio(datos.getPrecio());
        propiedad.setMoneda(datos.getMoneda().trim().toUpperCase());
        propiedad.setModalidad(datos.getModalidad().trim());
        propiedad.setTipo(datos.getTipo());
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

        propiedad.setCocheras(datos.getCocheras());

        Set<Long> idsConservar = idsImagenesConservar == null
                ? Set.of()
                : new HashSet<>(idsImagenesConservar);
        List<ImagenPropiedad> imagenesActuales = new ArrayList<>(propiedad.getImagenes());
        List<ImagenPropiedad> imagenesConservadas = imagenesActuales.stream()
                .filter(imagen -> idsConservar.contains(imagen.getId()))
                .sorted(java.util.Comparator.comparingInt(ImagenPropiedad::getOrden))
                .toList();
        long cantidadNuevas = imagenes == null ? 0 : imagenes.size();
        if (imagenesConservadas.size() + cantidadNuevas > MAX_IMAGENES) {
            throw new ImagenInvalidaException(
                    "Una propiedad puede tener como máximo " + MAX_IMAGENES + " imágenes.");
        }

        propiedad.getImagenes().removeIf(imagen -> !idsConservar.contains(imagen.getId()));
        for (int indice = 0; indice < imagenesConservadas.size(); indice++) {
            ImagenPropiedad imagen = imagenesConservadas.get(indice);
            imagen.setOrden(indice);
            imagen.setEtiqueta(indice == 0 ? "Portada" : "Galería");
        }
        if (imagenes != null) {
            for (String ruta : imagenes) {
                ImagenPropiedad imagen = new ImagenPropiedad();
                imagen.setUrl(ruta);
                imagen.setTextoAlternativo(propiedad.getTitulo());
                int orden = propiedad.getImagenes().size();
                imagen.setEtiqueta(orden == 0 ? "Portada" : "Galería");
                imagen.setOrden(orden);
                propiedad.agregarImagen(imagen);
            }
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

    @Transactional
    public void reactivar(Long id) {
        Propiedad propiedad = obtenerEntidad(id);
        propiedad.setPublicada(true);
        propiedad.setEstado("DISPONIBLE");
        repository.save(propiedad);
    }

    public long contarPublicadas() {
        return repository.countByPublicadaTrue();
    }
}
