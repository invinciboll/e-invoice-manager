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

import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.Invoice;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.exceptions.CauseRetriever;
import com.invinciboll.exceptions.ParserException;
import com.invinciboll.exceptions.TransformationException;
import com.invinciboll.service.cache.InvoiceCache;
import com.invinciboll.service.processing.FileService;
import com.invinciboll.service.processing.InvoiceProcessingService;

@RestController
public class FileController {
    private final InvoiceCache tempInvoiceCache;
    private final InvoiceDao invoiceDao;
    private final InvoiceProcessingService invoiceProcessingService;
    private final FileService fileService;

    @Autowired
    public FileController(InvoiceCache tempInvoiceCache, InvoiceDao invoiceDao, InvoiceProcessingService invoiceProcessingService, FileService fileService) {
        this.tempInvoiceCache = tempInvoiceCache;
        this.invoiceDao = invoiceDao;
        this.invoiceProcessingService = invoiceProcessingService;
        this.fileService = fileService;
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

        Invoice tempInvoice;
        try {
           tempInvoice = invoiceProcessingService.createNewInvoice(uploadedFile);
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving uploaded file: " + CauseRetriever.getRootCause(e));
        }


        try {
            invoiceProcessingService.processInvoice(tempInvoice);
        } catch (ParserException | IOException | TransformationException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing invoice: " + CauseRetriever.getRootCause(e));
        }

        tempInvoiceCache.put(tempInvoice);
        Map<String, Object> responseBody = invoiceProcessingService.prepareJSONResponse(invoiceDao, tempInvoice);
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

        Invoice invoice = tempInvoiceCache.get(invoiceId);
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
            invoiceProcessingService.persist(invoiceDao, invoice);
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

        Invoice invoice = tempInvoiceCache.get(invoiceId);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Invoice not in cache. Try to upload/import the file again.");
        }

        try {
            fileService.print(invoice.getTempGeneratedFilePath());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to print invoice: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
