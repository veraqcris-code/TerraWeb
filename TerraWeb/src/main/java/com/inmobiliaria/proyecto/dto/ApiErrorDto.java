package com.inmobiliaria.proyecto.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorDto(Instant fecha, int estado, String mensaje, Map<String, String> errores) {
}
