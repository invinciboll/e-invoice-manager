package com.invinciboll.entities;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import com.invinciboll.database.InvoiceDao;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.XdmNode;



public class TempInvoice {

    @Getter
    private UUID invoiceId;
    
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
        tempGenerateFileName = tempOriginalFilePath.getFileName().toString();
        xmlFormat = XMLFormat.NONE;
        keyInformation = new KeyInformation(null, null, null, null, null);
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

    private boolean checkIfInvoiceExists(InvoiceDao invoiceDao) {
        return invoiceDao.existsBySellerNameAndInvoiceReference(keyInformation.sellerName(), keyInformation.invoiceReference(), keyInformation.invoiceTypeCode());
    }

    public Map<String, Object> prepareJSONResponse(InvoiceDao invoiceDao) {
        Map<String, Object> response = new HashMap<>();
        response.put("fileUrl", "http://localhost:4711/" + "tempfiles/" + tempGenerateFileName);
        response.put("invoiceId", invoiceId);
        response.put("fileFormat", fileFormat.toString());
        response.put("xmlFormat", xmlFormat.toString());
        response.put("keyInformation", keyInformation);
        response.put("alreadyExists", checkIfInvoiceExists(invoiceDao));

        return response;
    }

    public void setKeyInformationFromUserInput(Map<String, Object> userInput) {
        try {
            // Validate and parse invoice date
            LocalDate invoiceDate = null;
            if (userInput.containsKey("invoiceDate") && userInput.get("invoiceDate") instanceof String) {
                try {
                    // Parse the full ISO 8601 date-time string and extract the LocalDate
                    String dateString = (String) userInput.get("invoiceDate");
                    invoiceDate = OffsetDateTime.parse(dateString).toLocalDate();
                } catch (Exception e) {
                    System.err.println("Error parsing invoice date: " + e.getMessage());
                    e.printStackTrace();
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
                    System.err.println("Error parsing total sum: " + e.getMessage());
                    e.printStackTrace();
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
                    System.err.println("Error parsing invoice type: " + e.getMessage());
                    e.printStackTrace();
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

        } catch (IllegalArgumentException e) {
            throw e; // Let the caller handle validation errors
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process user input");
        }
    }

    

    public void persist(InvoiceDao invoiceDao) {
        // Copy to output dir
        String outputDir = AppConfig.getInstance().getProperty("output.dir");
        Path dirPath = Path.of(outputDir, keyInformation.sellerName());
        
        String generatedFileName = keyInformation.invoiceReference() + "_" + keyInformation.invoiceTypeCode() + ".pdf";
        String originalFileName = "original_" + keyInformation.invoiceReference() + "_" + keyInformation.invoiceTypeCode() + originalFileExtension;

        Path generatedFileOutputPath = dirPath.resolve(generatedFileName);
        Path originalFileOutputPath = dirPath.resolve(originalFileName);

        if (fileFormat == FileFormat.PDF) { //TODO: IMPROVE THIS
            generatedFileOutputPath = originalFileOutputPath;
        }

        try {
            // Creates the directory and any necessary parent directories
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            // Copy temp files to the output directory
            if (fileFormat == FileFormat.PDF) { //todo: iMPROVE THIS
                Files.copy(tempOriginalFilePath, originalFileOutputPath);
            } else {
                Files.copy(tempOriginalFilePath, originalFileOutputPath);
                Files.copy(tempGeneratedFilePath, generatedFileOutputPath);
            }
        } catch (Exception e) {
            System.err.println("Error getting output directory: " + e.getMessage());
            e.printStackTrace();
        }

        // Convert Te   mpInvoice to InvoiceEntity
        InvoiceEntity invoiceEntity = new InvoiceEntity(this, originalFileOutputPath, generatedFileOutputPath);

        // Persist to database using DAO
        try {
            invoiceDao.save(invoiceEntity);
            System.out.println("Invoice persisted successfully: " + invoiceEntity.getInvoiceId());
        } catch (Exception e) {
            System.err.println("Error saving invoice to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void print() {
        System.out.println("Printing invoice: " + keyInformation.invoiceReference());
        NetworkPrinter.print(tempGeneratedFilePath.toString());
        // Print generated file with network printer (it is outside the docker container)
    }

} 

