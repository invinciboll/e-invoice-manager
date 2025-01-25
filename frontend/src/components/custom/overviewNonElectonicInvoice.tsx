"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { format } from "date-fns";
import { de, enUS } from "date-fns/locale";
import React from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as z from "zod";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
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

import { InformationCircleIcon } from "@heroicons/react/24/outline";
import { CalendarIcon } from "lucide-react";

import { backendUrl } from "@/Envs";
import {
    invoiceTypeMappings,
    useInvoiceTypeTranslator,
} from "@/utils/invoice-type-utils";
import { FileInfo, Progress } from "@/types";
import AnimatedButton from "./animated-button";

//------------------------//
// 1) Define Zod Schema
//------------------------//
const FormSchema = z.object({
    sellerName: z.string().min(1, "Required"),
    invoiceReference: z.string().optional(),
    invoiceType: z.string().optional(),
    invoiceDate: z.date().optional(),
    totalSum: z.number().optional(),
});

//------------------------//
// 2) Component Props
//------------------------//
type OverviewNEIProps = {
    fileInfo: FileInfo;
    iframeRef: React.RefObject<HTMLIFrameElement>;
    handleResetPage: () => void;
};

//------------------------//
// 3) Main Component
//------------------------//
const OverviewNEI: React.FC<OverviewNEIProps> = ({
    fileInfo,
    handleResetPage,
}) => {
    const { t, i18n } = useTranslation();
    const { translateInvoiceType } = useInvoiceTypeTranslator();
    const currentLang = i18n.language;
    const locale = currentLang === "de" ? de : enUS;

    // State for other operations
    const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
    const [isSaving, setIsSaving] = React.useState<Progress>(
        fileInfo.alreadyExists ? "DONE" : "NOT_STARTED"
    );
    const [showPopup, setShowPopup] = React.useState(false);

    //-------------------------------//
    // 4) Setup react-hook-form
    //-------------------------------//
    const form = useForm<z.infer<typeof FormSchema>>({
        resolver: zodResolver(FormSchema),
        defaultValues: {
            // Pull default values from fileInfo.keyInformation if available
            sellerName: fileInfo.keyInformation?.sellerName || "",
            invoiceReference: fileInfo.keyInformation?.invoiceReference || "",
            invoiceType: "", // We'll map the code to a string in the Select below
            invoiceDate: undefined,
            totalSum: fileInfo.keyInformation?.totalSum || 0,
        },
    });
    ////////////
    const handleButtonClick = () => {
        if (isSaving !== "DONE") {
            setShowPopup(true); // Show the confirmation popup
        } else {
            handleResetPage(); // Proceed directly if the process is done
        }
    };

    const handleConfirmClose = () => {
        setShowPopup(false); // Hide the popup
        handleResetPage(); // Proceed with closing
    };

    const handleCancel = () => {
        setShowPopup(false); // Simply hide the popup
    };

    const handlePrint = async () => {
        try {
            setIsPrinting("IN_PROGRESS");
            const response = await fetch(
                `${backendUrl}/print?invoiceId=${encodeURIComponent(fileInfo.id)}`,
                {
                    method: "POST",
                }
            );

            if (response.ok) {
                setIsPrinting("DONE");
            } else {
                const errorMessage = await response.text(); // Read server error message
                console.error("Error:", errorMessage);
            }
        } catch (error) {
            console.error("Network or server error occurred:", error);
        }
    };

    async function onSubmit(payload: z.infer<typeof FormSchema>) {
        try {
            setIsSaving("IN_PROGRESS");

            // Send POST request to your backend
            const response = await fetch(
                `${backendUrl}/persist?invoiceId=${encodeURIComponent(fileInfo.id)}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(payload),
                }
            );

            if (!response.ok) {
                setIsSaving("NOT_STARTED");
                throw new Error(`Error: ${response.status} ${response.statusText}`);
            }
            setIsSaving("DONE");
            const result = await response.json();

            // Handle success
            console.log("Form submitted successfully:", result);
            // alert("Form submitted successfully!")
        } catch (error) {
            // Handle errors
            console.error("Failed to submit the form:", error);
            // alert("Failed to submit the form. Please try again.")
        }
    }

    //---------------------------//
    // 7) Render
    //---------------------------//
    return (
        <div className="w-full flex flex-col items-center text-left space-y-6">
            <h2 className="text-2xl font-bold">{t("overview.key-information")}</h2>

            {/*
        shadcn/ui Form wrapper + native HTML <form> 
        using react-hook-form's onSubmit
      */}
            <Form {...form}>
                <form
                    onSubmit={form.handleSubmit(onSubmit)}
                    className="space-y-4 w-full"
                >
                    {/* Table Section */}
                    <div className="w-full">
                        <table className="table-auto w-full">
                            <tbody>
                                <tr>
                                    <td className="p-2 font-semibold">
                                        {t("overview.table.header.invoice-is")}
                                    </td>
                                    <td className="p-2">{t("no")}</td>
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
                                                        <Input placeholder="RX123456789" {...field} />
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
                                        {t("overview.table.header.invoice-type")}
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
                                                            onValueChange={field.onChange}
                                                            value={field.value}
                                                        >
                                                            <SelectTrigger className="w-[280px]">
                                                                <SelectValue
                                                                    placeholder={t(
                                                                        "overview.input.placeholder-type"
                                                                    )}
                                                                />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {Object.keys(invoiceTypeMappings).map(
                                                                    (typeCode) => (
                                                                        <SelectItem key={typeCode} value={typeCode}>
                                                                            {translateInvoiceType(Number(typeCode))}
                                                                        </SelectItem>
                                                                    )
                                                                )}
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
                                                        <Popover>
                                                            <PopoverTrigger asChild>
                                                                <Button
                                                                    variant={"outline"}
                                                                    className={cn(
                                                                        "w-[240px] justify-start text-left font-normal",
                                                                        !field.value && "text-muted-foreground"
                                                                    )}
                                                                >
                                                                    <CalendarIcon className="mr-2 h-4 w-4" />
                                                                    {field.value ? (
                                                                        format(field.value, "PPP", { locale })
                                                                    ) : (
                                                                        <span>{t("pick-a-date")}</span>
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
                                                                    onSelect={field.onChange}
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
                                                        <Input
                                                            placeholder={
                                                                currentLang === "de" ? "0,00" : "0.00"
                                                            }
                                                            // Display logic: replace "." with "," for German
                                                            value={
                                                                field.value
                                                                    ? currentLang === "de"
                                                                        ? field.value.toString().replace(".", ",")
                                                                        : field.value.toString()
                                                                    : ""
                                                            }
                                                            onChange={(e) => {
                                                                const input = e.target.value;
                                                                const parsedValue =
                                                                    currentLang === "de"
                                                                        ? parseFloat(input.replace(",", "."))
                                                                        : parseFloat(input);
                                                                if (!isNaN(parsedValue)) {
                                                                    field.onChange(parsedValue);
                                                                } else {
                                                                    field.onChange("");
                                                                }
                                                            }}
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

                    {/* Alert Section */}
                    {fileInfo.alreadyExists && (
                        <Alert className="w-full">
                            <InformationCircleIcon className="h-4 w-4" />
                            <AlertTitle>{t("alert.info.file-exists-header")}</AlertTitle>
                            <AlertDescription>
                                {t("alert.info.file-exists-message")}
                            </AlertDescription>
                        </Alert>
                    )}

                    {/* Buttons Section */}
                    <div className="flex flex-col items-center space-y-4 w-full">
                        <div className="flex space-x-4 w-full justify-center">
                            <AnimatedButton
                                translationIdentifier={"overview.button-print"}
                                onClick={handlePrint}
                                disabled={isPrinting === "IN_PROGRESS" || isPrinting === "DONE"}
                                progress={isPrinting}
                            />

                            <AnimatedButton
                                type="submit"
                                disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
                                progress={isSaving}
                                translationIdentifier={"overview.button-save"}
                                onClick={form.handleSubmit(onSubmit)}
                            />
                        </div>

                        <div className="w-full flex justify-center">
                            <Popover open={showPopup} onOpenChange={setShowPopup}>
                                <PopoverTrigger asChild>
                                    <Button
                                        onClick={handleButtonClick}
                                        variant="default"
                                        className="text-black w-32 bg-yellow-400 hover:bg-yellow-500"
                                        style={{ width: "calc(2 * 8rem + 1rem)" }}
                                    >
                                        {t("overview.button-close")}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-72">
                                    <h2 className="text-lg font-semibold mb-2">
                                        {t("popover.confirm-title")}
                                    </h2>
                                    <p className="text-gray-600 mb-4">
                                        {t("popover.confirm-message")}
                                    </p>
                                    <div className="flex justify-end space-x-2">
                                        <Button
                                            onClick={handleCancel}
                                            variant="outline"
                                            className="text-gray-700"
                                        >
                                            {t("popover.cancel")}
                                        </Button>
                                        <Button
                                            onClick={handleConfirmClose}
                                            variant="default"
                                            className="bg-red-500 text-white hover:bg-red-600"
                                        >
                                            {t("popover.confirm")}
                                        </Button>
                                    </div>
                                </PopoverContent>
                            </Popover>
                        </div>
                    </div>
                </form>
            </Form>
        </div>
    );
};

export default OverviewNEI;
