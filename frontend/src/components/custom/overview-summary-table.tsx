
import { FileInfo } from "@/types";
import { formatDate } from "@/utils/date-utils";
import { useInvoiceTypeTranslator } from "@/utils/invoice-type-utils";
import React from "react";
import { useTranslation } from "react-i18next";
import { InvoiceTypeInfoSheet } from "./invoice-type-info-sheet";

type SummaryTableProps = {
    fileInfo: FileInfo;
};


export const SummaryTable: React.FC<SummaryTableProps> = ({ fileInfo }) => {
    const { translateInvoiceType, getInvoiceTypeDescription } = useInvoiceTypeTranslator();
    const { t } = useTranslation();

    function isElectronicInvoice(): boolean {
        return fileInfo.inputFormat === "ZF_PDF" || fileInfo.inputFormat === "XML";
    }

    return (
        <table className="table-auto w-full">
            <tbody>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-is")}
                    </td>
                    <td className="p-2">
                        {isElectronicInvoice() ? t("yes") : t("no")}
                    </td>
                </tr>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-seller")}
                    </td>
                    <td className="p-2">{fileInfo.keyInformation.sellerName}</td>
                </tr>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-number")}
                    </td>
                    <td className="p-2">
                        {fileInfo.keyInformation.invoiceReference}
                    </td>
                </tr>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-type")} <InvoiceTypeInfoSheet />
                    </td>
                    <td className="p-2">
                        {translateInvoiceType(fileInfo.keyInformation.invoiceTypeCode)}
                    </td>
                </tr>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-date")}
                    </td>
                    <td className="p-2">
                        {formatDate(fileInfo.keyInformation.issuedDate)}
                    </td>
                </tr>
                <tr>
                    <td className="p-2 font-semibold">
                        {t("overview.table.header.invoice-amount")}
                    </td>
                    <td className="p-2">
                        {fileInfo.keyInformation.totalSum.toLocaleString("de-DE", {
                            style: "currency",
                            currency: "EUR",
                        })}
                    </td>
                </tr>
            </tbody>
        </table>);
};