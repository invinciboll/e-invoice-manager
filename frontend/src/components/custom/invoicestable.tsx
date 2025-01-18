import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";

interface Invoice {
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
        placeholder="Filter by Seller or Reference Number..."
        value={searchTerm}
        onChange={handleSearchChange}
        className="mb-4"
      />

      {/* Table */}
      <Table>
        <TableCaption>A list of your recent invoices.</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead onClick={() => handleSort("issuedDate")}>
              Issue Date {sortColumn === "issuedDate" && (sortOrder === "asc" ? "↑" : "↓")}
            </TableHead>
            <TableHead onClick={() => handleSort("sellerName")}>
              Seller {sortColumn === "sellerName" && (sortOrder === "asc" ? "↑" : "↓")}
            </TableHead>
            <TableHead onClick={() => handleSort("invoiceReference")}>
              Reference Number {sortColumn === "invoiceReference" && (sortOrder === "asc" ? "↑" : "↓")}
            </TableHead>
            <TableHead onClick={() => handleSort("invoiceTypeCode")}>
              Invoice Type Number {sortColumn === "invoiceTypeCode" && (sortOrder === "asc" ? "↑" : "↓")}
            </TableHead>
            <TableHead className="text-right" onClick={() => handleSort("totalSum")}>
              Amount {sortColumn === "totalSum" && (sortOrder === "asc" ? "↑" : "↓")}
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {paginatedInvoices.map((invoice) => (
            <TableRow key={invoice.invoiceReference}>
              <TableCell>{new Date(invoice.issuedDate).toLocaleDateString()}</TableCell>
              <TableCell>{invoice.sellerName}</TableCell>
              <TableCell>{invoice.invoiceReference}</TableCell>
              <TableCell>{invoice.invoiceTypeCode}</TableCell>
              <TableCell className="text-right">
                {invoice.totalSum.toLocaleString("en-US", { style: "currency", currency: "USD" })}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {/* Pagination */}
      {totalPages > 1 && (
        <Pagination className="mt-4">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                onClick={() => handlePageChange(Math.max(currentPage - 1, 1))}
              />
            </PaginationItem>
            {Array.from({ length: totalPages }, (_, index) => (
              <PaginationItem key={index}>
                <PaginationLink
                  href="#"
                  isActive={currentPage === index + 1}
                  onClick={() => handlePageChange(index + 1)}
                >
                  {index + 1}
                </PaginationLink>
              </PaginationItem>
            ))}
            {totalPages > 5 && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            <PaginationItem>
              <PaginationNext
                href="#"
                onClick={() => handlePageChange(Math.min(currentPage + 1, totalPages))}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </div>
  );
};
