package com.invinciboll.service.xrechnung;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.exceptions.runtime.ExtractorException;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

//TODO: Wording and processing flow can be improved.

/**
 * Detects file format and XML format of invoices.
 * Extracts embedded XML from ZF PDFs.
 * Parses XML content from files.
 */
@Component
public class XRechnungExtractor {
    private Processor processor;


    @Autowired
    public void setProcessor(Processor injectedProcessor) {
        this.processor = injectedProcessor;
    }

    /**
     * Detects the file format of the given input file.
     * Supports detection of XML files, ZF PDFs (PDFs with embedded XML), and plain PDFs.
     *
     * @param inputFile Path to the input file.
     * @return Detected FileFormat (XML, ZF_PDF, PDF, or INVALID).
     */
    public FileFormat detectFileFormat(Path inputFile) {
        String filePath = inputFile.toString();
        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fis.read(header, 0, 4);
        } catch (IOException e) {
            throw new ExtractorException("Failed to read file header from: " + filePath.toString(), e);
        }

        if (isXML(header)) {
            return FileFormat.XML;
        }

        if (isPDF(header)) {
            if (hasEmbeddedXml(filePath)) {
                return FileFormat.ZF_PDF;
            }
            return FileFormat.PDF;
        }

        return FileFormat.INVALID;
    }

    /**
     * Checks if the given header bytes indicate an XML file.
     *
     * @param header First few bytes of the file.
     * @return True if the header indicates an XML file, false otherwise.
     */
    private boolean isXML(byte[] header) {
        return header.length >= 4
            && header[0] == '<' && header[1] == '?' && header[2] == 'x' && header[3] == 'm';
    }

    /**
     * Checks if the given header bytes indicate a PDF file.
     *
     * @param header First few bytes of the file.
     * @return True if the header indicates a PDF file, false otherwise.
     */
    private boolean isPDF(byte[] header) {
        return header.length >= 4
            && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
    }

    /**
     * Checks if the given PDF file has embedded XML content.
     *
     * @param pdfPath Path to the PDF file.
     * @return True if the PDF has embedded XML, false otherwise.
     */
    private boolean hasEmbeddedXml(String pdfPath) {
        try (PDDocument document = PDDocument.load(new java.io.File(pdfPath))) {
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            if (catalog == null) return false;

            // check for embedded files
            var names = catalog.getNames();
            if (names != null && names.getEmbeddedFiles() != null) {
                var embeddedFiles = names.getEmbeddedFiles().getNames();
                for (var entry : embeddedFiles.entrySet()) {
                    String fileName = entry.getKey();
                    if (fileName.toLowerCase().endsWith(".xml")) {
                        return true;
                    }
                }
            }

            // fallback: check for XML in metadata
            PDMetadata metadata = catalog.getMetadata();
            if (metadata != null) {
                String meta = new String(metadata.toByteArray());
                if (meta.contains("<CrossIndustryInvoice") || meta.contains("<Invoice")) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new ExtractorException("PDF was not loaded correctly, unable to check for embedded XML: " + pdfPath, e);
        }
        return false;
    }

    /**
     * Detects the XML format of the given XML document.
     * Supports detection of UBL Invoice, UBL Credit Note, and CII formats.
     *
     * @param xmlDocument Parsed XML document as XdmNode.
     * @return Detected XMLFormat (UBL_INVOICE, UBL_CREDIT_NOTE, CII, or UNKNOWN).
     * @throws IllegalArgumentException If the provided XML does not have a root element.
     */
    public XMLFormat detectXmlFormat(XdmNode xmlDocument) {
        // Get the root element explicitly
        XdmNode rootElement = null;
        for (XdmNode child : xmlDocument.children()) {
            if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
                rootElement = child;
                break;
            }
        }

        // Handle the case where no root element is found
        if (rootElement == null) {
            throw new IllegalArgumentException("The provided XML does not have a root element.");
        }

        // Get the root element's local name and namespace
        QName rootName = rootElement.getNodeName(); // Get QName of the root element
        String localName = rootName.getLocalName(); // Local name of the root element
        String namespaceURI = rootName.getNamespaceURI(); // Namespace URI of the root element

        // Check for UBL namespaces and root elements
        if ("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2".equals(namespaceURI) && "Invoice".equals(localName)) {
            return XMLFormat.UBL_INVOICE;
        } else if ("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2".equals(namespaceURI) && "CreditNote".equals(localName)) {
            return XMLFormat.UBL_CREDIT_NOTE;
        }

        // Check for CII namespaces and root elements
        if ("urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100".equals(namespaceURI) && "CrossIndustryInvoice".equals(localName)) {
            return XMLFormat.CII;
        }

        // If no match, return UNKNOWN_XML
        return XMLFormat.UNKNOWN;
    }

    /**
     * Extracts the embedded XML content from a ZF PDF file.
     *
     * @param pdfPath Path to the PDF file.
     * @return Extracted XML content as a String.
     */
    private String extractEmbeddedXmlFromPdf(Path pdfPath) {
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {

            PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
            PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();

            if (embeddedFiles == null) {
                throw new ExtractorException("No embedded files found in PDF: " + pdfPath.toString());
            }

            Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
            if (embeddedFileNames == null || embeddedFileNames.isEmpty()) {
                throw new ExtractorException("No named embedded files found in PDF: " + pdfPath.toString());
            }

            for (Map.Entry<String, PDComplexFileSpecification> entry : embeddedFileNames.entrySet()) {
                String fileName = entry.getKey().toLowerCase();
                if (fileName.endsWith(".xml")) {
                    PDComplexFileSpecification fileSpec = entry.getValue();

                    // In PDFBox 3.x, use getCOSObject() to get the COSStream
                    COSStream cosStream = fileSpec.getEmbeddedFile().getCOSObject();

                    try (var inputStream = cosStream.createInputStream();
                         var baos = new ByteArrayOutputStream()) {
                        inputStream.transferTo(baos);
                        return baos.toString(StandardCharsets.UTF_8);
                    }
                }
            }

            throw new ExtractorException("No embedded XML file found in PDF: " + pdfPath.toString());
        } catch (IOException e) {
            throw new ExtractorException("Failed to extract embedded XML from PDF: " + pdfPath.toString(), e);
        }
    }

    /**
     * Parses XML content from the given file path based on the specified file format.
     * Supports parsing from ZF PDFs (PDFs with embedded XML) and plain XML files.
     *
     * @param inputPath Path to the input file (PDF or XML).
     * @param fileFormat Detected FileFormat of the input file.
     * @return Parsed XML document as XdmNode.
     */
    public XdmNode parseXmlContent(Path inputPath, FileFormat fileFormat) {
        if (fileFormat != FileFormat.ZF_PDF && fileFormat != FileFormat.XML) {
            throw new ExtractorException("Unsupported file format. Can not extract XML content from File: " + inputPath.toString());
        }

        String xmlContentString;

        if (fileFormat == FileFormat.ZF_PDF) {
            xmlContentString = extractEmbeddedXmlFromPdf(inputPath);
        } else  {
            try{
                xmlContentString = Files.readString(inputPath, StandardCharsets.UTF_8); // Read XML from file
            } catch (IOException e) {
                throw new ExtractorException("Unable to read XML content from XML file: " + inputPath.toString(), e);
            }
        }

        DocumentBuilder builder = processor.newDocumentBuilder();
        try {
            return builder.build(new StreamSource(new StringReader(xmlContentString)));
        } catch (SaxonApiException e) {
            throw new ExtractorException("Unable to create Saxon document from XML content: ", e);
        }
    }
}

