import i18next from "i18next";
import { z } from "zod";

export const FormSchemaNormalInvoice = z.object({
    sellerName: z.string().min(1, i18next.t("input-required")),
    invoiceReference: z.string().min(1, i18next.t("input-required")),
    invoiceType: z.number().min(1, i18next.t("input-required")),
    invoiceDate: z.date().min(new Date(1970, 1, 1), i18next.t("input-required")),
    totalSum: z.number().min(Number.NEGATIVE_INFINITY, i18next.t("input-required")),
});