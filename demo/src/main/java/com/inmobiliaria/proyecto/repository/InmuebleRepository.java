package com.inmobiliaria.proyecto.repository;

import com.inmobiliaria.proyecto.models.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {
    // Spring Boot se encarga de crear las consultas SQL 
}