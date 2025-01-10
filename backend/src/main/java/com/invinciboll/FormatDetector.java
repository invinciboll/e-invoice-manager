package com.invinciboll;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.mustangproject.ZUGFeRD.ZUGFeRDInvoiceImporter;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

public class FormatDetector {

    public static FileFormat detectFileFormat(Path inputFile) throws Exception {
        String filePath = inputFile.toString();
        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fis.read(header, 0, 4);
        } catch (IOException e) {
            throw new Exception("Error reading file header: " + e.getMessage());
        }

        if (isXML(header)) {
            return FileFormat.XML;
        }

        if (isPDF(header)) {
            if (isZUGFeRDPdf(filePath)) {
                return FileFormat.ZF_PDF;
            }
            return FileFormat.PDF;
        }

        return FileFormat.INVALID;
    }

    private static boolean isXML(byte[] header) {
        return header.length >= 4
            && header[0] == '<' && header[1] == '?' && header[2] == 'x' && header[3] == 'm';
    }

    private static boolean isPDF(byte[] header) {
        return header.length >= 4
            && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
    }

    private static boolean isZUGFeRDPdf(String pdfPath) {
        ZUGFeRDInvoiceImporter zii = null;
        try {
            zii = new ZUGFeRDInvoiceImporter(pdfPath);
            zii.extractInvoice(); // Will fail if not a ZUGFeRD PDF
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static XMLFormat detectXmlFormat(XdmNode xmlDocument) throws Exception {
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
}

