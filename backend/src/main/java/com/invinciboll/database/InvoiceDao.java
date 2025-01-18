package com.invinciboll.database;

import com.invinciboll.entities.InvoiceEntity;

import java.util.List;
import java.util.UUID;

public interface InvoiceDao {
    void save(InvoiceEntity invoice);
    InvoiceEntity findById(UUID invoiceId);
    List<InvoiceEntity> findAll();
    void deleteById(UUID invoiceId);

    boolean existsBySellerNameAndInvoiceReference(String sellerName, String invoiceReference, Integer invoiceTypeCode);

}
