import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
    HoverCard,
    HoverCardContent,
    HoverCardTrigger,
} from "@/components/ui/hover-card";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { backendUrl } from "@/Envs";
import { useInvoiceTypeTranslator } from "@/invoiceTypesCodes";
import { FileInfo, Progress } from "@/types";
import { formatDate } from "@/util";
import { InformationCircleIcon } from "@heroicons/react/24/outline";
import React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "../ui/button";
import AnimatedButton from "./animated-button";

type OverviewProps = {
    fileInfo: FileInfo;
    iframeRef: React.RefObject<HTMLIFrameElement>;
    handleResetPage: () => void;
};

const Overview: React.FC<OverviewProps> = ({ fileInfo, handleResetPage }) => {
    const { t } = useTranslation();
    const { translateInvoiceType, getInvoiceTypeDescription } =
        useInvoiceTypeTranslator();
    // Add state to track if file is printed and saved
    const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
    const [isSaving, setIsSaving] = React.useState<Progress>(
        fileInfo.alreadyExists ? "DONE" : "NOT_STARTED"
    );
    const [showPopup, setShowPopup] = React.useState(false);
    const [persistErrorMessage, setPersistErrorMessage] = React.useState("");
    const [printErrorMessage, setPrintErrorMessage] = React.useState("");

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
            setPrintErrorMessage("");
            const response = await fetch(
                `${backendUrl}/print?invoiceId=${encodeURIComponent(fileInfo.id)}`,
                {
                    method: "POST",
                }
            );

            if (response.ok) {
                setIsPrinting("DONE");
            } else {
                const printErrorMessage = await response.text(); // Read server error message
                setPrintErrorMessage(printErrorMessage);
                setIsPrinting("NOT_STARTED");
            }
        } catch (error) {
            setIsPrinting("NOT_STARTED");
            setPrintErrorMessage("Print failed due to unexpected error.");
        }
    };

    const handlePersist = async () => {
        try {
            setIsSaving("IN_PROGRESS");
            setPersistErrorMessage("");
            const response = await fetch(
                `${backendUrl}/persist?invoiceId=${encodeURIComponent(fileInfo.id)}`,
                {
                    method: "POST",
                }
            );

            if (response.ok) {
                setIsSaving("DONE");
            } else {
                const persistErrorMessage = await response.text(); // Read server error message
                setIsSaving("NOT_STARTED");
                setPersistErrorMessage(persistErrorMessage);
            }
        } catch (error) {
            setIsSaving("NOT_STARTED");
            setPersistErrorMessage("Print failed due to unexpected error.");
        }
    };

    return (
        <div className="w-full flex flex-col items-center text-left space-y-6">
            <h2 className="text-2xl font-bold">{t("overview.key-information")}</h2>

            {/* Table Section */}
            <div className="w-full">
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
                                {t("overview.table.header.invoice-type")}
                            </td>
                            <td className="p-2">
                                {translateInvoiceType(fileInfo.keyInformation.invoiceTypeCode)}
                                <HoverCard>
                                    <HoverCardTrigger>
                                        <InformationCircleIcon className="w-5 h-5 inline-block ml-1 text-gray-400" />
                                    </HoverCardTrigger>
                                    <HoverCardContent>
                                        {getInvoiceTypeDescription(
                                            fileInfo.keyInformation.invoiceTypeCode
                                        )}
                                    </HoverCardContent>
                                </HoverCard>
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
                </table>
            </div>

            {/* Alert Section */}
            {fileInfo.alreadyExists && (
                <Alert className="w-full">
                    <InformationCircleIcon className="h-5 w-5" />
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
                        progress={isPrinting}
                        onClick={handlePrint}
                        disabled={isPrinting === "IN_PROGRESS" || isPrinting === "DONE"}
                    />

                    <AnimatedButton
                        translationIdentifier={"overview.button-save"}
                        progress={isSaving}
                        onClick={handlePersist}
                        disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
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

                {persistErrorMessage && (
                    <Alert variant="destructive" className="w-full">
                        <InformationCircleIcon className="h-5 w-5" />
                        <AlertTitle>{t("alert.error.persist-header")}</AlertTitle>
                        <AlertDescription>{persistErrorMessage}</AlertDescription>
                    </Alert>
                )}

                {printErrorMessage && (
                    <Alert variant="destructive" className="w-full">
                        <InformationCircleIcon className="h-5 w-5" />
                        <AlertTitle>{t("alert.error.print")}</AlertTitle>
                        <AlertDescription>{printErrorMessage}</AlertDescription>
                    </Alert>
                )}
            </div>
        </div>
    );
};

export default Overview;
