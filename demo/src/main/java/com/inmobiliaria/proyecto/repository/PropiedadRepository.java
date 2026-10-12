package com.inmobiliaria.proyecto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.Propiedad;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    List<Propiedad> findByPublicadaTrueOrderByFechaPublicacionDesc(Pageable pageable);

    Optional<Propiedad> findByIdAndPublicadaTrue(Long id);

    @EntityGraph(attributePaths = {"imagenes", "agente"})
    List<Propiedad> findAllByOrderByFechaPublicacionDesc();

    List<Propiedad> findByAgente_Id(Long agenteId);

    long countByPublicadaTrue();
}
