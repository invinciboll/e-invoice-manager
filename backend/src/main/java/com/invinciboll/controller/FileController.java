package com.invinciboll.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.TempInvoice;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.exceptions.CauseRetriever;
import com.invinciboll.exceptions.ParserException;
import com.invinciboll.exceptions.TransformationException;
import com.invinciboll.service.cache.TempInvoiceCache;
import com.invinciboll.service.xrechnung.XRechnungExtractor;
import com.invinciboll.service.xrechnung.XRechnungParser;
import com.invinciboll.service.xrechnung.XRechnungTransformer;

@RestController
public class FileController {
    private final TempInvoiceCache tempInvoiceCache;
    private final InvoiceDao invoiceDao;
    private final AppConfig appConfig;
    private final XRechnungExtractor extractor;
    private final XRechnungParser parser;
    private final XRechnungTransformer transformer;

    @Autowired
    public FileController(TempInvoiceCache tempInvoiceCache, InvoiceDao invoiceDao, AppConfig appConfig, XRechnungExtractor extractor, XRechnungParser parser, XRechnungTransformer transformer) {
        this.tempInvoiceCache = tempInvoiceCache;
        this.invoiceDao = invoiceDao;
        this.appConfig = appConfig;
        this.extractor = extractor;
        this.parser = parser;
        this.transformer = transformer;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
            @RequestParam("file") MultipartFile uploadedFile) {

        String contentType = uploadedFile.getContentType();
        if (contentType == null ||
            (!contentType.equals("application/pdf") &&
             !contentType.equals("application/xml") &&
             !contentType.equals("text/xml"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("File format is invalid, must be PDF or XML.");
        }

        TempInvoice temporaryInvoice = new TempInvoice(appConfig, extractor, parser, transformer);

        try {
            temporaryInvoice.setFile(uploadedFile);
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create temporary files: " + e.getMessage());
        }

        try {
            temporaryInvoice.process();
        } catch (ParserException | IOException | TransformationException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing invoice: " + CauseRetriever.getRootCause(e));
        }

        tempInvoiceCache.put(temporaryInvoice);

        Map<String, Object> responseBody = temporaryInvoice.prepareJSONResponse(invoiceDao);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/persist")
    @ResponseBody
    public ResponseEntity<?> persistInvoice(
            @RequestParam("invoiceId") String id,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid invoice ID format: " + e.getMessage());
        }

        TempInvoice invoice = tempInvoiceCache.get(invoiceId);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invoice not in cache. Try to upload/import the file again.");
        }

        if (requestBody != null && invoice.getFileFormat() == FileFormat.PDF) {
            // Set key information from user input (applicable only for non-e-invoice PDF files)
            try {
                invoice.setKeyInformationFromUserInput(requestBody);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid request body: " + e.getMessage()); // Invalid request body
            }
        }

        try {
            invoice.persist(invoiceDao);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to persist invoice: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }


    @PostMapping("/print")
    public ResponseEntity<?> printInvoice(@RequestParam("invoiceId") String id) {
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid invoice ID format.");
        }

        TempInvoice invoice = tempInvoiceCache.get(invoiceId);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Invoice not in cache. Try to upload/import the file again.");
        }

        try {
            invoice.print();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to print invoice: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
