package com.inmobiliaria.proyecto.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inmobiliaria.proyecto.models.SolicitudVisita;

public interface SolicitudVisitaRepository extends JpaRepository<SolicitudVisita, Long> {

    List<SolicitudVisita> findByArchivadaOrderByFechaSolicitudDesc(boolean archivada, Pageable pageable);

    long countByArchivadaFalse();
}
