package com.inmobiliaria.proyecto.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.inmobiliaria.proyecto.dto.ApiErrorDto;
import com.inmobiliaria.proyecto.controller.api.PropiedadRestController;

@RestControllerAdvice(basePackageClasses = PropiedadRestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorDto> recursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto(Instant.now(), 404, excepcion.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> solicitudInvalida(MethodArgumentNotValidException excepcion) {
        Map<String, String> errores = excepcion.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Valor no válido" : error.getDefaultMessage(),
                        (primero, segundo) -> primero));
        return ResponseEntity.badRequest()
                .body(new ApiErrorDto(Instant.now(), 400, "La solicitud contiene datos no válidos", errores));
    }
}
