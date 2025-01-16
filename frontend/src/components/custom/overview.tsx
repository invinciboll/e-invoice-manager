import { FileInfo, InputFileFormat, TechnicalStandard } from "@/types";
import { Button } from "../ui/button";

type OverviewProps = {
    fileInfo: FileInfo;
    iframeRef: React.RefObject<HTMLIFrameElement>;
};


const Overview : React.FC<OverviewProps> = ({ fileInfo, iframeRef }) => {
    
    function isElectronicInvoice(): boolean {
        return fileInfo.inputFormat === "ZUGfeRD" || fileInfo.inputFormat === "XRechnung";
    }

    const handlePrint = () => {
        if (iframeRef?.current) {
          iframeRef.current.contentWindow?.focus(); // Focus the iframe
          iframeRef.current.contentWindow?.print(); // Trigger the print dialog
        } else {
          console.error('No iframe reference available');
        }
    };
      

    return (
        <div className="w-full flex flex-col items-center text-center space-y-6">
          <h2 className="text-2xl font-bold">Overview</h2>
          <table className="table-auto border-collapse border border-gray-300">
            <tbody>
              <tr className="border-b border-gray-300">
                <td className="p-2 font-semibold text-gray-700">E-Invoice</td>
                <td className="p-2 text-gray-600 flex items-center justify-center">
                  {isElectronicInvoice() ? (
                    <span className="text-green-500 font-bold">&#10003;</span> // Green Checkmark
                  ) : (
                    <span className="text-red-500 font-bold">&#10007;</span> // Red Cross
                  )}
                </td>
              </tr>
              <tr className="border-b border-gray-300">
                <td className="p-2 font-semibold text-gray-700">Format</td>
                <td className="p-2 text-gray-600">{fileInfo.inputFormat} ({fileInfo.technicalStandard})</td>
              </tr>
              <tr>
                <td className="p-2 font-semibold text-gray-700">Abc</td>
                <td className="p-2 text-gray-600">johndoe@example.com</td>
              </tr>
            </tbody>
          </table>
          <div className="flex space-x-4">
            <Button onClick={handlePrint} variant="default" className="bg-blue-500 text-white hover:bg-blue-600">
              Print
            </Button>
            <Button variant="default" className="bg-green-500 text-white hover:bg-green-600">
              Save
            </Button>
          </div>
        </div>
      );      
}

export default Overview;