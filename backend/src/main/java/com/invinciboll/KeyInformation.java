package com.invinciboll;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KeyInformation(
    String invoiceReference,
    String sellerName,
    Integer invoiceTypeCode,
    LocalDate issuedDate,
    BigDecimal totalSum
) {
    @Override
    public String toString() {
        return """
            KeyInformation:
                invoiceReference: %s
                sellerName: %s
                invoiceTypeCode: %s
                issuedDate: %s
                totalSum: %s
            """.formatted(
                invoiceReference,
                sellerName,
                invoiceTypeCode,
                issuedDate,
                totalSum
            );
    }
}
