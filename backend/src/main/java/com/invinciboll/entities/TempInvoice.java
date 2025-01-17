package com.invinciboll.entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.FormatDetector;
import com.invinciboll.KeyInformation;
import com.invinciboll.NetworkPrinter;
import com.invinciboll.XRechnungTransformer;
import com.invinciboll.configuration.AppConfig;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.XdmNode;



public class TempInvoice {

    @Getter
    private UUID invoiceId;
    
    private Path tempFilesPath;
    private Path tempOriginalFilePath;
    private Path tempGeneratedFilePath;
    private String tempGenerateFileName;

    private String originalFileExtension;
    @Getter
    private FileFormat fileFormat;

    @Getter
    private XdmNode xmlContent;
    private XdmNode xrContent;
    private XdmNode foContent;
    @Getter @Setter
    private XMLFormat xmlFormat;
    @Getter
    private KeyInformation keyInformation;


    public TempInvoice(){
        this.invoiceId = UUID.randomUUID();

        String tempfiles = AppConfig.getInstance().getProperty("tempfiles.dir", "tempfiles"); //TODO: Static
        tempFilesPath = Paths.get(System.getProperty("user.dir"), tempfiles);
        if (!Files.exists(tempFilesPath)) {
            try {
                Files.createDirectories(tempFilesPath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error creating tempfiles directory");
            }
        }
    }

    public void setFile(MultipartFile uploadedFile) throws RuntimeException{
        originalFileExtension = "."+ FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
        String newFileName = "org_" + this.invoiceId.toString() + originalFileExtension;
        tempOriginalFilePath = tempFilesPath.resolve(newFileName);
        try {
            uploadedFile.transferTo(tempOriginalFilePath.toFile());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving file");
        }

    }


    public void process() throws Exception {
        try {
            fileFormat = FormatDetector.detectFileFormat(tempOriginalFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Error detecting file format: " + e.getMessage());
        }

        switch (fileFormat) {
            case PDF:
                processRegularInvoice();
                break;
            case XML:
            case ZF_PDF:
                processElectronicInvoice();
                break;
            case INVALID:
            default:
                throw new RuntimeException("Invalid file format");
        }
    }

    private void processRegularInvoice() {

    }

    private void processElectronicInvoice() {
        try {
            // Extract XML content
            xmlContent = XRechnungTransformer.parseXmlContent(tempOriginalFilePath, fileFormat);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing XML content: " + e.getMessage());
        }

        try {
            // Determine technical XML standard (ubl, cii), needed for transformation
            xmlFormat = FormatDetector.detectXmlFormat(xmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Error detecting XML format: " + e.getMessage());
        }

        try {
            xrContent = XRechnungTransformer.transformToXR(xmlContent, xmlFormat);
        } catch (Exception e) {
            throw new RuntimeException("Error transforming to intermediate representation: " + e.getMessage());
        }

        try {
            foContent = XRechnungTransformer.transformToFO(xrContent);
        } catch (Exception e) {
            throw new RuntimeException("Error transforming to FO: " + e.getMessage());
        }

        try {
            tempGenerateFileName = "gen_" + this.invoiceId.toString() + ".pdf";
            tempGeneratedFilePath = tempFilesPath.resolve(tempGenerateFileName);
            XRechnungTransformer.renderPDF(foContent, tempGeneratedFilePath.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }

        try {
            keyInformation = XRechnungTransformer.extractKeyInformation(xrContent);
        } catch (Exception e) {
            System.err.println("Error extracting key information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> prepareJSONResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("fileUrl", "http://localhost:4711/" + "tempfiles/" + tempGenerateFileName);
        response.put("invoiceId", invoiceId);
        response.put("fileFormat", fileFormat.toString());
        response.put("xmlFormat", xmlFormat.toString());
        response.put("keyInformation", keyInformation);

        return response;
    }

    public void persist() {
        // Copy to output dir
        try {
            // Creates the directory and any necessary parent directories
            String outputDir = AppConfig.getInstance().getProperty("output.dir");
            Path dirPath = Path.of(outputDir, keyInformation.sellerName());
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Copy temp files to the output directory
            String generatedFileName = keyInformation.invoiceReference() + ".pdf";
            String originalFileName = "original_" + keyInformation.invoiceReference() + originalFileExtension;

            Files.copy(tempGeneratedFilePath, dirPath.resolve(generatedFileName));
            Files.copy(tempOriginalFilePath, dirPath.resolve(originalFileName));

        } catch (Exception e) {
            System.err.println("Error getting output directory: " + e.getMessage());
            e.printStackTrace();
        }

        // TODO: Persist in database as InvoiceEntity
    }

    public void print() {
        System.out.println("Printing invoice: " + keyInformation.invoiceReference());
        NetworkPrinter.print(tempGeneratedFilePath.toString());
        // Print generated file with network printer (it is outside the docker container)
    }

} 

