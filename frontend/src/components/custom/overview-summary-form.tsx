
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { FileInfo } from "@/types";
import { zodResolver } from "@hookform/resolvers/zod";
import { format } from "date-fns";
import { de, enUS } from "date-fns/locale";
import React from "react";
import CurrencyInput from 'react-currency-input-field';
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as z from "zod";

import { CalendarIcon } from "lucide-react";

import { backendUrl } from "@/Envs";
import { Progress } from "@/types";
import { FormSchemaNormalInvoice } from "@/utils/form-schema";
import {
    invoiceTypeMappings
} from "@/utils/invoice-type-utils";
import { InvoiceTypeInfoSheet } from "./invoice-type-info-sheet";

type SummaryFormProps = {
    fileInfo: FileInfo;
    formId: string;
    updateSubmitButtonState: (state: Progress) => void;
};


export const SummaryForm: React.FC<SummaryFormProps> = ({ fileInfo, formId, updateSubmitButtonState }) => {
    const { t, i18n } = useTranslation();
    const currentLang = i18n.language;
    const locale = currentLang === "de" ? de : enUS;
    const [isCalendarOpen, setIsCalendarOpen] = React.useState(false)

    const form = useForm<z.infer<typeof FormSchemaNormalInvoice>>({
        resolver: zodResolver(FormSchemaNormalInvoice),
    });

    async function onSubmit(payload: z.infer<typeof FormSchemaNormalInvoice>) {
        try {
            updateSubmitButtonState("IN_PROGRESS");
            const p = {
                ...payload,
                // Convert the date to the desired format or adjust timezone
                invoiceDate: payload.invoiceDate
                    ? format(payload.invoiceDate, "yyyy-MM-dd") // Or adjust as needed
                    : null,
            };

            const response = await fetch(
                `${backendUrl}/persist?invoiceId=${encodeURIComponent(fileInfo.id)}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(p),
                }
            );

            if (!response.ok) {
                updateSubmitButtonState("NOT_STARTED");
                throw new Error(`Error: ${response.status} ${response.statusText}`);
            }
            updateSubmitButtonState("DONE");

            console.log("Form submitted successfully:", p);
        } catch (error) {
            console.error("Failed to submit the form:", error);
        }
    }


    return (
        <Form {...form}>
            <form
                id={formId}
                onSubmit={form.handleSubmit(onSubmit)}
                className="space-y-4 w-full"
            >
                {/* Table Section */}
                <div className="w-full">
                    <table className="table-fixed w-full">
                        <tbody>
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-is")}
                                </td>
                                <td className="p-2"><Input value={t("no")} disabled={true} /></td>
                            </tr>

                            {/* Seller Name Field */}
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-seller")}
                                </td>
                                <td className="p-2">
                                    <FormField
                                        control={form.control}
                                        name="sellerName"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel className="sr-only">
                                                    {t("overview.table.header.invoice-seller")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input placeholder="Lorem GmbH" {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </td>
                            </tr>

                            {/* Invoice Reference Field */}
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-number")}
                                </td>
                                <td className="p-2">
                                    <FormField
                                        control={form.control}
                                        name="invoiceReference"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel className="sr-only">
                                                    {t("overview.table.header.invoice-number")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input placeholder="RX1234CF56789" {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </td>
                            </tr>

                            {/* Invoice Type Field */}
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-type")} <InvoiceTypeInfoSheet />
                                </td>
                                <td className="p-2">
                                    <FormField
                                        control={form.control}
                                        name="invoiceType"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel className="sr-only">
                                                    {t("overview.table.header.invoice-type")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Select
                                                        onValueChange={(value) => field.onChange(Number(value))} // Convert the value back to a number for internal use
                                                        value={String(field.value)} // Display the currently selected `typeCode` as a string
                                                    >
                                                        <SelectTrigger className="w-full">
                                                            <SelectValue
                                                                placeholder={t("overview.input.placeholder-type")}
                                                            />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {Array.from(invoiceTypeMappings.entries()).map(([typeCode, translationIdentifier]) => {
                                                                return (
                                                                    <SelectItem key={String(typeCode)} value={String(typeCode)}>
                                                                        {t(translationIdentifier)} {/* Display the associated string */}
                                                                    </SelectItem>
                                                                );
                                                            })}
                                                        </SelectContent>
                                                    </Select>
                                                </FormControl>

                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </td>
                            </tr>

                            {/* Invoice Date Field */}
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-date")}
                                </td>
                                <td className="p-2">
                                    <FormField
                                        control={form.control}
                                        name="invoiceDate"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel className="sr-only">
                                                    {t("overview.table.header.invoice-date")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen} >
                                                        <PopoverTrigger asChild>
                                                            <Button
                                                                variant={"outline"}
                                                                className={cn(
                                                                    "w-full justify-start text-left font-normal",
                                                                    !field.value && "text-muted-foreground"
                                                                )}
                                                            >
                                                                <CalendarIcon className="mr-2 h-4 w-4" />
                                                                {field.value ? (
                                                                    format(field.value, "PPP", { locale })
                                                                ) : (
                                                                    <span>{t("overview.input.placeholder-date")}</span>
                                                                )}
                                                            </Button>
                                                        </PopoverTrigger>
                                                        <PopoverContent
                                                            className="w-auto p-0"
                                                            align="start"
                                                        >
                                                            <Calendar
                                                                mode="single"
                                                                selected={field.value}
                                                                onSelect={(e) => { field.onChange(e); setIsCalendarOpen(false); }}
                                                                initialFocus
                                                                locale={locale}
                                                            />
                                                        </PopoverContent>
                                                    </Popover>
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                </td>
                            </tr>
                            {/* Invoice Amount Field */}
                            <tr>
                                <td className="p-2 font-semibold">
                                    {t("overview.table.header.invoice-amount")}
                                </td>
                                <td className="p-2">
                                    <FormField
                                        control={form.control}
                                        name="totalSum"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel className="sr-only">
                                                    {t("overview.table.header.invoice-amount")}
                                                </FormLabel>
                                                <FormControl>
                                                    <CurrencyInput
                                                        intlConfig={i18n.language === "en" ? { locale: 'en-GB', currency: 'EUR' } : { locale: 'de-DE', currency: 'EUR' }}
                                                        id="input-totalSum"
                                                        name="input-totalSum"
                                                        placeholder={i18n.language === "en" ? "€ 0.00" : "0.00 €"}
                                                        decimalsLimit={2}
                                                        decimalScale={2}
                                                        onValueChange={(value, name, values) => field.onChange(values?.float)}
                                                        className={cn(
                                                            "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-base shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
                                                        )}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </form>
        </Form>);
};