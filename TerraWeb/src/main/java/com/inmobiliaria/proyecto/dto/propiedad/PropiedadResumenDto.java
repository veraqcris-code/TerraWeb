package com.inmobiliaria.proyecto.dto.propiedad;

import java.math.BigDecimal;

public record PropiedadResumenDto(
        Long id,
        String titulo,
        BigDecimal precio,
        String moneda,
        String direccionCompleta,
        Integer habitaciones,
        Integer banos,
        BigDecimal areaConstruida,
        String imagenUrl,
        String textoAlternativo) {
}
