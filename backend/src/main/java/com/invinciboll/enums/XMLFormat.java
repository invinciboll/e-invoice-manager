package com.invinciboll.enums;

public enum XMLFormat {
    UBL_INVOICE,
    UBL_CREDIT_NOTE,
    CII,
    // EN16931_COMPLIANT,
    UNKNOWN,
    NONE; // No XML content

    /**
     * Converts a string from invoice_metadata.json to a XMLFormat enum value. (Used in tests)
    */
    public static XMLFormat fromMetadata(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }

        String normalized = value.trim().toLowerCase();

        switch (normalized) {
            case "ubl_invoice":
                return UBL_INVOICE;
            case "ubl_credit_note":
                return UBL_CREDIT_NOTE;
            case "cii":
                return CII;
            default:
                return UNKNOWN;
        }
    }
}

