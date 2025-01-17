package com.wordtopdf.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/api/conversion")
public class WordToPdfController {
    
    // Ruta que devuelve el archivo convertido en bytes
    @PostMapping("/word-to-pdf-bytes")
    public ResponseEntity<byte[]> convertWordToPdfBytes(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {

            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, pdfOutputStream);
            pdfDocument.open();

            document.getParagraphs().forEach(paragraph -> {
                try {
                    pdfDocument.add(new Paragraph(paragraph.getText()));
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            });

            pdfDocument.close();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=converted.pdf")
                    .body(pdfOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Ruta que devuelve el archivo convertido en bits (stream de datos binarios)
    @PostMapping("/word-to-pdf-bits")
    public ResponseEntity<InputStream> convertWordToPdfBits(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {

            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, pdfOutputStream);
            pdfDocument.open();

            document.getParagraphs().forEach(paragraph -> {
                try {
                    pdfDocument.add(new Paragraph(paragraph.getText()));
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            });

            pdfDocument.close();

            // Convertir el contenido del ByteArrayOutputStream a un InputStream
            InputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());

            // Retornar el stream como una respuesta de bits (sin cuerpo de la respuesta)
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=converted.pdf")
                    .body(pdfInputStream); // Retornar un InputStream como respuesta (stream de bits)
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
