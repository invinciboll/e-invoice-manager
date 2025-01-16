import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { FileInfo } from "@/types";
import { ReceiptPoundSterlingIcon } from "lucide-react";
import React, { useRef, useState } from "react";

type FileUploadProps = {
  onUpload: (fileInfo: FileInfo) => void; // Callback to pass pdfUrl and fileId
};

const FileUpload: React.FC<FileUploadProps> = ({ onUpload }) => {
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
        const jsonResponse = await response.json(); // Expecting JSON with `pdfUrl` and `fileId`
        const { fileUrl, invoiceId } = jsonResponse;
        
        const fileInfo: FileInfo = {
            url: fileUrl,
            id: invoiceId,
            inputFormat: "XRechnung",
            technicalStandard: "UBL"
        }
        setUploadStatus("File uploaded and processed successfully!");
        setFile(null); // Clear the file after upload

        // Pass the pdfUrl and fileId back to the parent component
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
    <div className="h-full w-full flex flex-col items-center justify-center bg-gray-100">
      <h1 className="text-3xl font-bold mb-4">Import File</h1>
      <p className="mb-6 text-gray-600">Drag and drop your PDF or XML file below, or use the buttons to upload.</p>

      {/* Drag-and-Drop Area */}
      <Card
        className="border-dashed border-2 border-gray-400 w-3/4 max-w-lg h-48 flex items-center justify-center bg-white"
        onDrop={handleDrop}
        onDragOver={(event) => event.preventDefault()}
      >
        <CardContent>
          {file === null ? (
            <p className="text-gray-500">Drag and drop a file here, or click to upload</p>
          ) : (
            <ul>
              <li className="text-gray-700">{file.name}</li>
            </ul>
          )}
        </CardContent>
      </Card>

      {/* Error Message */}
      {error && <p className="mt-4 text-red-500">{error}</p>}

      {/* Upload Status */}
      {uploadStatus && <p className="mt-4 text-blue-500">{uploadStatus}</p>}

      {/* Buttons */}
      <div className="mt-6 flex space-x-4">
        <Button onClick={handleUploadClick} variant="default" className="bg-blue-500 text-white hover:bg-blue-600">
          Upload
        </Button>
        <Button variant="secondary" onClick={clearFiles}>
          Clear
        </Button>
        <Button
          onClick={uploadFile}
          variant="default"
          className="bg-green-500 text-white hover:bg-green-600"
        >
          Process
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
