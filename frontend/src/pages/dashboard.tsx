import { useState, useEffect } from "react";
import { GraphsPanel } from "@/components/custom/graphspanel";

interface Invoice {
  sellerName: string;
  invoiceReference: string;
  invoiceTypeCode: number;
  issuedDate: string;
  totalSum: number;
  fileFormat: string;
}

const Dashboard = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchInvoices = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await fetch("http://localhost:4711/invoices");
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

  return (
    <div className="h-full w-full flex flex-col items-center justify-center pt-[6rem]">
      {loading ? (
        <p>Loading...</p>
      ) : error ? (
        <p className="text-red-500">Error: {error}</p>
      ) : (
        <>
          {/* Graphs Section */}
          <GraphsPanel invoices={invoices} />
        </>
      )}
    </div>
  );
};

export default Dashboard;
