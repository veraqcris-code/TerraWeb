package com.inmobiliaria.proyecto.dto.propiedad;

public record ImagenPropiedadDto(
        String url,
        String textoAlternativo,
        String etiqueta,
        int orden) {
}
