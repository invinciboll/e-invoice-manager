package com.invinciboll.enums;

public enum FileFormat {
    XML,
    PDF,    // Regular PDF, not e-invoice
    ZF_PDF, // ZUGFeRD/Factur-X PDF
    INVALID;

    /**
     * Converts a string from invoice_metadata.json to a FileFormat enum value. (Used in tests)
     */
    public static FileFormat fromMetadata(String value) {
        if (value == null || value.isBlank()) {
            return INVALID;
        }

        String normalized = value.trim().toLowerCase();

        switch (normalized) {
            case "xml":
                return XML;
            case "plain-pdf":
                return PDF;
            case "factur-x":
                return ZF_PDF;
            default:
                return INVALID;
        }
    }
}
