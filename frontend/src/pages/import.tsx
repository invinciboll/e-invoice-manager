import React, { useRef, useState } from "react";
import FileUpload from "@/components/custom/fileupload";
import PdfViewer from "@/components/custom/pdfviewer";
import Overview from "@/components/custom/overview";
import { FileInfo } from "@/types";

const Import = () => {
  const [fileInfo, setFileInfo] = useState<FileInfo | null>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);
  
  const handleFileUpload = (info: FileInfo) => {
    setFileInfo(info);
  };
  return (
    <div className="w-full h-screen flex items-center justify-center bg-gray-100">
      {!fileInfo ? (
        <FileUpload onUpload={handleFileUpload} />
      ) : (
        <div className="w-full flex justify-center">
          <div className="w-[70%] p-4">
            <PdfViewer fileInfo={fileInfo} iframeRef={iframeRef} />
          </div>
          <div className="w-[30%] p-4 ml-auto">
            <Overview fileInfo={fileInfo} iframeRef={iframeRef}  />
          </div>
        </div>
      )}
    </div>
  );
};

export default Import;
