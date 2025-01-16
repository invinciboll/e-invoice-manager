import { FileInfo } from "@/types";
import React from "react";

type PdfViewerProps = {
  fileInfo: FileInfo;
  iframeRef: React.RefObject<HTMLIFrameElement>;
};

const PdfViewer: React.FC<PdfViewerProps> = ({ fileInfo, iframeRef }) => {
  return (
    <div className="w-full h-full flex flex-col items-center justify-center">
      <h2 className="text-2xl font-bold mb-4">PDF Preview</h2>
      <iframe
        ref={iframeRef}
        src={fileInfo.url}
        title="PDF Viewer"
        style={{ width: "100%", height: "100vh", border: "1px solid gray" }}
      ></iframe>
    </div>
  );
};

export default PdfViewer;
