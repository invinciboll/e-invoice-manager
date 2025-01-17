
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
  