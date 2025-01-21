package com.wordtopdf.controller;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/conversion")
public class WordToPdfController {

    @PostMapping("/word-to-pdf")
    public ResponseEntity<?> convertWordToPdf(@RequestParam("file") MultipartFile wordFile) {
        if (wordFile == null || wordFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No se ha subido ningún archivo.");
        }

        // Rutas temporales para guardar los archivos
        String inputFileName = wordFile.getOriginalFilename();
        if (inputFileName == null) {
            return ResponseEntity.badRequest().body("Nombre de archivo no válido.");
        }

        Path inputFilePath = Paths.get(System.getProperty("java.io.tmpdir"), inputFileName);
        Path outputFilePath = Paths.get(System.getProperty("java.io.tmpdir"), inputFileName.replaceAll("\\.\\w+$", ".pdf"));

        try {
            // Guardar el archivo temporalmente
            Files.write(inputFilePath, wordFile.getBytes());

            // Ruta de instalación de LibreOffice
            String libreOfficePath = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";
            String[] command = {
                libreOfficePath,
                "--headless",
                "--convert-to", "pdf",
                "--outdir", inputFilePath.getParent().toString(),
                inputFilePath.toString()
            };

            // Ejecutar LibreOffice
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

            // Esperar a que el proceso termine
            process.waitFor();

            // Verificar si se generó el archivo PDF
            if (!Files.exists(outputFilePath)) {
                throw new IOException("Error al generar el archivo PDF.");
            }

            // Leer el archivo PDF y devolverlo al cliente
            byte[] pdfBytes = Files.readAllBytes(outputFilePath);
            Files.delete(inputFilePath); // Eliminar archivo de entrada temporal
            Files.delete(outputFilePath); // Eliminar archivo PDF temporal

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=documento.pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + ex.getMessage());
        }
    }

}
