package com.invinciboll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.InvoiceEntity;

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
}
