package com.invinciboll.entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.FOPException;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.t;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.FormatDetector;
import com.invinciboll.KeyInformation;
import com.invinciboll.NetworkPrinter;
import com.invinciboll.XRechnungTransformer;
import com.invinciboll.configuration.AppConfig;
import com.invinciboll.database.InvoiceDao;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.exceptions.ParserException;
import com.invinciboll.exceptions.TransformationException;

import ch.qos.logback.core.helpers.Transform;
import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;



public class TempInvoice {

    @Getter
    private UUID invoiceId;

    @Getter
    private String fileHash;

    private Path tempFilesPath;
    @Getter
    private Path tempOriginalFilePath;
    @Getter
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

    private AppConfig appConfig;

    public TempInvoice(AppConfig appConfig){
        this.invoiceId = UUID.randomUUID();
        this.appConfig = appConfig;

        String tempfiles = appConfig.getTempfilesDir();
        tempFilesPath = Paths.get(System.getProperty("user.dir"), tempfiles);
        if (!Files.exists(tempFilesPath)) { //TODO: move this to app
            try {
                Files.createDirectories(tempFilesPath);
            } catch (IOException e) {
                throw new RuntimeException("Error creating tempfiles directory: " + e.getMessage(), e);
            }
        }
    }

    public void setFile(MultipartFile uploadedFile) throws IOException, IllegalStateException {
        originalFileExtension = "."+ FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
        String newFileName = "org_" + this.invoiceId.toString() + originalFileExtension;
        tempOriginalFilePath = tempFilesPath.resolve(newFileName);

        uploadedFile.transferTo(tempOriginalFilePath.toFile());
    }


