package com.inmobiliaria.proyecto.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.inmobiliaria.proyecto.models.SolicitudVisita;

public interface SolicitudVisitaRepository extends JpaRepository<SolicitudVisita, Long> {

    @EntityGraph(attributePaths = {"propiedad", "propiedad.agente", "agente"})
    List<SolicitudVisita> findByArchivadaOrderByFechaSolicitudDesc(boolean archivada, Pageable pageable);

    @EntityGraph(attributePaths = {"propiedad", "propiedad.agente", "agente"})
    List<SolicitudVisita> findAllByOrderByFechaSolicitudDesc();

    long countByArchivadaFalse();

    @Transactional
    @Modifying
    @Query("update SolicitudVisita solicitud set solicitud.propiedad = null where solicitud.propiedad.id = :propiedadId")
    void desvincularPropiedad(@Param("propiedadId") Long propiedadId);
}
