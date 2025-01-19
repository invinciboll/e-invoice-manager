package com.invinciboll.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.InvoiceEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.PathVariable;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/invoices") // Base URL for all endpoints in this controller
public class InvoicesController {

    private final InvoiceDao invoiceDao;

    @Autowired
    public InvoicesController(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    /**
     * Fetch all invoices
     * 
     * @return ResponseEntity containing the list of invoices and HTTP status
     */
    @GetMapping
    public ResponseEntity<List<InvoiceEntity>> getAllInvoices() {
        try {
            List<InvoiceEntity> invoices = invoiceDao.findAll();
            return ResponseEntity.ok(invoices); // 200 OK with the list of invoices
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null); // 500 Internal Server Error with no body
        }
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoicePdf(@PathVariable String invoiceId) {
        UUID id = UUID.fromString(invoiceId);
        InvoiceEntity invoice = invoiceDao.findById(id);
        String fileUrl = "http://localhost:4711/" + invoice.getGeneratedFileSavePath();

        try {
            Path path = Paths.get(invoice.getGeneratedFileSavePath());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while accessing the file");
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("fileUrl", fileUrl);
        return ResponseEntity.ok(responseBody);
    }

}