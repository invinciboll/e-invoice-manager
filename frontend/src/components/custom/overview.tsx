import { FileInfo, InputFileFormat, Progress, TechnicalStandard } from "@/types";
import { Button } from "../ui/button";
import React from "react";

type OverviewProps = {
  fileInfo: FileInfo;
  iframeRef: React.RefObject<HTMLIFrameElement>;
};


const Overview: React.FC<OverviewProps> = ({ fileInfo, iframeRef }) => {

  // Add state to track if file is printed and saved
  const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
  const [isSaving, setIsSaving] = React.useState<Progress>("NOT_STARTED");



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
      <h2 className="text-2xl font-bold">Overview</h2>
      <table className="table-auto border-collapse border border-gray-300">
        <tbody>
          <tr className="border-b border-gray-300">
            <td className="p-2 font-semibold text-gray-700">E-Invoice</td>
            <td className="p-2 text-gray-600 flex">
              {isElectronicInvoice() ? (
                "Yes"
              ) : (
                "No"
              )}
            </td>
          </tr>
          <tr className="border-b border-gray-300">
            <td className="p-2 font-semibold text-gray-700">Format</td>
            <td className="p-2 text-gray-600">{fileInfo.inputFormat} ({fileInfo.technicalStandard})</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">Seller</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.sellerName}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">Invoice Ref.</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.invoiceReference}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">Invoice Type</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.invoiceTypeCode}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">Issue Date</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.issuedDate}</td>
          </tr>
          <tr>
            <td className="p-2 font-semibold text-gray-700">Total Sum</td>
            <td className="p-2 text-gray-600">{fileInfo.keyInformation.totalSum}</td>
          </tr>
        </tbody>
      </table>
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
          ) : 'Print'}
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
          ) : 'Save'}
        </Button>
        
      </div>
    </div>
  );
}

export default Overview;