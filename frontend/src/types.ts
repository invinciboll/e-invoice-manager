
export type InputFileFormat = "ZF_PDF" | "XML" | "PDF" | "Invalid";
export type TechnicalStandard = "CII" | "UBL"

export type Progress = "NOT_STARTED" | "IN_PROGRESS" | "DONE"

export type KeyInformation = {
    invoiceReference: string,
    sellerName: string,
    invoiceTypeCode: number,
    issuedDate: string,
    totalSum: number
}

export type FileInfo = {
    url: string,
    id: string,
    inputFormat: InputFileFormat, 
    technicalStandard: TechnicalStandard,
    keyInformation: KeyInformation,
    alreadyExists: boolean
}
  

export interface Invoice {
    sellerName: string;
    invoiceReference: string;
    issuedDate: string; // Dates are usually strings when coming from APIs
    totalSum: number; // Use number for precise calculations
    fileFormat: string; // Assuming this is a string (Enum stored as string)
}
  