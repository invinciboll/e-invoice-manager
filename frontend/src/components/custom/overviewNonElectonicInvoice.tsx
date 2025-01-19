import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { FileInfo, Progress } from "@/types";
import { InformationCircleIcon } from "@heroicons/react/24/outline";
import React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { format } from "date-fns";
import { de, enUS } from "date-fns/locale"; // Import locales for date-fns
import { CalendarIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import { Calendar } from "@/components/ui/calendar";

type OverviewNEIProps = {
  fileInfo: FileInfo;
  iframeRef: React.RefObject<HTMLIFrameElement>;
  handleResetPage: () => void;
};

const OverviewNEI: React.FC<OverviewNEIProps> = ({ fileInfo, handleResetPage }) => {
  const { t, i18n } = useTranslation();
  const currentLang = i18n.language; // Get current language
  const locale = currentLang === "de" ? de : enUS; // Set locale based on language

  const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
  const [isSaving, setIsSaving] = React.useState<Progress>(fileInfo.alreadyExists ? "DONE" : "NOT_STARTED");
  const [showPopup, setShowPopup] = React.useState(false);
  const [date, setDate] = React.useState<Date | undefined>();

  const [keyInformation, setKeyInformation] = React.useState(fileInfo.keyInformation);

  const handleInputChange = (field: keyof typeof keyInformation, value: any) => {
    setKeyInformation((prev) => ({ ...prev, [field]: value }));
  };

  const handleButtonClick = () => {
    if (isSaving !== "DONE") {
      setShowPopup(true); // Show the confirmation popup
    } else {
      handleResetPage(); // Proceed directly if the process is done
    }
  };

  const handleConfirmClose = () => {
    setShowPopup(false);
    handleResetPage();
  };

  const handleCancel = () => {
    setShowPopup(false);
  };

  function isElectronicInvoice(): boolean {
    return fileInfo.inputFormat === "ZF_PDF" || fileInfo.inputFormat === "XML";
  }

  return (
    <div className="w-full flex flex-col items-center text-left space-y-6">
      <h2 className="text-2xl font-bold">{t("overview.key-information")}</h2>

      {/* Table Section */}
      <div className="w-full">
        <table className="table-auto w-full">
          <tbody>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-is")}</td>
              <td className="p-2">
                {isElectronicInvoice() ? t("yes") : t("no")}
              </td>
            </tr>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-seller")}</td>
              <td className="p-2">
                <Input
                  value={keyInformation.sellerName || ""}
                  onChange={(e) => handleInputChange("sellerName", e.target.value)}
                  placeholder={t("overview.input.placeholder.seller-name")}
                />
              </td>
            </tr>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-number")}</td>
              <td className="p-2">
                <Input
                  value={keyInformation.invoiceReference || ""}
                  onChange={(e) => handleInputChange("invoiceReference", e.target.value)}
                  placeholder={t("overview.input.placeholder.invoice-number")}
                />
              </td>
            </tr>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-type")}</td>
              <td className="p-2">
                <HoverCard>
                  <HoverCardTrigger>
                    <InformationCircleIcon className="w-5 h-5 inline-block ml-1 text-gray-400" />
                  </HoverCardTrigger>
                  <HoverCardContent>
                    {/* Placeholder for Invoice Type Description */}
                  </HoverCardContent>
                </HoverCard>
              </td>
            </tr>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-date")}</td>
              <td className="p-2">
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant={"outline"}
                      className={cn(
                        "w-[240px] justify-start text-left font-normal",
                        !date && "text-muted-foreground"
                      )}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {date ? format(date, "PPP", { locale }) : <span>{t("pick-a-date")}</span>}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={date}
                      onSelect={setDate}
                      initialFocus
                      locale={locale}
                    />
                  </PopoverContent>
                </Popover>
              </td>
            </tr>
            <tr>
              <td className="p-2 font-semibold">{t("overview.table.header.invoice-amount")}</td>
              <td className="p-2">
                <Input
                  type="number"
                  value={keyInformation.totalSum !== undefined ? keyInformation.totalSum : ""}
                  onChange={(e) => handleInputChange("totalSum", parseFloat(e.target.value) || 0)}
                  placeholder={t("overview.input.placeholder.invoice-amount")}
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
    </div>
  );
};

export default OverviewNEI;
