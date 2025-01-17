package com.invinciboll;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.invinciboll.entities.TempInvoice;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempInvoiceCache {

    private final Cache<UUID, TempInvoice> cache;

    public TempInvoiceCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // Set expiration time
                .maximumSize(1000)                     // Set max cache size
                .build();
    }

    // Store an object in the cache
    public void put(TempInvoice invoice) {;
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
}
