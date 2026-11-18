package com.inmobiliaria.proyecto.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.inmobiliaria.proyecto.exception.ImagenInvalidaException;

class ImagenStorageServiceTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void guardaUnaImagenPngConNombreNoControladoPorElCliente() throws Exception {
        ImagenStorageService servicio = new ImagenStorageService(directorioTemporal.toString());
        byte[] firmaPng = {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x00
        };
        MockMultipartFile archivo = new MockMultipartFile(
                "imagenes", "../../fachada.png", "image/png", firmaPng);

        List<String> rutas = servicio.guardar(List.of(archivo), 10);

        assertEquals(1, rutas.size());
        assertTrue(rutas.get(0).matches("/uploads/[a-f0-9-]+\\.png"));
        try (var archivos = Files.list(directorioTemporal)) {
            assertEquals(1, archivos.count());
        }
    }

    @Test
    void rechazaArchivosQueNoTienenFirmaDeImagenPermitida() {
        ImagenStorageService servicio = new ImagenStorageService(directorioTemporal.toString());
        MockMultipartFile archivo = new MockMultipartFile(
                "imagenes", "archivo.png", "image/png", "no es una imagen".getBytes());

        assertThrows(ImagenInvalidaException.class, () -> servicio.guardar(List.of(archivo), 10));
    }

    @Test
    void rechazaMasImagenesQueElLimiteDelFormulario() {
        ImagenStorageService servicio = new ImagenStorageService(directorioTemporal.toString());
        List<MultipartFile> archivos = List.of(imagenPng("uno.png"), imagenPng("dos.png"));

        assertThrows(ImagenInvalidaException.class, () -> servicio.guardar(archivos, 1));
    }

    private MockMultipartFile imagenPng(String nombre) {
        byte[] firmaPng = {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x00
        };
        return new MockMultipartFile("imagenes", nombre, "image/png", firmaPng);
    }
}
