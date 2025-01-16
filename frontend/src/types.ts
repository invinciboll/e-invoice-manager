
export type InputFileFormat = "ZUGfeRD" | "XRechnung" | "PDF" | "Invalid";
export type TechnicalStandard = "CII" | "UBL"

export type FileInfo = {
    url: string,
    id: string,
    inputFormat: InputFileFormat, 
    technicalStandard: TechnicalStandard
}
  