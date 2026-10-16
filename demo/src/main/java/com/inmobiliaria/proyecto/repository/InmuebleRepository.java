package com.inmobiliaria.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inmobiliaria.proyecto.models.Inmueble;

@Repository
public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {
}