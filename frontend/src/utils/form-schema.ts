import { z } from "zod";


export const FormSchemaNormalInvoice = z.object({
    sellerName: z.string().min(1, "Required"),
    invoiceReference: z.string().min(1, "Required"),
    invoiceType: z.number().min(1, "Required"),
    invoiceDate: z.date().min(new Date(2000, 1, 1), "Required"),
    totalSum: z.number().min(1, "Required"),
});