    public void process() throws IOException, ParserException, TransformationException, IllegalArgumentException {
        fileFormat = FormatDetector.detectFileFormat(tempOriginalFilePath);
        fileHash = FormatDetector.computeFileHash(tempOriginalFilePath, "SHA-256");

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
                throw new IllegalArgumentException("File can not be interpreted as valid PDF or XML, format is: " + fileFormat);
        }
    }

    private void processRegularInvoice() {
        tempGenerateFileName = tempOriginalFilePath.getFileName().toString();
        xmlFormat = XMLFormat.NONE;
        keyInformation = new KeyInformation(null, null, null, null, null);
    }

    private void processElectronicInvoice() throws ParserException, TransformationException {
        try {
            xmlContent = XRechnungTransformer.parseXmlContent(tempOriginalFilePath, fileFormat);
            xmlFormat = FormatDetector.detectXmlFormat(xmlContent);
        } catch (IOException | ParserException | IllegalArgumentException e) {
            throw new ParserException("Error parsing XML content: " + e.getMessage(), e);
        }

        try {
            xrContent = XRechnungTransformer.transformToXR(xmlContent, xmlFormat);
            foContent = XRechnungTransformer.transformToFO(xrContent);
        } catch (SaxonApiException e) {
            throw new TransformationException("Error transforming to intermediate representation: " + e.getMessage(), e);
        }

        tempGenerateFileName = "gen_" + this.invoiceId.toString() + ".pdf";
        tempGeneratedFilePath = tempFilesPath.resolve(tempGenerateFileName);
        try {
            XRechnungTransformer.renderPDF(foContent, tempGeneratedFilePath.toString());
        } catch (IOException | SaxonApiException | FOPException e) {
            throw new TransformationException("Error rendering output PDF file: " + e.getMessage(),  e);
        }

        keyInformation = XRechnungTransformer.extractKeyInformation(xrContent);
    }

    private boolean checkIfInvoiceExists(InvoiceDao invoiceDao) {
        return invoiceDao.existsByFileHash(fileHash);
    }

    public Map<String, Object> prepareJSONResponse(InvoiceDao invoiceDao) {
        Map<String, Object> response = new HashMap<>();
        String fileUrl = "http://" + appConfig.getBackendHost() + ":" + appConfig.getBackendPort() + "/" + appConfig.getTempfilesDir() + "/" + tempGenerateFileName;
        response.put("fileUrl", fileUrl);
        response.put("invoiceId", invoiceId);
        response.put("fileFormat", fileFormat.toString());
        response.put("xmlFormat", xmlFormat.toString());
        response.put("keyInformation", keyInformation);
        response.put("alreadyExists", checkIfInvoiceExists(invoiceDao));

        return response;
    }

    public void setKeyInformationFromUserInput(Map<String, Object> userInput) {

        // Validate and parse invoice date
        LocalDate invoiceDate = null;
        if (userInput.containsKey("invoiceDate") && userInput.get("invoiceDate") instanceof String) {
            try {
                // Parse the full ISO 8601 date-time string and extract the LocalDate
                String dateString = (String) userInput.get("invoiceDate");
                invoiceDate = OffsetDateTime.parse(dateString).toLocalDate();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for 'invoiceDate'");
            }
        }

        // Validate and parse total sum
        BigDecimal totalSum = null;
        if (userInput.containsKey("totalSum")) {
            try {
                Object totalSumInput = userInput.get("totalSum");
                if (totalSumInput instanceof String) {
                    String sanitizedValue = ((String) totalSumInput).replace(",", ".");
                    totalSum = new BigDecimal(sanitizedValue);
                } else if (totalSumInput instanceof Number) {
                    totalSum = BigDecimal.valueOf(((Number) totalSumInput).doubleValue());
                } else {
                    throw new IllegalArgumentException("Invalid data type for 'totalSum'");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid format for 'totalSum'");
            }
        }

        // Validate and parse invoice type
        Integer invoiceTypeCode = null;
        if (userInput.containsKey("invoiceType")) {
            try {
                Object invoiceTypeInput = userInput.get("invoiceType");
                if (invoiceTypeInput instanceof String) {
                    invoiceTypeCode = Integer.parseInt((String) invoiceTypeInput);
                } else if (invoiceTypeInput instanceof Number) {
                    invoiceTypeCode = ((Number) invoiceTypeInput).intValue();
                } else {
                    throw new IllegalArgumentException("Invalid data type for 'invoiceType'");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid format for 'invoiceType'");
            }
        }

        // Construct KeyInformation object
        keyInformation = new KeyInformation(
            (String) userInput.get("invoiceReference"),
            (String) userInput.get("sellerName"),
            invoiceTypeCode,
            invoiceDate,
            totalSum
        );
    }



    public void persist(InvoiceDao invoiceDao) throws IOException{
        String outputDir = appConfig.getOutputDir();
        Path dirPath = Path.of(outputDir, keyInformation.sellerName());

        String generatedFileName = keyInformation.invoiceReference() + "_" + fileHash + ".pdf";
        String originalFileName = "original_" + keyInformation.invoiceReference() + "_" + fileHash + originalFileExtension;

        Path generatedFileOutputPath = dirPath.resolve(generatedFileName);
        Path originalFileOutputPath = dirPath.resolve(originalFileName);

        if (fileFormat == FileFormat.PDF) { //TODO: IMPROVE THIS
            generatedFileOutputPath = originalFileOutputPath;
        }

        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Copy temp files to the output directory
            if (fileFormat == FileFormat.PDF) { //TODO: IMPROVE THIS
                Files.copy(tempOriginalFilePath, originalFileOutputPath);
            } else {
                Files.copy(tempOriginalFilePath, originalFileOutputPath);
                Files.copy(tempGeneratedFilePath, generatedFileOutputPath);
            }
        } catch (IOException e) {
            throw new IOException("Error copying temp files to output directory: " + e.getMessage(), e);
        }

        InvoiceEntity invoiceEntity = new InvoiceEntity(this, originalFileOutputPath, generatedFileOutputPath);
        invoiceDao.save(invoiceEntity);
    }

    public void print() throws IOException {
        NetworkPrinter.print(appConfig.getPrinterIp(), appConfig.getPrinterPort(), tempGeneratedFilePath.toString());
    }

}

