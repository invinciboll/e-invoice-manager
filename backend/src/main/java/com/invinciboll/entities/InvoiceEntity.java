package com.invinciboll.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class InvoiceEntity {

    
    @Id
    private UUID invoiceId; // Primary Key

    private String originalFileSavePath; // Path stored as String
    private String generatedFileSavePath; // Path stored as String

    @Enumerated(EnumType.STRING)
    private FileFormat fileFormat; // Enum stored as String
    @Enumerated(EnumType.STRING)
    private XMLFormat xmlFormat; // Enum stored as Stringz

    // Data extracted from XML or provided manually
    private String sellerName;
    private String invoiceReference;
    private Integer invoiceTypeCode; // Use Integer for nullable type numbers
    private LocalDate issuedDate; // Dates represented as LocalDate
    private LocalDate paymentDueDate; // Dates represented as LocalDate
    private BigDecimal totalSum; // Precise monetary values
}   //Der Gesamtbetrag der Rechnung mit Umsatzsteuer.
