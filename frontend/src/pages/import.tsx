import React, { useRef, useState, useEffect } from "react";
import FileUpload from "@/components/custom/fileupload";
import PdfViewer from "@/components/custom/pdfviewer";
import Overview from "@/components/custom/overview";
import { FileInfo } from "@/types";

const Import = () => {
  const [fileInfo, setFileInfo] = useState<FileInfo | null>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const contentRef = useRef<HTMLDivElement>(null); // Ref for the content to scroll to
  const dividerRef = useRef<HTMLDivElement>(null); // Ref for the divider to animate

  const navbarHeight = 64; // Adjust this value to match your navbar's height in pixels

  useEffect(() => {
    if (fileInfo && contentRef.current) {
      const element = contentRef.current;
      const offset = element.getBoundingClientRect().top + window.scrollY - navbarHeight;

      // Smooth scroll to adjusted position
      window.scrollTo({ top: offset, behavior: "smooth" });

      // Fade in the divider line
      if (dividerRef.current) {
        dividerRef.current.classList.remove("opacity-0");
        dividerRef.current.classList.add("opacity-100");
      }
    }
  }, [fileInfo, navbarHeight]);

  const handleFileUpload = (info: FileInfo) => {
    setFileInfo(info);
  };

  return (
    <div className="w-full min-h-screen flex flex-col">
      {/* File Upload Section */}
      <div className="w-full flex flex-col items-center justify-center h-screen bg-gray-50 dark:bg-gray-800">
        <FileUpload onUpload={handleFileUpload} />
      </div>

      {/* Content Section */}
      {fileInfo && (
        <div
          ref={contentRef} // Attach the ref to the content section
          className="w-full max-w-7xl mx-auto flex flex-col lg:flex-row justify-center pt-16 px-4 lg:px-8 min-h-screen"
        >
          <div className="w-full lg:w-[70%]">
            <PdfViewer fileInfo={fileInfo} iframeRef={iframeRef} />
          </div>
          <div className="w-full lg:w-[30%] mt-8 lg:mt-0 lg:ml-8">
            <Overview fileInfo={fileInfo} iframeRef={iframeRef} />
          </div>
        </div>
      )}
    </div>
  );
};

export default Import;
