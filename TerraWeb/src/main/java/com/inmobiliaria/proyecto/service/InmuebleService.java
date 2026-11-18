package com.inmobiliaria.proyecto.service;

import com.inmobiliaria.proyecto.models.Inmueble;
import com.inmobiliaria.proyecto.repository.InmuebleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InmuebleService {

    private final InmuebleRepository repository;

    public InmuebleService(InmuebleRepository repository) {
        this.repository = repository;
    }

    public List<Inmueble> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Inmueble> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Inmueble guardar(Inmueble inmueble) {
        return repository.save(inmueble);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}