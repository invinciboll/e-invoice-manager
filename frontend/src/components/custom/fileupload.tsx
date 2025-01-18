import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { FileInfo } from "@/types";
import { ReceiptPoundSterlingIcon, XIcon } from "lucide-react";
import React, { useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { DocumentCurrencyEuroIcon} from "@heroicons/react/24/solid";

type FileUploadProps = {
  onUpload: (fileInfo: FileInfo) => void;
};

const FileUpload: React.FC<FileUploadProps> = ({ onUpload }) => {
  const { t } = useTranslation();
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [uploadStatus, setUploadStatus] = useState("");
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
    setUploadStatus("");
  };

  const uploadFile = async () => {
    if (!file) {
      setError("Please select a file to upload.");
      return;
    }

    setError("");
    setUploadStatus("Uploading...");

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("http://localhost:4711/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const jsonResponse = await response.json();
        const { fileUrl, invoiceId, fileFormat, xmlFormat, keyInformation, alreadyExists } = jsonResponse;

        const fileInfo: FileInfo = {
          url: fileUrl,
          id: invoiceId,
          inputFormat: fileFormat,
          technicalStandard: xmlFormat,
          keyInformation,
          alreadyExists,
        };
        setUploadStatus("File uploaded and processed successfully!");
        setFile(null);
        onUpload(fileInfo);
      } else {
        const errorMessage = await response.text();
        setError(`Upload failed: ${errorMessage}`);
      }
    } catch (err) {
      setError("An error occurred while uploading the file.");
    }
  };

  return (
    <div className="h-full w-full flex flex-col items-center justify-center">
      <h1 className="text-3xl font-bold mb-4">{t("fileupload.header")}</h1>
      <p className="mb-6 text-gray-600 dark:text-gray-300">
        {t("fileupload.subheader")}
      </p>

      {/* Drag-and-Drop Area */}
      <Card
        className="relative border-dashed border-2 border-gray-400 dark:border-gray-600 w-3/4 max-w-lg h-48 flex items-center justify-center bg-gray-50 dark:bg-gray-800"
        onDrop={handleDrop}
        onDragOver={(event) => event.preventDefault()}
        onClick={handleUploadClick}
      >
        <CardContent className="text-center">
          {file === null ? (
            <div>
              <DocumentCurrencyEuroIcon className="mx-auto mb-2 h-10 w-10 text-gray-400 dark:text-gray-500" />
              <p className="text-gray-500 dark:text-gray-400">
                {t("fileupload.drop-area-text-1")}<span className="font-semibold"> {t("fileupload.drop-area-text-2")}</span>
              </p>
            </div>
          ) : (
            <div className="flex flex-col items-center">
              <p className="text-gray-700 dark:text-gray-300">{file.name}</p>
              <button
                onClick={clearFiles}
                className="mt-2 text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300"
                aria-label="Clear file"
              >
                <XIcon className="w-5 h-5" />
              </button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Error Message */}
      {error && <p className="mt-4 text-red-500">{error}</p>}

      {/* Upload Status */}
      {uploadStatus && <p className="mt-4 text-blue-500 dark:text-blue-400">{uploadStatus}</p>}

      {/* Process Button */}
      <div className="mt-6">
        <Button
          onClick={uploadFile}
          variant="default"
          disabled={!file}
          className={`${
            file ? "bg-yellow-400 text-black hover:bg-yellow-500" : "bg-gray-300 text-gray-500 cursor-not-allowed"
          }`}
        >
          {t("fileupload.process-button")}
        </Button>
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
};

export default FileUpload;
