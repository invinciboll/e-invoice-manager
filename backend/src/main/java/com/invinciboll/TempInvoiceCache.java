package com.invinciboll;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.invinciboll.entities.TempInvoice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempInvoiceCache {

    private final Cache<UUID, TempInvoice> cache;

    public TempInvoiceCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS) // Set expiration time
                .maximumSize(50)                     // Set max cache size
                .removalListener(new RemovalListener<UUID, TempInvoice>() {
                    @Override
                    public void onRemoval(UUID key, TempInvoice invoice, RemovalCause cause) {
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
    public void put(TempInvoice invoice) {
        cache.put(invoice.getInvoiceId(), invoice);
    }

    // Retrieve an object from the cache
    public TempInvoice get(UUID invoiceId) {
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
