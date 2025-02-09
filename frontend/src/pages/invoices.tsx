import { InvoiceTable } from "@/components/custom/invoices-table";
import { backendUrl } from "@/Envs";
import { useEffect, useState } from "react";

interface Invoice {
    invoiceId: string;
    sellerName: string;
    invoiceReference: string;
    invoiceTypeCode: number;
    issuedDate: string;
    totalSum: number;
    fileFormat: string;
}

const Invoices = () => {
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState("");

    // Set default sorting to descending by date
    const [sortColumn, setSortColumn] = useState<keyof Invoice>("issuedDate");
    const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");

    useEffect(() => {
        const fetchInvoices = async () => {
            try {
                setLoading(true);
                setError(null);

                const response = await fetch(`${backendUrl}/invoices`);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data: Invoice[] = await response.json();
                setInvoices(data);
            } catch (err) {
                setError((err as Error).message);
            } finally {
                setLoading(false);
            }
        };

        fetchInvoices();
    }, []);

    const sortedInvoices = [...invoices].sort((a, b) => {
        if (!sortColumn) return 0;
        const valueA = a[sortColumn];
        const valueB = b[sortColumn];

        if (typeof valueA === "string" && typeof valueB === "string") {
            return sortOrder === "asc"
                ? valueA.localeCompare(valueB)
                : valueB.localeCompare(valueA);
        }

        if (typeof valueA === "number" && typeof valueB === "number") {
            return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
        }

        if (sortColumn === "issuedDate") {
            const dateA = new Date(a.issuedDate);
            const dateB = new Date(b.issuedDate);
            return sortOrder === "asc"
                ? dateA.getTime() - dateB.getTime()
                : dateB.getTime() - dateA.getTime();
        }

        return 0;
    });

    const filteredInvoices = sortedInvoices.filter(
        (invoice) =>
            invoice.sellerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invoice.invoiceReference.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="h-screen w-screen flex flex-col items-center justify-start overflow-hidden">
            {loading ? (
                <p className="mt-10">Loading...</p>
            ) : error ? (
                <p className="text-red-500 mt-10">Error: {error}</p>
            ) : (
                <div
                    className="w-full max-w-5xl mt-10 p-8 shadow-lg rounded-md"
                    style={{ height: "700px" }} // Fixed height for consistent card size
                >
                    {/* Table Section */}
                    <InvoiceTable
                        invoices={invoices}
                        searchTerm={searchTerm}
                        setSearchTerm={setSearchTerm}
                        sortColumn={sortColumn}
                        setSortColumn={setSortColumn}
                        sortOrder={sortOrder}
                        setSortOrder={setSortOrder}
                        filteredInvoices={filteredInvoices}
                    />
                </div>
            )}
        </div>
    );
};

export default Invoices;
