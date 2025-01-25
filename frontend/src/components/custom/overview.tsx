import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { backendUrl } from "@/Envs";
import { FileInfo, Progress } from "@/types";
import { InformationCircleIcon } from "@heroicons/react/24/outline";
import React from "react";
import { useTranslation } from "react-i18next";
import AnimatedButton from "./animated-button";
import { ConfirmSavePopover } from "./confirm-save-popover";
import { SummaryForm } from "./overview-summary-form";
import { SummaryTable } from "./overview-summary-table";
import { FormSchemaNormalInvoice } from "@/utils/form-schema";
import { z } from "zod";

type OverviewProps = {
    fileInfo: FileInfo;
    iframeRef: React.RefObject<HTMLIFrameElement>;
    handleResetPage: () => void;
};

const Overview: React.FC<OverviewProps> = ({ fileInfo, handleResetPage }) => {
    const { t } = useTranslation();

    const isNormalPdf = fileInfo.inputFormat === "PDF";

    const [isPrinting, setIsPrinting] = React.useState<Progress>("NOT_STARTED");
    const [isSaving, setIsSaving] = React.useState<Progress>(
        fileInfo.alreadyExists ? "DONE" : "NOT_STARTED"
    );

    const [persistErrorMessage, setPersistErrorMessage] = React.useState("");
    const [printErrorMessage, setPrintErrorMessage] = React.useState("");

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
                {fileInfo.inputFormat !== "PDF" ? (
                    <SummaryTable fileInfo={fileInfo} />
                ) : (
                    <SummaryForm fileInfo={fileInfo} formId="summaryForm" />)}
            </div>

            {/* Info Alert Section */}
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
                        onClick={handlePrint}
                        translationIdentifier={"overview.button-print"}
                        progress={isPrinting}
                        disabled={isPrinting === "IN_PROGRESS" || isPrinting === "DONE"}
                    />
                    {isNormalPdf ? (
                        <AnimatedButton
                            type="submit"
                            formId="summaryForm"
                            translationIdentifier={"overview.button-save"}
                            progress={isSaving}
                            disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
                        />
                    ) : (
                        <AnimatedButton
                            onClick={handlePersist}
                            translationIdentifier={"overview.button-save"}
                            progress={isSaving}
                            disabled={isSaving === "IN_PROGRESS" || isSaving === "DONE"}
                        />
                    )}
                </div>
            </div>
            <div className="w-full flex justify-center">
                <ConfirmSavePopover handleResetPage={handleResetPage} triggerPopover={isSaving !== "DONE"} />
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
    );
};

export default Overview;
