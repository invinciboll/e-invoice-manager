package com.invinciboll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.mustangproject.ZUGFeRD.ZUGFeRDInvoiceImporter;

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


public class XRechnungTransformer {
    private static final Processor processor = new Processor(false); // Saxon Processor (no schema validation)
    private static final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI()); // FOP Factory

    public static XdmNode parseXmlContent(Path inputPath, FileFormat fileFormat) throws Exception {
        String xmlContentString;

        // Read XML content depending on the file format
        if (fileFormat == FileFormat.ZF_PDF) {
            ZUGFeRDInvoiceImporter zii = new ZUGFeRDInvoiceImporter(inputPath.toString());
            xmlContentString = zii.getUTF8(); // Extract XML content from ZF_PDF
        } else if (fileFormat == FileFormat.XML) {
            xmlContentString = Files.readString(inputPath, StandardCharsets.UTF_8); // Read XML from file
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
        }

        DocumentBuilder builder = processor.newDocumentBuilder();
        builder.setLineNumbering(true); // Optional: Enable line numbering for debugging

        // Parse the XML content string into an XdmNode
        return builder.build(new StreamSource(new StringReader(xmlContentString)));
    }

    public static XdmNode transformToXR(XdmNode inputXmlDoc, XMLFormat xmlFormat) throws Exception {
        String xslToXR;

        // Determine the appropriate XSLT based on XML format
        switch (xmlFormat) {
            case UBL_INVOICE:
                xslToXR = AppConfig.getInstance().getProperty("xsl.ubl-invoice.to.xr");
                break;
            case UBL_CREDIT_NOTE:
                xslToXR = AppConfig.getInstance().getProperty("xsl.ubl-creditnote.to.xr");
                break;
            case CII:
                xslToXR = AppConfig.getInstance().getProperty("xsl.cii.to.xr");
                break;
            default:
                throw new IllegalArgumentException("Unsupported XML format: " + xmlFormat);
        }

        // Compile the XSLT
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable = compiler.compile(new StreamSource(xslToXR));

        // Set up the transformer
        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(inputXmlDoc); // Set the input XML document as context

        // Set up the destination for the transformed result
        XdmDestination destination = new XdmDestination();

        // Perform the transformation
        transformer.setDestination(destination);
        transformer.transform();

        // Return the resulting XdmNode
        return destination.getXdmNode();
    }

    public static KeyInformation extractKeyInformation(XdmNode xrContent) {

        XPathCompiler xpathCompiler = processor.newXPathCompiler();

        // Declare namespaces if necessary (adjust based on your schema)
        xpathCompiler.declareNamespace("xr", "urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung-1");

        // XPath expressions for Seller Name and Invoice Number
        String sellerNameXPath = "//xr:Seller_name";
        String invoiceNumberXPath = "//xr:invoice/xr:Invoice_number";

        String sellerName = "Not Found";
        String invoiceNumber = "Not Found";

        try {
            sellerName = extractValue(xpathCompiler, xrContent, sellerNameXPath);
            invoiceNumber = extractValue(xpathCompiler, xrContent, invoiceNumberXPath);
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }

        return new KeyInformation(invoiceNumber, sellerName);
    }

    private static String extractValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws SaxonApiException {
        XdmValue result = xpathCompiler.evaluate(expression, xrContent);
        return result.size() > 0 ? result.itemAt(0).getStringValue() : "Not Found";
    }


    public static XdmNode transformToFO(XdmNode xrContent) throws Exception {
        String xslToFO = AppConfig.getInstance().getProperty("xsl.xr.to.fo");
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable = compiler.compile(new StreamSource(xslToFO));

        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(xrContent);

        XdmDestination destination = new XdmDestination();

        transformer.setDestination(destination);
        transformer.transform();

        return destination.getXdmNode();
    }

    public static void renderPDF(XdmNode foInput, String outputPDFPath) throws Exception {
        File tempFOFile = File.createTempFile("temp-output", ".fo");
        try (OutputStream foOut = new FileOutputStream(tempFOFile)) {
            Serializer serializer = foInput.getProcessor().newSerializer(foOut);
            serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
            serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
            serializer.serializeNode(foInput);
        }

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        try (OutputStream pdfOut = new FileOutputStream(outputPDFPath)) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);
            javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer(); // Identity transformer
            transformer.transform(new javax.xml.transform.stream.StreamSource(tempFOFile), new SAXResult(fop.getDefaultHandler()));
        } finally {
            // Clean up temporary FO file
            tempFOFile.delete();
        }
    }
}
