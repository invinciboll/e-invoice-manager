import { FileInfo, InputFileFormat, Progress, TechnicalStandard } from "@/types";
import { Button } from "../ui/button";
import React from "react";
import { useTranslation } from "react-i18next";
import { formatDate } from "@/util";
import { useInvoiceTypeTranslator } from "@/invoiceTypesCodes";

type OverviewProps = {
  fileInfo: FileInfo;
  iframeRef: React.RefObject<HTMLIFrameElement>;
};


const Overview: React.FC<OverviewProps> = ({ fileInfo }) => {
  const { t } = useTranslation();
  const { translateInvoiceType } = useInvoiceTypeTranslator();
  // Add state to track if file is printed and saved
  const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
  const [isSaving, setIsSaving] = React.useState<Progress>(fileInfo.alreadyExists ? "DONE" : "NOT_STARTED");

  function isElectronicInvoice(): boolean {
    return fileInfo.inputFormat === "ZF_PDF" || fileInfo.inputFormat === "XML";
  }

  const handlePrint = async () => {
    try {
      setIsPrinting("IN_PROGRESS");
      const response = await fetch(`http://localhost:4711/print?invoiceId=${encodeURIComponent(fileInfo.id)}`, {
        method: 'POST'
      });

      if (response.ok) {
        setIsPrinting("DONE");
      } else {
        const errorMessage = await response.text(); // Read server error message
        console.error('Error:', errorMessage);
      }
    } catch (error) {
      console.error('Network or server error occurred:', error);
    }
  };

  const handlePersist = async() => {
    try {
      setIsSaving("IN_PROGRESS");
      const response = await fetch(`http://localhost:4711/persist?invoiceId=${encodeURIComponent(fileInfo.id)}`, {
        method: 'POST'
      });

      if (response.ok) {
        setIsSaving("DONE");
      } else {
        const errorMessage = await response.text(); // Read server error message
        console.error('Error:', errorMessage);
      }
    } catch (error) {
      console.error('Network or server error occurred:', error);
    }
  }

  return (
    <div className="w-full flex flex-col items-center text-left space-y-6">
      <h2 className="text-2xl font-bold">{t("overview.key-information")}</h2>
      <table className="table-auto">
        <tbody>
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-is")}</td>
            <td className="p-2 text-gray-600">
              {isElectronicInvoice() ? (
                "Yes"
              ) : (
                "No"
              )}
            </td>
          </tr>
          {/* <tr className="border-b border-gray-300">
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-format")}</td>
            <td className="p-2 text-gray-600">{fileInfo.inputFormat} ({fileInfo.technicalStandard})</td>
          </tr> */}
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-seller")}</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.sellerName}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-number")}</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.invoiceReference}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-type")}</td>
            <td className="p-2 text-gray-600">{translateInvoiceType(fileInfo.keyInformation.invoiceTypeCode)}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-date")}</td>
            <td className="p-2 text-gray-600">{formatDate(fileInfo.keyInformation.issuedDate)}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-amount")}</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.totalSum.toLocaleString("de-DE", { style: "currency", currency: "EUR" })}</td>
          </tr>
        </tbody>
      </table>
      {fileInfo.alreadyExists && <div> {t("overview.file-exists-message")}</div>}
      <div className="flex space-x-4">
        <Button 
          onClick={handlePrint} 
          variant="default" 
          className={`text-black w-32 ${isPrinting === "NOT_STARTED" ? 'bg-yellow-400 hover:bg-yellow-500': 'bg-gray-500 hover:bg-gray-600'}`}
          disabled={isPrinting === "IN_PROGRESS" || isPrinting === "DONE"}
        >
          {isPrinting === "IN_PROGRESS" ? (
            <svg className="animate-spin h-5 w-5 text-black" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
            </svg>
          ) : isPrinting === "DONE" ? (
            <span className="flex items-center">
              <svg className="w-5 h-5 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" d="M5 13l4 4L19 7"></path>
              </svg> 
            </span>
          ) : t("overview.button-print")}
        </Button>

        <Button 
          onClick={handlePersist} 
          variant="default" 
          className={`text-black w-32 ${isSaving === "NOT_STARTED" ? 'bg-yellow-400 hover:bg-yellow-500': 'bg-gray-500 hover:bg-gray-600'}`}
          disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
        >
          {isSaving === "IN_PROGRESS" ? (
            <svg className="animate-spin h-5 w-5 text-black" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
            </svg>
          ) : isSaving === "DONE" ? (
            <span className="flex items-center">
              <svg className="w-5 h-5 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" d="M5 13l4 4L19 7"></path>
              </svg> 
            </span>
          ) : t("overview.button-save")}
        </Button>
        
      </div>
    </div>
  );
}

export default Overview;