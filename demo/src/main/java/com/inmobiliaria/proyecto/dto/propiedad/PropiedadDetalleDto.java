package com.inmobiliaria.proyecto.dto.propiedad;

import java.math.BigDecimal;
import java.util.List;

import com.inmobiliaria.proyecto.dto.agente.AgenteDto;

public record PropiedadDetalleDto(
        Long id,
        String titulo,
        BigDecimal precio,
        String moneda,
        String modalidad,
        String direccion,
        String distrito,
        String ciudad,
        String descripcion,
        String tituloConcepto,
        String conceptoParrafoUno,
        String conceptoParrafoDos,
        String arquitecto,
        String ubicacionMapa,
        String etiquetaMapa,
        String imagenMapaUrl,
        Integer habitaciones,
        Integer banos,
        BigDecimal areaConstruida,
        BigDecimal areaTotal,
        Integer cocheras,
        Integer anioEntrega,
        BigDecimal mantenimiento,
        String monedaMantenimiento,
        String zonificacion,
        String deposito,
        String orientacion,
        String instalaciones,
        String politicaMascotas,
        String descripcionEdificio,
        String dossierUrl,
        List<ImagenPropiedadDto> imagenes,
        List<CaracteristicaPropiedadDto> caracteristicas,
        AgenteDto agente) {
}
