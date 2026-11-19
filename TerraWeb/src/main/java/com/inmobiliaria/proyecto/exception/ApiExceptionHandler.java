package com.inmobiliaria.proyecto.exception;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
public class ApiExceptionHandler {

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void writeJsonError(HttpServletResponse response, int status, String mensaje, Map<String, String> errores) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        StringBuilder sb = new StringBuilder();
        sb.append("{\"fecha\":\"").append(fechaIso).append("\",");
        sb.append("\"estado\":").append(status).append(",");
        sb.append("\"mensaje\":\"").append(escapeJson(mensaje)).append("\",");
        sb.append("\"errores\":{");

        boolean first = true;
        for (Map.Entry<String, String> entry : errores.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}}");

        response.getWriter().write(sb.toString());
    }

    private static void writeSimpleError(HttpServletResponse response, int status, String mensaje) throws IOException {
        writeJsonError(response, status, mensaje, Map.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public void recursoNoEncontrado(RecursoNoEncontradoException excepcion, HttpServletResponse response) {
        try {
            writeSimpleError(response, HttpStatus.NOT_FOUND.value(), excepcion.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void solicitudInvalida(MethodArgumentNotValidException excepcion, HttpServletResponse response) {
        Map<String, String> errores = excepcion.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Valor no válido" : error.getDefaultMessage(),
                        (primero, segundo) -> primero,
                        TreeMap::new));
        excepcion.getBindingResult().getGlobalErrors().stream()
                .findFirst()
                .ifPresent(error -> errores.put(
                        "global",
                        error.getDefaultMessage() == null ? "La solicitud contiene datos no válidos" : error.getDefaultMessage()));
        try {
            writeJsonError(response, HttpStatus.BAD_REQUEST.value(), "La solicitud contiene datos no válidos", errores);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void restriccionInvalida(ConstraintViolationException excepcion, HttpServletResponse response) {
        Map<String, String> errores = excepcion.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violacion -> violacion.getPropertyPath().toString(),
                        violacion -> violacion.getMessage(),
                        (primero, segundo) -> primero,
                        TreeMap::new));
        try {
            writeJsonError(response, HttpStatus.BAD_REQUEST.value(), "La solicitud contiene datos no válidos", errores);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void cuerpoInvalido(HttpMessageNotReadableException excepcion, HttpServletResponse response) {
        try {
            writeJsonError(response, HttpStatus.BAD_REQUEST.value(),
                    "El cuerpo de la solicitud no tiene un formato válido",
                    Map.of("body", "Revisa el JSON enviado"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public void metodoNoPermitido(HttpRequestMethodNotSupportedException excepcion, HttpServletResponse response) {
        try {
            String method = excepcion.getMethod() == null ? "" : excepcion.getMethod();
            writeJsonError(response, HttpStatus.METHOD_NOT_ALLOWED.value(),
                    "El método HTTP no está disponible para este recurso",
                    Map.of("method", method));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public void tipoContenidoNoAdmitido(HttpMediaTypeNotSupportedException excepcion, HttpServletResponse response) {
        try {
            String ct = excepcion.getContentType() == null ? "" : excepcion.getContentType().toString();
            writeJsonError(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                    "El tipo de contenido de la solicitud no está admitido",
                    Map.of("contentType", ct));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(CorreoEnUsoException.class)
    public void correoEnUso(CorreoEnUsoException excepcion, HttpServletResponse response) {
        try {
            writeJsonError(response, HttpStatus.CONFLICT.value(), excepcion.getMessage(),
                    Map.of("correo", excepcion.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(ImagenInvalidaException.class)
    public void imagenInvalida(ImagenInvalidaException excepcion, HttpServletResponse response) {
        try {
            writeJsonError(response, HttpStatus.BAD_REQUEST.value(), excepcion.getMessage(),
                    Map.of("imagenes", excepcion.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
