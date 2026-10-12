package com.inmobiliaria.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.Agente;

public interface AgenteRepository extends JpaRepository<Agente, Long> {

    long countByActivoTrue();
}
