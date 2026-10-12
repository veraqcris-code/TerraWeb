package com.inmobiliaria.proyecto.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.agente.AgenteDto;
import com.inmobiliaria.proyecto.dto.admin.AgenteAdminFormDto;
import com.inmobiliaria.proyecto.exception.RecursoNoEncontradoException;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.mapper.PropiedadMapper;
import com.inmobiliaria.proyecto.repository.AgenteRepository;
import com.inmobiliaria.proyecto.repository.PropiedadRepository;
import java.util.List;

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

    public Agente obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el agente " + id));
    }

    @Transactional
    public Agente guardar(Long id, AgenteAdminFormDto datos) {
        Agente agente = id == null ? new Agente() : obtenerEntidad(id);
        agente.setNombre(datos.getNombre().trim());
        agente.setCargo(datos.getCargo().trim());
        agente.setFotoUrl(datos.getFotoUrl());
        agente.setTelefono(datos.getTelefono());
        agente.setWhatsapp(datos.getWhatsapp());
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
}
