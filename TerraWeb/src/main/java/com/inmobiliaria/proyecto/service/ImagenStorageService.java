package com.inmobiliaria.proyecto.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;

@Service
public class ImagenStorageService {

    private static final long MAX_IMAGE_SIZE = 15L * 1024 * 1024;
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    private final Path uploadDirectory;

    public ImagenStorageService(@Value("${app.uploads.directory}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public List<String> guardar(List<MultipartFile> archivos, int maximo) {
        List<MultipartFile> imagenes = archivos == null
                ? List.of()
                : archivos.stream().filter(archivo -> archivo != null && !archivo.isEmpty()).toList();
        if (imagenes.size() > maximo) {
            throw new ImagenInvalidaException("Puedes subir como máximo " + maximo + " imágenes.");
        }

        List<Path> guardadas = new ArrayList<>();
        List<String> rutas = new ArrayList<>();
        try {
            Files.createDirectories(uploadDirectory);
            for (MultipartFile archivo : imagenes) {
                if (archivo.getSize() > MAX_IMAGE_SIZE) {
                    throw new ImagenInvalidaException("Cada imagen debe pesar 15 MB o menos.");
                }

                String extension = detectarExtension(archivo);
                String nombreArchivo = UUID.randomUUID() + extension;
                Path destino = uploadDirectory.resolve(nombreArchivo).normalize();
                if (!destino.getParent().equals(uploadDirectory)) {
                    throw new ImagenInvalidaException("Nombre de archivo de imagen no válido.");
                }
                guardadas.add(destino);
                try (InputStream contenido = archivo.getInputStream()) {
                    Files.copy(contenido, destino);
                }
                rutas.add("/uploads/" + nombreArchivo);
            }
            return List.copyOf(rutas);
        } catch (IOException excepcion) {
            eliminarArchivos(guardadas, excepcion);
            throw new IllegalStateException("No se pudieron guardar las imágenes.", excepcion);
        } catch (ImagenInvalidaException excepcion) {
            eliminarArchivos(guardadas, excepcion);
            throw excepcion;
        }
    }

    public void eliminar(List<String> rutas) {
        for (String ruta : rutas) {
            if (ruta == null || !ruta.matches("/uploads/[a-f0-9-]+\\.(jpg|png|webp)")) {
                continue;
            }
            Path archivo = uploadDirectory.resolve(ruta.substring("/uploads/".length())).normalize();
            if (!archivo.getParent().equals(uploadDirectory)) {
                continue;
            }
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException excepcion) {
                throw new IllegalStateException("No se pudo eliminar una imagen reemplazada.", excepcion);
            }
        }
    }

    private String detectarExtension(MultipartFile archivo) throws IOException {
        byte[] firma;
        try (InputStream contenido = archivo.getInputStream()) {
            firma = contenido.readNBytes(12);
        }

        if (coincide(firma, PNG_SIGNATURE)) {
            return ".png";
        }
        if (firma.length >= 3
                && (firma[0] & 0xff) == 0xff
                && (firma[1] & 0xff) == 0xd8
                && (firma[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if (firma.length >= 12
                && texto(firma, 0, 4).equals("RIFF")
                && texto(firma, 8, 4).equals("WEBP")) {
            return ".webp";
        }
        throw new ImagenInvalidaException("Usa imágenes JPG, PNG o WEBP válidas.");
    }

    private boolean coincide(byte[] contenido, byte[] firma) {
        if (contenido.length < firma.length) {
            return false;
        }
        for (int indice = 0; indice < firma.length; indice++) {
            if (contenido[indice] != firma[indice]) {
                return false;
            }
        }
        return true;
    }

    private String texto(byte[] contenido, int inicio, int longitud) {
        return new String(contenido, inicio, longitud, java.nio.charset.StandardCharsets.US_ASCII);
    }

    private void eliminarArchivos(List<Path> archivos, Exception excepcionOriginal) {
        for (Path archivo : archivos) {
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException excepcionLimpieza) {
                excepcionOriginal.addSuppressed(excepcionLimpieza);
            }
        }
    }
}
