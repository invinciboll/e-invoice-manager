package com.invinciboll.service.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.database.InvoiceDao;
import com.invinciboll.entities.InvoiceEntity;
import com.invinciboll.entities.KeyInformation;
import com.invinciboll.entities.Invoice;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.exceptions.ParserException;
import com.invinciboll.exceptions.TransformationException;
import com.invinciboll.service.cache.TempInvoiceCache;
import com.invinciboll.service.xrechnung.XRechnungExtractor;
import com.invinciboll.service.xrechnung.XRechnungParser;
import com.invinciboll.service.xrechnung.XRechnungTransformer;
import com.invinciboll.service.xrechnung.XRechnungValidator;
import com.invinciboll.service.xrechnung.XRechnungVisualizer;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

@Service
public class InvoiceProcessingService {
    private final AppConfig appConfig;
    private final XRechnungExtractor extractor;
    private final XRechnungParser parser;
    private final XRechnungTransformer transformer;
    private final XRechnungValidator validator;
    private final XRechnungVisualizer visualizer;
    private final FileService fileService;

    @Autowired
    public InvoiceProcessingService(
            AppConfig appConfig,
            XRechnungExtractor extractor,
            XRechnungParser parser,
            XRechnungTransformer transformer,
            XRechnungValidator validator,
            TempInvoiceCache cache,
            XRechnungVisualizer visualizer,
            FileService fileService) {
        this.appConfig = appConfig;
        this.extractor = extractor;
        this.parser = parser;
        this.transformer = transformer;
        this.validator = validator;
        this.visualizer = visualizer;
        this.fileService = fileService;
    }

    public Invoice createNewInvoice(org.springframework.web.multipart.MultipartFile uploadedFile) throws IOException, IllegalStateException {
        String tempfiles = appConfig.getTempfilesDir();
        var tempFilesPath = Paths.get(System.getProperty("user.dir"), tempfiles);

        Invoice invoice = new Invoice(uploadedFile, tempFilesPath);
        fileService.transferFile(uploadedFile, invoice.getTempOriginalFilePath());
        invoice.setFileHash(fileService.computeFileHash(invoice.getTempOriginalFilePath()));

        return invoice;
    }

    public void processInvoice(Invoice invoice) throws IOException, ParserException, TransformationException, IllegalArgumentException {
        FileFormat fileFormat = extractor.detectFileFormat(invoice.getTempOriginalFilePath());
        invoice.setFileFormat(fileFormat);

        switch (fileFormat) {
            case PDF:
                processRegularInvoice(invoice);
                break;
            case XML:
            case ZF_PDF:
                processElectronicInvoice(invoice);
                break;
            case INVALID:
            default:
                throw new IllegalArgumentException("File can not be interpreted as valid PDF or XML, format is: " + fileFormat);
        }
    }

    private void processRegularInvoice(Invoice invoice) {
        invoice.setTempGeneratedFileName(invoice.getTempOriginalFilePath().getFileName().toString());
        invoice.setXmlFormat(XMLFormat.NONE);
        invoice.setKeyInformation(new KeyInformation(null, null, null, null, null));
    }

    private void processElectronicInvoice(Invoice invoice) throws ParserException, TransformationException {
        XdmNode xmlContent;
        XMLFormat xmlFormat;
        try {
            xmlContent = extractor.parseXmlContent(invoice.getTempOriginalFilePath(), invoice.getFileFormat());
            xmlFormat = extractor.detectXmlFormat(xmlContent);
            invoice.setXmlFormat(xmlFormat);
        } catch (IOException | ParserException | IllegalArgumentException e) {
            throw new ParserException("Error parsing XML content: " + e.getMessage(), e);
        }

        try {
            boolean isAcceptable= validator.validate(xmlContent, "Test");
            if (!isAcceptable) {
                return; // TODO: User feedback, Stop processing if not acceptable
            }
        } catch (Exception e) {
            throw new ParserException("XML Validation failed, see log for details." + e.getMessage(), e);
        }

        XdmNode xrContent;
        XdmNode foContent;
        try {
            xrContent = transformer.xmlToXr(xmlContent, xmlFormat);
            foContent = transformer.xrToFo(xrContent);
        } catch (SaxonApiException e) {
            throw new TransformationException("Error transforming to intermediate representation: " + e.getMessage(), e);
        }

        try {
            visualizer.renderPDF(foContent, invoice.getTempGeneratedFilePath().toString());
        } catch (IOException | SaxonApiException | FOPException e) {
            throw new TransformationException("Error rendering output PDF file: " + e.getMessage(),  e);
        }

        KeyInformation keyInformation = parser.extractKeyInformation(xrContent);
        invoice.setKeyInformation(keyInformation);
    }

    public Map<String, Object> prepareJSONResponse(InvoiceDao invoiceDao, Invoice invoice) {
        Map<String, Object> response = new HashMap<>();
        String fileUrl = "http://" + appConfig.getBackendHost() + ":" + appConfig.getBackendPort() + "/" + appConfig.getTempfilesDir() + "/" + invoice.getTempGeneratedFileName(); // TODO: Secure context switch
        response.put("fileUrl", fileUrl);
        response.put("invoiceId", invoice.getInvoiceId());
        response.put("fileFormat", invoice.getFileFormat().toString());
        response.put("xmlFormat", invoice.getXmlFormat().toString());
        response.put("keyInformation", invoice.getKeyInformation());
        response.put("alreadyExists", checkIfInvoiceExists(invoiceDao, invoice));
        return response;
    }

    private boolean checkIfInvoiceExists(InvoiceDao invoiceDao, Invoice invoice) {
        return invoiceDao.existsByFileHash(invoice.getFileHash());
    }

    public void persist(InvoiceDao invoiceDao, Invoice invoice) throws IOException{
        String outputDir = appConfig.getOutputDir();
        Path dirPath = Path.of(outputDir, invoice.getKeyInformation().sellerName());

        String generatedFileName = invoice.getKeyInformation().invoiceReference() + "_" + invoice.getFileHash() + ".pdf";
        String originalFileName = "original_" + invoice.getKeyInformation().invoiceReference() + "_" + invoice.getFileHash() + invoice.getOriginalFileExtension();

        Path generatedFileOutputPath = dirPath.resolve(generatedFileName);
        Path originalFileOutputPath = dirPath.resolve(originalFileName);

        if (invoice.getFileFormat() == FileFormat.PDF) { //TODO: IMPROVE THIS
            generatedFileOutputPath = originalFileOutputPath;
        }

        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Copy temp files to the output directory
            if (invoice.getFileFormat() == FileFormat.PDF) { //TODO: IMPROVE THIS
                Files.copy(invoice.getTempOriginalFilePath(), originalFileOutputPath);
            } else {
                Files.copy(invoice.getTempOriginalFilePath(), originalFileOutputPath);
                Files.copy(invoice.getTempGeneratedFilePath(), generatedFileOutputPath);
            }
        } catch (IOException e) {
            throw new IOException("Error copying temp files to output directory: " + e.getMessage(), e);
        }

        InvoiceEntity invoiceEntity = new InvoiceEntity(invoice, originalFileOutputPath, generatedFileOutputPath);
        invoiceDao.save(invoiceEntity);
    }
}
