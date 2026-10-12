package com.inmobiliaria.proyecto.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.dto.testimonio.TestimonioDto;
import com.inmobiliaria.proyecto.models.Testimonio;
import com.inmobiliaria.proyecto.repository.TestimonioRepository;

@Service
@Transactional(readOnly = true)
public class TestimonioService {

    private final TestimonioRepository repository;

    public TestimonioService(TestimonioRepository repository) {
        this.repository = repository;
    }

    public List<TestimonioDto> listarDestacados(int limite) {
        int cantidad = Math.max(1, Math.min(limite, 100));
        return repository.findByActivoTrueOrderByIdDesc(PageRequest.of(0, cantidad))
                .stream()
                .map(this::aDto)
                .toList();
    }

    private TestimonioDto aDto(Testimonio testimonio) {
        return new TestimonioDto(
                testimonio.getId(),
                testimonio.getNombre(),
                testimonio.getUbicacion(),
                testimonio.getTitulo(),
                testimonio.getContenido(),
                testimonio.getFotoUrl(),
                testimonio.getPuntuacion());
    }
}
