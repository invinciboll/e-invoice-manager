import { Input } from "@/components/ui/input";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { backendUrl } from "@/Envs";
import { useInvoiceTypeTranslator } from "@/invoiceTypesCodes";
import { formatDate } from "@/util";
import { ArrowTopRightOnSquareIcon } from "@heroicons/react/24/solid";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";

interface Invoice {
    invoiceId: string;
    sellerName: string;
    invoiceReference: string;
    invoiceTypeCode: number;
    issuedDate: string;
    totalSum: number;
    fileFormat: string;
}

interface InvoiceTableProps {
    invoices: Invoice[];
    searchTerm: string;
    setSearchTerm: (value: string) => void;
    sortColumn: keyof Invoice | null;
    setSortColumn: (column: keyof Invoice) => void;
    sortOrder: "asc" | "desc";
    setSortOrder: (order: "asc" | "desc") => void;
    filteredInvoices: Invoice[];
}

export const InvoiceTable: React.FC<InvoiceTableProps> = ({
    searchTerm,
    setSearchTerm,
    sortColumn,
    setSortColumn,
    sortOrder,
    setSortOrder,
    filteredInvoices,
}) => {
    const { t } = useTranslation();
    const { translateInvoiceType } = useInvoiceTypeTranslator();
    const [currentPage, setCurrentPage] = useState(1);
    const rowsPerPage = 10;

    // Calculate pagination
    const totalPages = Math.ceil(filteredInvoices.length / rowsPerPage);
    const paginatedInvoices = filteredInvoices.slice(
        (currentPage - 1) * rowsPerPage,
        currentPage * rowsPerPage
    );

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(event.target.value);
        setCurrentPage(1); // Reset to first page on search
    };

    const handleSort = (column: keyof Invoice) => {
        if (sortColumn === column) {
            setSortOrder(sortOrder === "asc" ? "desc" : "asc"); // Toggle sort order
        } else {
            setSortColumn(column);
            setSortOrder("asc"); // Default to ascending order
        }
    };

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    return (
        <div className="mt-6 w-full max-w-4xl">
            {/* Search Input */}
            <Input
                placeholder={t("invoices.filter-input")}
                value={searchTerm}
                onChange={handleSearchChange}
                className="mb-4"
            />
            {/* Table */}
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead onClick={() => handleSort("issuedDate")}>
                            {t("invoices.table.issue-date")}{" "}
                            {sortColumn === "issuedDate" && (sortOrder === "asc" ? "↑" : "↓")}
                        </TableHead>
                        <TableHead onClick={() => handleSort("sellerName")}>
                            {t("invoices.table.seller")}{" "}
                            {sortColumn === "sellerName" && (sortOrder === "asc" ? "↑" : "↓")}
                        </TableHead>
                        <TableHead onClick={() => handleSort("invoiceReference")}>
                            {t("invoices.table.reference-number")}{" "}
                            {sortColumn === "invoiceReference" &&
                                (sortOrder === "asc" ? "↑" : "↓")}
                        </TableHead>
                        <TableHead onClick={() => handleSort("invoiceTypeCode")}>
                            {t("invoices.table.type-number")}{" "}
                            {sortColumn === "invoiceTypeCode" &&
                                (sortOrder === "asc" ? "↑" : "↓")}
                        </TableHead>
                        <TableHead
                            className="text-right"
                            onClick={() => handleSort("totalSum")}
                        >
                            {t("invoices.table.amount")}{" "}
                            {sortColumn === "totalSum" && (sortOrder === "asc" ? "↑" : "↓")}
                        </TableHead>
                        <TableHead />
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {paginatedInvoices.map((invoice) => (
                        <TableRow key={invoice.invoiceId}>
                            <TableCell>{formatDate(invoice.issuedDate)}</TableCell>
                            <TableCell>{invoice.sellerName}</TableCell>
                            <TableCell>{invoice.invoiceReference}</TableCell>
                            <TableCell>
                                {translateInvoiceType(invoice.invoiceTypeCode)}
                            </TableCell>
                            <TableCell className="text-right">
                                {invoice.totalSum.toLocaleString("de-DE", {
                                    style: "currency",
                                    currency: "EUR",
                                })}
                            </TableCell>
                            <TableCell>
                                <ArrowTopRightOnSquareIcon
                                    onClick={() => {
                                        // Send request with invoice.id
                                        fetch(`${backendUrl}/invoices/${invoice.invoiceId}`, {
                                            method: "GET",
                                        })
                                            .then((response) => response.json())
                                            .then((data) => {
                                                if (data.fileUrl) {
                                                    // Open the PDF in a new tab
                                                    window.open(data.fileUrl, "_blank");
                                                } else {
                                                    console.error(
                                                        "File URL not found in response:",
                                                        data
                                                    );
                                                }
                                            })
                                            .catch((error) => {
                                                console.error("Error fetching invoice:", error);
                                            });
                                    }}
                                    className="h-5 w-5 cursor-pointer hover:text-yellow-400 transition-colors"
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            {/* Pagination */}
            {totalPages > 1 && (
                <Pagination className="mt-4">
                    <PaginationContent>
                        {/* Previous Button */}
                        <PaginationItem>
                            <PaginationPrevious
                                href="#"
                                onClick={() => handlePageChange(Math.max(currentPage - 1, 1))}
                                className="px-4 py-2 rounded text-black dark:text-white hover:bg-yellow-400 hover:text-white transition-colors"
                            >
                                {t("invoices.pagination.previous")}
                            </PaginationPrevious>
                        </PaginationItem>

                        {/* Pagination Numbers */}
                        {Array.from({ length: totalPages }, (_, index) => (
                            <PaginationItem key={index}>
                                <PaginationLink
                                    href="#"
                                    isActive={currentPage === index + 1}
                                    onClick={() => handlePageChange(index + 1)}
                                    className={`px-4 py-2 rounded ${currentPage === index + 1
                                            ? "bg-yellow-400 text-white"
                                            : "text-black dark:text-white hover:bg-yellow-400 hover:text-white"
                                        } transition-colors`}
                                >
                                    {index + 1}
                                </PaginationLink>
                            </PaginationItem>
                        ))}

                        {/* Ellipsis for Overflow */}
                        {totalPages > 5 && (
                            <PaginationItem>
                                <PaginationEllipsis className="text-black dark:text-white" />
                            </PaginationItem>
                        )}

                        {/* Next Button */}
                        <PaginationItem>
                            <PaginationNext
                                href="#"
                                onClick={() =>
                                    handlePageChange(Math.min(currentPage + 1, totalPages))
                                }
                                className="px-4 py-2 rounded text-black dark:text-white hover:bg-yellow-400 hover:text-white transition-colors"
                            >
                                {t("invoices.pagination.next")}
                            </PaginationNext>
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            )}
        </div>
    );
};
