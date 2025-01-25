import { z } from "zod";


export const FormSchemaNormalInvoice = z.object({
    sellerName: z.string().min(1, "Required"),
    invoiceReference: z.string().optional(),
    invoiceType: z.string().optional(),
    invoiceDate: z.date().optional(),
    totalSum: z.number().optional(),
});