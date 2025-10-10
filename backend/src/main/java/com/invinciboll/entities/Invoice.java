package com.invinciboll.entities;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.util.Utils;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.s9api.XdmNode;

public class Invoice {
    // Filenames get changed during processing, to ensure correct identification.
    // They always follow this pattern:
    // org_<UUID>.xml/.pdf      - The original input file (can be either pdf or xml)
    // gen_<UUID>.pdf           - The generated PDF from the Visualizer

    @Getter
    private UUID invoiceId;
    @Getter @Setter
    private String fileHash;
    @Getter
    private Path tempOriginalFilePath; // The path to the original file in the temporary directory
    @Getter
    private Path tempGeneratedFilePath; // The path to the generated file in the temporary directory
    @Getter @Setter
    private String tempGeneratedFileName;
    @Getter
    private String originalFileExtension;
    @Getter @Setter
    private FileFormat fileFormat;
    @Getter
    private XdmNode xmlContent;
    @Getter @Setter
    private XMLFormat xmlFormat;
    @Getter @Setter
    private KeyInformation keyInformation;

    public Invoice(MultipartFile uploadedFile, Path tempFilesPath) {
        this.invoiceId = UUID.randomUUID(); // Unique ID to identify each invoice, even after processing finsihed

        originalFileExtension = "."+ FilenameUtils.getExtension(uploadedFile.getOriginalFilename());

        String newFileName = "org_" + this.invoiceId.toString() + originalFileExtension;
        tempOriginalFilePath = tempFilesPath.resolve(newFileName);

        tempGeneratedFileName = "gen_" + this.invoiceId.toString() + ".pdf";
        tempGeneratedFilePath = tempFilesPath.resolve(tempGeneratedFileName);
    }


    public void setKeyInformationFromUserInput(Map<String, Object> userInput) {
        // Validate and parse invoice date
        LocalDate invoiceDate = null;
        if (userInput.containsKey("invoiceDate") && userInput.get("invoiceDate") instanceof String) {
            try {
                // Parse the date "yyyy-MM-dd"
                invoiceDate = LocalDate.parse((String) userInput.get("invoiceDate"));
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
                    totalSum = new BigDecimal(totalSumInput.toString());
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
            Utils.sanitizeReference((String) userInput.get("invoiceReference")),
            Utils.sanitizeSellerName((String) userInput.get("sellerName")),
            invoiceTypeCode,
            invoiceDate,
            totalSum
        );
    }
}
