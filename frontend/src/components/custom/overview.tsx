import { FileInfo, InputFileFormat, Progress, TechnicalStandard } from "@/types";
import { Button } from "../ui/button";
import React from "react";
import { useTranslation } from "react-i18next";
import { formatDate } from "@/util";
import { useInvoiceTypeTranslator } from "@/invoiceTypesCodes";
import { InformationCircleIcon } from "@heroicons/react/24/outline";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"



type OverviewProps = {
  fileInfo: FileInfo;
  iframeRef: React.RefObject<HTMLIFrameElement>;
  handleResetPage: () => void;
};


const Overview: React.FC<OverviewProps> = ({ fileInfo, handleResetPage }) => {
  const { t } = useTranslation();
  const { translateInvoiceType, getInvoiceTypeDescription } = useInvoiceTypeTranslator();
  // Add state to track if file is printed and saved
  const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
  const [isSaving, setIsSaving] = React.useState<Progress>(fileInfo.alreadyExists ? "DONE" : "NOT_STARTED");
  const [showPopup, setShowPopup] = React.useState(false);

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

  const handlePersist = async () => {
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
      
      {/* Table Section */}
      <div className="w-full">
        <table className="table-auto w-full">
          <tbody>
            <tr>
              <td className="p-2 font-semibold text-gray-700">{t("overview.table.header.invoice-is")}</td>
              <td className="p-2 text-gray-600">
                {isElectronicInvoice() ? (
                  t("yes")
                ) : (
                  t("no")
                )}
              </td>
            </tr>
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
              <td className="p-2 text-gray-600">
                {translateInvoiceType(fileInfo.keyInformation.invoiceTypeCode)}
                <HoverCard>
                  <HoverCardTrigger>
                    <InformationCircleIcon className="w-5 h-5 inline-block ml-1 text-gray-400" />
                  </HoverCardTrigger>
                  <HoverCardContent>
                    {getInvoiceTypeDescription(fileInfo.keyInformation.invoiceTypeCode)}
                  </HoverCardContent>
                </HoverCard>
              </td>
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
          <Button
            onClick={handlePrint}
            variant="default"
            className={`text-black w-32 ${isPrinting === "NOT_STARTED" ? 'bg-yellow-400 hover:bg-yellow-500' : 'bg-gray-500 hover:bg-gray-600'}`}
            disabled={isPrinting === "IN_PROGRESS" || isPrinting === "DONE"}
          >
            {t("overview.button-print")}
          </Button>
  
          <Button
            onClick={handlePersist}
            variant="default"
            className={`text-black w-32 ${isSaving === "NOT_STARTED" ? 'bg-yellow-400 hover:bg-yellow-500' : 'bg-gray-500 hover:bg-gray-600'}`}
            disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
          >
            {t("overview.button-save")}
          </Button>
        </div>
  
        <div className="w-full flex justify-center">
          <Popover open={showPopup} onOpenChange={setShowPopup}>
            <PopoverTrigger asChild>
              <Button
                onClick={handleButtonClick}
                variant="default"
                className="text-black w-32 bg-yellow-400 hover:bg-yellow-500"
                style={{ width: 'calc(2 * 8rem + 1rem)' }}
              >
                {t("overview.button-close")}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-72">
              <h2 className="text-lg font-semibold mb-2">{t("popover.confirm-title")}</h2>
              <p className="text-gray-600 mb-4">{t("popover.confirm-message")}</p>
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
    </div>
  );  
}

export default Overview;