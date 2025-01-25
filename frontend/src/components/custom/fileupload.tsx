import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { backendUrl } from "@/Envs";
import { FileInfo, Progress } from "@/types";
import { DocumentCurrencyEuroIcon } from "@heroicons/react/24/solid";
import { XIcon } from "lucide-react";
import React, {
  forwardRef,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle } from "lucide-react";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import AnimatedButton from "./animatedbutton";

type FileUploadProps = {
  onUpload: (fileInfo: FileInfo) => void;
};

const FileUpload = forwardRef(({ onUpload }: FileUploadProps, ref) => {
  const { t } = useTranslation();
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [uploadStatus, setUploadStatus] =
    React.useState<Progress>("NOT_STARTED");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setError("");
    const uploadedFiles = Array.from(event.dataTransfer.files);
    handleFileValidation(uploadedFiles);
  };

  const handleFileValidation = (uploadedFiles: File[]) => {
    if (uploadedFiles.length > 1) {
      setError("Only one file can be uploaded at a time.");
      return;
    }
    const validFiles = uploadedFiles.filter((file) =>
      ["application/pdf", "application/xml", "text/xml"].includes(file.type)
    );

    if (validFiles.length === 0) {
      setError("Only PDF and XML files are allowed.");
    } else {
      setFile(validFiles[0]);
      setUploadStatus("NOT_STARTED");
    }
  };

  const handleFileSelection = (event: React.ChangeEvent<HTMLInputElement>) => {
    setError("");
    const selectedFiles = Array.from(event.target.files || []);
    handleFileValidation(selectedFiles);
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const clearFiles = () => {
    setFile(null);
    setError("");
    setUploadStatus("NOT_STARTED");
  };

  const uploadFile = async () => {
    console.log(
      import.meta.env.VITE_BACKEND_HOST,
      import.meta.env.VITE_BACKEND_PORT
    );

    console.log(backendUrl);
    if (!file) {
      setError("Please select a file to upload.");
      return;
    }

    setError("");
    setUploadStatus("IN_PROGRESS");

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`${backendUrl}/upload`, {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const jsonResponse = await response.json();
        const {
          fileUrl,
          invoiceId,
          fileFormat,
          xmlFormat,
          keyInformation,
          alreadyExists,
        } = jsonResponse;

        const fileInfo: FileInfo = {
          url: fileUrl,
          id: invoiceId,
          inputFormat: fileFormat,
          technicalStandard: xmlFormat,
          keyInformation,
          alreadyExists,
        };
        setUploadStatus("DONE");
        setFile(null);
        onUpload(fileInfo);
      } else {
        const errorMessage = await response.text();
        setError(`${errorMessage}`);
        setUploadStatus("NOT_STARTED");
      }
    } catch (err) {
      setError("An error occurred while uploading the file.");
      setUploadStatus("NOT_STARTED");
    }
  };

  // Expose the clearFiles method to the parent component
  useImperativeHandle(ref, () => ({
    clearFiles,
  }));

  return (
    <div className="h-full w-full flex flex-col items-center justify-center">
      <h1 className="text-3xl font-bold mb-4">{t("fileupload.header")}</h1>
      <p className="mb-6 text-gray-600 dark:text-gray-300">
        {t("fileupload.subheader")}
      </p>

      {/* Drag-and-Drop Area */}
      <Card
        className="relative border-dashed border-4 w-3/4 max-w-lg h-48 flex items-center justify-center"
        onDrop={handleDrop}
        onDragOver={(event) => event.preventDefault()}
        onClick={handleUploadClick}
      >
        <CardContent className="text-center">
          {file === null ? (
            <div>
              <DocumentCurrencyEuroIcon className="mx-auto mb-2 h-10 w-10 text-gray-400 dark:text-gray-500" />
              <p className="text-gray-500 dark:text-gray-400">
                {t("fileupload.drop-area-text-1")}
                <span className="font-semibold">
                  {" "}
                  {t("fileupload.drop-area-text-2")}
                </span>
              </p>
            </div>
          ) : (
            <div className="flex flex-col items-center">
              <p className="text-gray-700 dark:text-gray-300">{file.name}</p>
              <button
                onClick={clearFiles}
                className="bg-red-500 text-white hover:bg-red-600"
                aria-label="Clear file"
              >
                <XIcon className="w-5 h-5" />
              </button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Error Message */}
      {error && (
        <Alert variant="destructive" className="max-w-lg">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>{t("alert.error.file-upload-header")}</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Process Button */}
      <div className="mt-6">
        <AnimatedButton
          translationIdentifier={"fileupload.process-button"}
          progress={uploadStatus}
          onClick={uploadFile}
          disabled={file === null || error !== "" || uploadStatus !== "NOT_STARTED"}
        />
      </div>

      {/* Hidden File Input */}
      <input
        type="file"
        ref={fileInputRef}
        className="hidden"
        accept=".pdf,.xml"
        onChange={handleFileSelection}
      />
    </div>
  );
});

FileUpload.displayName = "FileUpload";

export default FileUpload;
