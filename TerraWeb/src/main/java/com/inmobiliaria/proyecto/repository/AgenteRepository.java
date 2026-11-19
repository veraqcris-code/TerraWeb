package com.inmobiliaria.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.Agente;
import java.util.List;

public interface AgenteRepository extends JpaRepository<Agente, Long> {

    long countByActivoTrue();

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    List<Agente> findByActivoTrueOrderByNombreAsc();
}
