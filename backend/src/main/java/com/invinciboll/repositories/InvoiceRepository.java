package com.invinciboll.repositories;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.invinciboll.entities.InvoiceEntity;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID>{
    
}


