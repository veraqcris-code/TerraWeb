package com.inmobiliaria.proyecto.mapper;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.inmobiliaria.proyecto.dto.agente.AgenteDto;
import com.inmobiliaria.proyecto.dto.propiedad.CaracteristicaPropiedadDto;
import com.inmobiliaria.proyecto.dto.propiedad.ImagenPropiedadDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadDetalleDto;
import com.inmobiliaria.proyecto.dto.propiedad.PropiedadResumenDto;
import com.inmobiliaria.proyecto.models.Agente;
import com.inmobiliaria.proyecto.models.Propiedad;

@Component
public class PropiedadMapper {

    public PropiedadResumenDto aResumen(Propiedad propiedad) {
        String direccionCompleta = String.join(", ",
                Stream.of(propiedad.getDireccion(), propiedad.getDistrito(), propiedad.getCiudad())
                        .filter(valor -> valor != null && !valor.isBlank())
                        .toList());
        String imagenUrl = propiedad.getImagenes().stream()
                .findFirst()
                .map(imagen -> imagen.getUrl())
                .orElse(null);
        String textoAlternativo = propiedad.getImagenes().stream()
                .findFirst()
                .map(imagen -> imagen.getTextoAlternativo())
                .orElse(propiedad.getTitulo());

        return new PropiedadResumenDto(
                propiedad.getId(),
                propiedad.getTitulo(),
                propiedad.getPrecio(),
                propiedad.getMoneda(),
                direccionCompleta,
                propiedad.getHabitaciones(),
                propiedad.getBanos(),
                propiedad.getAreaConstruida(),
                imagenUrl,
                textoAlternativo);
    }

    public PropiedadDetalleDto aDetalle(Propiedad propiedad) {
        List<ImagenPropiedadDto> imagenes = propiedad.getImagenes().stream()
                .map(imagen -> new ImagenPropiedadDto(
                        imagen.getUrl(),
                        imagen.getTextoAlternativo(),
                        imagen.getEtiqueta(),
                        imagen.getOrden()))
                .toList();
        List<CaracteristicaPropiedadDto> caracteristicas = propiedad.getCaracteristicas().stream()
                .map(caracteristica -> new CaracteristicaPropiedadDto(
                        caracteristica.getEtiqueta(),
                        caracteristica.getValor()))
                .toList();
        AgenteDto agente = aDto(propiedad.getAgente());

        return new PropiedadDetalleDto(
                propiedad.getId(),
                propiedad.getTitulo(),
                propiedad.getPrecio(),
                propiedad.getMoneda(),
                propiedad.getModalidad(),
                propiedad.getDireccion(),
                propiedad.getDistrito(),
                propiedad.getCiudad(),
                propiedad.getDescripcion(),
                propiedad.getTituloConcepto(),
                propiedad.getConceptoParrafoUno(),
                propiedad.getConceptoParrafoDos(),
                propiedad.getArquitecto(),
                propiedad.getUbicacionMapa(),
                propiedad.getEtiquetaMapa(),
                propiedad.getImagenMapaUrl(),
                propiedad.getHabitaciones(),
                propiedad.getBanos(),
                propiedad.getAreaConstruida(),
                propiedad.getAreaTotal(),
                propiedad.getCocheras(),
                propiedad.getAnioEntrega(),
                propiedad.getMantenimiento(),
                propiedad.getMonedaMantenimiento(),
                propiedad.getZonificacion(),
                propiedad.getDeposito(),
                propiedad.getOrientacion(),
                propiedad.getInstalaciones(),
                propiedad.getPoliticaMascotas(),
                propiedad.getDescripcionEdificio(),
                propiedad.getDossierUrl(),
                imagenes,
                caracteristicas,
                agente);
    }

    public AgenteDto aDto(Agente agente) {
        if (agente == null) {
            return null;
        }
        return new AgenteDto(
                agente.getId(),
                agente.getNombre(),
                agente.getCargo(),
                agente.getFotoUrl(),
                agente.getTelefono(),
                agente.getWhatsapp());
    }
}
