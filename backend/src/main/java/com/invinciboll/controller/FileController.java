package com.invinciboll.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.TempInvoiceCache;
import com.invinciboll.entities.TempInvoice;
import com.invinciboll.enums.ErrorCode;

@RestController
public class FileController {
    private static final TempInvoiceCache cache = new TempInvoiceCache();

    @PostMapping("/upload") 
    public ResponseEntity<?> handleFileUpload( 
            @RequestParam("file") MultipartFile uploadedFile) {

        String contentType = uploadedFile.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/pdf") &&
             !contentType.equals("application/xml") &&
             !contentType.equals("text/xml"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(ErrorCode.ERR001.getMessage()); // Invalid file Type
        }

        TempInvoice temporaryInvoice = new TempInvoice();
        try {
            temporaryInvoice.setFile(uploadedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorCode.ERR004.getMessage()); 
        }

        try {
            temporaryInvoice.process();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

        cache.put(temporaryInvoice);

        Map<String, Object> responseBody = temporaryInvoice.prepareJSONResponse();   
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/persist")
    public ResponseEntity<?> persistInvoice(@RequestParam("invoiceId") String id) {
        System.out.println("Persisting invoice with ID: " + id);
        UUID invoiceId = UUID.fromString(id);
        TempInvoice invoice = cache.get(invoiceId);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorCode.ERR002.getMessage()); // Invoice not found
        }

        invoice.persist();
        cache.remove(invoiceId);

        return ResponseEntity.ok().build();
    }
}
