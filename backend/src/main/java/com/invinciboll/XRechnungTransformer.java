package com.invinciboll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.mustangproject.ZUGFeRD.ZUGFeRDInvoiceImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.exceptions.ParserException;
import com.invinciboll.util.Util;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

@Component
public class XRechnungTransformer {
    private static final Processor processor = new Processor(false); // Saxon Processor (no schema validation)
    private static final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI()); // FOP Factory

    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        XRechnungTransformer.appConfig = appConfig;
    }


    public static XdmNode parseXmlContent(Path inputPath, FileFormat fileFormat) throws IllegalArgumentException, IOException, ParserException {
        if (fileFormat != FileFormat.ZF_PDF && fileFormat != FileFormat.XML) {
            throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
        }

        String xmlContentString;

        if (fileFormat == FileFormat.ZF_PDF) {
            ZUGFeRDInvoiceImporter zii = new ZUGFeRDInvoiceImporter(inputPath.toString());
            xmlContentString = zii.getUTF8(); // Extract XML content from ZF_PDF
        } else  {
            try{
                xmlContentString = Files.readString(inputPath, StandardCharsets.UTF_8); // Read XML from file
            } catch (IOException e) {
                throw new IOException("Unable to read XML content from XML file", e);
            }
        }

        DocumentBuilder builder = processor.newDocumentBuilder();
        try {
            // Parse the XML content string into an XdmNode
            return builder.build(new StreamSource(new StringReader(xmlContentString)));
        } catch (SaxonApiException e) {
            throw new ParserException("Unable to parse XML content", e);
        }
    }

    public static XdmNode transformToXR(XdmNode inputXmlDoc, XMLFormat xmlFormat) throws SaxonApiException {
        String xslToXR;

        // Determine the appropriate XSLT based on XML format
        switch (xmlFormat) {
            case UBL_INVOICE:
                xslToXR = appConfig.getUblInvoiceToXR();
                break;
            case UBL_CREDIT_NOTE:
                xslToXR = appConfig.getUblCreditNoteToXR();
                break;
            case CII:
                xslToXR = appConfig.getCiiToXR();
                break;
            default:
                throw new IllegalStateException("Method should not be invoked for format: " + xmlFormat);
        }

        // Compile the XSLT
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable = compiler.compile(new StreamSource(xslToXR));

        // Set up the transformer
        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(inputXmlDoc); // Set the input XML document as context

        // Set up the destination for the transformed result
        XdmDestination destination = new XdmDestination();
        transformer.setDestination(destination);
        transformer.transform();

        // Return the resulting XdmNode
        return destination.getXdmNode();
    }

    public static KeyInformation extractKeyInformation(XdmNode xrContent) throws ParserException {
        XPathCompiler xpathCompiler = processor.newXPathCompiler();

        // Declare namespaces
        xpathCompiler.declareNamespace("xr", "urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung-1");

        // XPath expressions
        String sellerNameXPath = "//xr:Seller_name";
        String invoiceReferenceXPath = "//xr:invoice/xr:Invoice_number";
        String invoiceTypeCodeXPath = "//xr:invoice/xr:Invoice_type_code";
        String issuedDateXPath = "//xr:invoice/xr:Invoice_issue_date";
        String totalSumXPath = "//xr:Invoice_total_amount_with_VAT";

        String sellerName = "Not Found";
        String invoiceReference = "Not Found";
        Integer invoiceTypeCode = -1; // Use `null` to indicate no value
        LocalDate issuedDate = LocalDate.MIN; // Represents the smallest possible LocalDate
        BigDecimal totalSum = BigDecimal.valueOf(-1); // Placeholder value indicating invalid

        try {
            sellerName = extractStringValue(xpathCompiler, xrContent, sellerNameXPath);
            invoiceReference = extractStringValue(xpathCompiler, xrContent, invoiceReferenceXPath);
            invoiceTypeCode = extractIntegerValue(xpathCompiler, xrContent, invoiceTypeCodeXPath);
            issuedDate = extractLocalDateValue(xpathCompiler, xrContent, issuedDateXPath);
            totalSum = extractBigDecimalValue(xpathCompiler, xrContent, totalSumXPath);
        } catch (ParserException e) {
            // Log the error for debugging
            System.err.println("Error extracting key information: " + e.getMessage());
            e.printStackTrace();

            // Optionally rethrow with additional context
            throw new ParserException("Error in extractKeyInformation: Unable to parse key details", e);
        }

        // Return successfully extracted key information
        KeyInformation keyInformation = new KeyInformation(invoiceReference, Util.sanitizeSellerName(sellerName), invoiceTypeCode, issuedDate, totalSum);
        return keyInformation;
    }


    public static String extractStringValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            XdmValue result = xpathCompiler.evaluate(expression, xrContent);
            return result.size() > 0 ? result.itemAt(0).getStringValue() : null;
        } catch (SaxonApiException e) {
            throw new ParserException("Error extracting string value for expression: " + expression, e);
        }
    }

    public static Integer extractIntegerValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : Integer.MIN_VALUE;
        } catch (NumberFormatException e) {
            throw new ParserException("Invalid integer format for expression: " + expression, e);
        }
    }

    public static LocalDate extractLocalDateValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? LocalDate.parse(value) : LocalDate.MIN;
        } catch (DateTimeParseException e) {
            throw new ParserException("Invalid date format for expression: " + expression, e);
        }
    }

    public static BigDecimal extractBigDecimalValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? new BigDecimal(value) : BigDecimal.valueOf(-1);
        } catch (NumberFormatException e) {
            throw new ParserException("Invalid BigDecimal format for expression: " + expression, e);
        }
    }


    public static XdmNode transformToFO(XdmNode xrContent) throws SaxonApiException {
        String xslToFO = appConfig.getXrToFo();
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable = compiler.compile(new StreamSource(xslToFO));

        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(xrContent);

        XdmDestination destination = new XdmDestination();

        transformer.setDestination(destination);
        transformer.transform();

        return destination.getXdmNode();
    }

    public static void renderPDF(XdmNode foInput, String outputPDFPath) throws IOException, FOPException, SaxonApiException {
        File tempFOFile = File.createTempFile("temp-output", ".fo");
        try (OutputStream foOut = new FileOutputStream(tempFOFile)) {
            Serializer serializer = foInput.getProcessor().newSerializer(foOut);
            serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
            serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
            serializer.serializeNode(foInput);
        } catch (IOException e) {
            throw new IOException("Error writing FO content to temporary file: " + e.getMessage());
        } catch (SaxonApiException e) {
            throw new SaxonApiException("Error serializing FO content: " + e.getMessage(), e);
        }

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        try (OutputStream pdfOut = new FileOutputStream(outputPDFPath)) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);
            javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer(); // Identity transformer
            transformer.transform(new javax.xml.transform.stream.StreamSource(tempFOFile), new SAXResult(fop.getDefaultHandler()));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Error writing PDF content to output file: " + e.getMessage());
        } catch (FOPException | javax.xml.transform.TransformerException e) {
            throw new FOPException("Error rendering PDF content: " + e.getMessage(), e);
        } finally {
            // Clean up temporary FO file
            tempFOFile.delete();
        }
    }
}
