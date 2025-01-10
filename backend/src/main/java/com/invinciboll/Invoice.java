package com.invinciboll;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.XdmNode;



public class Invoice {
    @Getter @Setter
    private FileFormat fileFormat;
    @Getter
    private XdmNode xmlContent;
    private XdmNode xrContent;
    private XdmNode foContent;
    @Getter @Setter
    private XMLFormat xmlFormat;
    private KeyInformation keyInformation;

    public Invoice(Path inputPath) {
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
            // Create dir for sellerName if not exists
            String outputDir = AppConfig.getInstance().getProperty("output.dir");
            Path dirPath = Path.of(outputDir, keyInformation.sellerName());

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath); // Creates the directory and any necessary parent directories
            }

            Path outputPath = dirPath.resolve(keyInformation.billingRefNumber() + ".pdf");
            XRechnungTransformer.renderPDF(foContent, outputPath.toString());
            System.out.println("PDF generated successfully");
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
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
