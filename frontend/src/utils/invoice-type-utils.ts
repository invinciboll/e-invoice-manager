import { useTranslation } from "react-i18next";

export type InvoiceCode = 326 | 380 | 381 | 384 | 389 | 875 | 876 | 877;

// Define the mappings between codes and translation keys
export const invoiceTypeMappings: Record<InvoiceCode, string> = {
    326: "invoiceTypes.partialInvoice",
    380: "invoiceTypes.commercialInvoice",
    381: "invoiceTypes.creditNote",
    384: "invoiceTypes.correctedInvoice",
    389: "invoiceTypes.selfBilledInvoice",
    875: "invoiceTypes.partialConstructionInvoice",
    876: "invoiceTypes.partialFinalConstructionInvoice",
    877: "invoiceTypes.finalConstructionInvoice",
};

// Utility function to translate invoice types
export const useInvoiceTypeTranslator = () => {
    const { t } = useTranslation();

    /**
     * Translates an invoice code into the corresponding localized string.
     * @param code The invoice type code (e.g., 326, 380).
     * @returns The translated string in the current language.
     */
    const translateInvoiceType = (code: number): string => {
        const key = invoiceTypeMappings[code as InvoiceCode] ?? "invoiceTypes.unknown";
        return t(key);
    };

    const getInvoiceTypeDescription = (code: number): string => {

        const key = invoiceTypeMappings[code as InvoiceCode] ?? "invoiceTypes.unknown";
        return t(key + "-description")
    }

    return { translateInvoiceType, getInvoiceTypeDescription };
};


