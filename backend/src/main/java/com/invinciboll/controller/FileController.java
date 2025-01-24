package com.invinciboll.controller;

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

import com.invinciboll.TempInvoiceCache;
import com.invinciboll.configuration.AppConfig;
import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.TempInvoice;
import com.invinciboll.enums.ErrorCode;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.exceptions.CauseRetriever;

@RestController
public class FileController {
    private static final TempInvoiceCache cache = new TempInvoiceCache();
    private final InvoiceDao invoiceDao;
    private final AppConfig appConfig;

    @Autowired
    public FileController(InvoiceDao invoiceDao, AppConfig appConfig) {
        this.invoiceDao = invoiceDao;
        this.appConfig = appConfig;
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

        TempInvoice temporaryInvoice = new TempInvoice(appConfig);
        try {
            temporaryInvoice.setFile(uploadedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage() + " Please retry and contact the admin if the issue persists." );
        }

        try {
            temporaryInvoice.process();
        } catch (Exception e) {
            Throwable cause = CauseRetriever.getRootCause(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + cause.getMessage());
        }

        cache.put(temporaryInvoice);
  
        Map<String, Object> responseBody = temporaryInvoice.prepareJSONResponse(invoiceDao); 
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/persist")
    @ResponseBody
    public ResponseEntity<?> persistInvoice(
            @RequestParam("invoiceId") String id, 
            @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            UUID invoiceId = UUID.fromString(id);
            System.out.println("Request Body: " + requestBody + ", Invoice ID: " + invoiceId);
            TempInvoice invoice = cache.get(invoiceId);
            System.out.println("Invoice: " + invoice);
            if (invoice == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorCode.ERR002.getMessage()); // Invoice not found
            }

            // Process the body if it's not empty and the format is PDF
            if (requestBody != null && invoice.getFileFormat() == FileFormat.PDF) {
                // Assuming setKeyInformationFromUserInput accepts a Map or JSON string
                try {
                    invoice.setKeyInformationFromUserInput(requestBody);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(e.toString()); // Invalid request body
                }
;            }

            // Persist the invoice
            invoice.persist(invoiceDao);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid invoice ID format.");
        } catch (Exception e) {
            // Generic error handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }


    @PostMapping("/print")
    public ResponseEntity<?> printInvoice(@RequestParam("invoiceId") String id) {
        UUID invoiceId = UUID.fromString(id);
        TempInvoice invoice = cache.get(invoiceId);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invoice ID not found"); // Invoice not found
        }

        // invoice.print();

        return ResponseEntity.ok().build();
    }
}
