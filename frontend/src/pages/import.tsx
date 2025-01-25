import FileUpload from "@/components/custom/file-upload";
import Overview from "@/components/custom/overview";
import OverviewNEI from "@/components/custom/overviewNonElectonicInvoice";
import PdfViewer from "@/components/custom/pdf-viewer";
import { FileInfo } from "@/types";
import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";

const Import = () => {
    const [fileInfo, setFileInfo] = useState<FileInfo | null>(null);
    const iframeRef = useRef<HTMLIFrameElement>(null);
    const contentRef = useRef<HTMLDivElement>(null); // Ref for the content to scroll to

    const location = useLocation();

    useEffect(() => {
        if (location.state?.reset) {
            handleResetPage();
        }
    }, [location.state]);

    const fileUploadRef = useRef<{ clearFiles: () => void }>(null);
    const handleResetPage = () => {
        fileUploadRef.current?.clearFiles();
        setFileInfo(null);
    };

    const navbarHeight = 64; // Adjust this value to match your navbar's height in pixels

    useEffect(() => {
        if (fileInfo && contentRef.current) {
            const element = contentRef.current;
            const offset =
                element.getBoundingClientRect().top + window.scrollY - navbarHeight;

            // Smooth scroll to adjusted position
            window.scrollTo({ top: offset, behavior: "smooth" });
        }
    }, [fileInfo, navbarHeight]);

    const handleFileUpload = (info: FileInfo) => {
        setFileInfo(info);
    };

    return (
        <div className="w-full min-h-screen flex flex-col">
            {/* File Upload Section */}
            <div className="w-full flex flex-col items-center justify-center h-screen">
                <FileUpload ref={fileUploadRef} onUpload={handleFileUpload} />
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
                    <div className="w-full lg:w-[30%] mt-8 lg:mt-0 lg:ml-8 flex flex-col items-center">
                        {fileInfo.inputFormat === "ZF_PDF" ||
                            fileInfo.inputFormat === "XML" ? (
                            <Overview
                                fileInfo={fileInfo}
                                iframeRef={iframeRef}
                                handleResetPage={handleResetPage}
                            />
                        ) : (
                            <OverviewNEI
                                fileInfo={fileInfo}
                                handleResetPage={handleResetPage}
                                iframeRef={iframeRef}
                            />
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default Import;
