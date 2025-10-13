package com.invinciboll.service.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.invinciboll.entities.Invoice;

@Component
public class InvoiceCache {

    private final Cache<UUID, Invoice> cache;

    public InvoiceCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS) // Set expiration time
                .maximumSize(50)                     // Set max cache size
                .removalListener(new RemovalListener<UUID, Invoice>() {
                    @Override
                    public void onRemoval(UUID key, Invoice invoice, RemovalCause cause) {
                        if (invoice != null) {
                            // Perform cleanup for temp files
                            deleteTempFile(invoice.getTempGeneratedFilePath());
                            deleteTempFile(invoice.getTempOriginalFilePath());
                        }
                    }
                })
                .build();
    }

    // Store an object in the cache
    public void put(Invoice invoice) {
        System.out.println("Put to cache - " + invoice.getInvoiceId());
        cache.put(invoice.getInvoiceId(), invoice);
    }

    // Retrieve an object from the cache
    public Invoice get(UUID invoiceId) {
        return cache.getIfPresent(invoiceId);
    }

    // Remove an object from the cache
    public void remove(UUID invoiceId) {
        cache.invalidate(invoiceId);
    }

    // Utility method to delete temp files
    private void deleteTempFile(Path filePath) {
        if (filePath != null && Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                System.out.println("Deleted temp file: " + filePath);
            } catch (Exception e) {
                System.err.println("Failed to delete temp file: " + filePath + " - " + e.getMessage());
            }
        }
    }
}
