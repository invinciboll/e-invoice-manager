package com.invinciboll.entities;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceEntity {

    private UUID invoiceId; // Primary Key
    private String originalFileSavePath; // Path stored as String
    private String generatedFileSavePath; // Path stored as String
    private String fileFormat; // Enum stored as String
    private String xmlFormat; // Enum stored as String
    private String sellerName;
    private String invoiceReference;
    private Integer invoiceTypeCode; // Nullable type numbers
    private LocalDate issuedDate; // Dates represented as LocalDate
    private BigDecimal totalSum; // Precise monetary values

    public InvoiceEntity(TempInvoice invoice, Path originalFileOutputPath, Path generatedFileOutputPath) {
        this.invoiceId = invoice.getInvoiceId();
        this.originalFileSavePath = originalFileOutputPath.toString()   ;
        this.generatedFileSavePath = generatedFileOutputPath.toString();
        this.fileFormat = invoice.getFileFormat().toString();
        this.xmlFormat = invoice.getXmlFormat().toString();
        this.sellerName = invoice.getKeyInformation().sellerName();
        this.invoiceReference = invoice.getKeyInformation().invoiceReference();
        this.invoiceTypeCode = invoice.getKeyInformation().invoiceTypeCode();
        this.issuedDate = invoice.getKeyInformation().issuedDate();
        this.totalSum = invoice.getKeyInformation().totalSum();
    }

    public InvoiceEntity(UUID invoiceId, String originalFileSavePath, String generatedFileSavePath,
        String fileFormat, String xmlFormat, String sellerName, String invoiceReference,
        Integer invoiceTypeCode, LocalDate issuedDate, BigDecimal totalSum) {
        this.invoiceId = invoiceId;
        this.originalFileSavePath = originalFileSavePath;
        this.generatedFileSavePath = generatedFileSavePath;
        this.fileFormat = fileFormat;
        this.xmlFormat = xmlFormat;
        this.sellerName = sellerName;
        this.invoiceReference = invoiceReference;
        this.invoiceTypeCode = invoiceTypeCode;
        this.issuedDate = issuedDate;
        this.totalSum = totalSum;
    }
}  