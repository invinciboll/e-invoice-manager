package com.invinciboll;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.XdmNode;
import java.util.UUID;



public class Invoice {
    @Getter
    private FileFormat fileFormat;
    private String originalFileName;
    private Path inputPath;
    private Path outputPath;
    @Getter
    private Path tempPdfFilePath;
    @Getter
    private XdmNode xmlContent;
    private XdmNode xrContent;
    private XdmNode foContent;
    @Getter @Setter
    private XMLFormat xmlFormat;
    private KeyInformation keyInformation;
    @Getter
    private String invoiceId; 

    public Invoice(Path inputPath, String originalFileName) {
        this.invoiceId = UUID.randomUUID().toString();
        this.inputPath = inputPath;
        this.originalFileName = originalFileName;
        try {
            fileFormat = FormatDetector.detectFileFormat(inputPath);
        } catch (Exception e) {
            System.err.println("Error detecting file format: " + e.getMessage());
            e.printStackTrace();
        }

        if (fileFormat == FileFormat.PDF || fileFormat == FileFormat.INVALID) {
            System.out.println("Skipping file");
            return; //TODO: Implement
        }

        // TODO: Potentially add validation step

        try {
            xmlContent = XRechnungTransformer.parseXmlContent(inputPath, fileFormat);
        } catch (Exception e) {
            System.err.println("Error parsing XML content: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            xmlFormat = FormatDetector.detectXmlFormat(xmlContent);
        } catch (Exception e) {
            System.err.println("Error detecting XML format: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            xrContent = XRechnungTransformer.transformToXR(xmlContent, xmlFormat);
            System.out.println("Intermediate Representation");
        } catch (Exception e) {
            System.err.println("Error transforming to intermediate representation: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            keyInformation = XRechnungTransformer.extractKeyInformation(xrContent);
        } catch (Exception e) {
            System.err.println("Error extracting key information: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            foContent = XRechnungTransformer.transformToFO(xrContent);
        } catch (Exception e) {
            System.err.println("Error transforming to FO: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            String outputDir = AppConfig.getInstance().getProperty("tempfiles.dir");
            tempPdfFilePath = Paths.get(outputDir).resolve(UUID.randomUUID().toString() + ".pdf");
            XRechnungTransformer.renderPDF(foContent, tempPdfFilePath.toString());
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            // Creates the directory and any necessary parent directories
            String outputDir = AppConfig.getInstance().getProperty("output.dir");
            Path dirPath = Path.of(outputDir, keyInformation.sellerName());
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Copy temp files to the output directory
            String pdfFileName = keyInformation.billingRefNumber() + ".pdf";
            String invoiceFileName = "original_" + keyInformation.billingRefNumber() + originalFileName.substring(originalFileName.lastIndexOf("."));

            Files.copy(outputPath, dirPath.resolve(pdfFileName));
            Files.copy(inputPath, dirPath.resolve(invoiceFileName));

        } catch (Exception e) {
            System.err.println("Error getting output directory: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Invoice{" +
                // "inputPath=" + inputPath +
                "\n\tfileFormat=" + fileFormat +
                "\n\txmlFormat=" + xmlFormat +
                "\n\tbillingRefNumber=" + keyInformation.billingRefNumber() +
                "\n\tsellerName=" + keyInformation.sellerName() +
                "\n}";
    }
}
