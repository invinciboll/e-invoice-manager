package com.invinciboll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.database.InvoiceDao;

@RestController
@RequestMapping("/meta")
public class MetaController {

    private final InvoiceDao invoiceDao;
    private final AppConfig appConfig;

    @Autowired
    public MetaController(InvoiceDao invoiceDao, AppConfig appConfig) {
        this.invoiceDao = invoiceDao;
        this.appConfig = appConfig;
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getSellers() {
        List<String> sellers = invoiceDao.findDistinctSellers();
        return ResponseEntity.ok(sellers);
    }

}
