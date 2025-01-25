package com.invinciboll;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.management.RuntimeErrorException;

import org.mustangproject.ZUGFeRD.ZUGFeRDInvoiceImporter;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;

import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

public class FormatDetector {

    public static FileFormat detectFileFormat(Path inputFile) throws IOException {
        String filePath = inputFile.toString();
        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fis.read(header, 0, 4);
        } catch (IOException e) {
            throw new IOException("Error reading file header: " + e.getMessage());
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

    public static String computeFileHash(Path inputFile, String hashAlgorithm) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid hash algorithm: " + hashAlgorithm, e);
        }

        // Read the file content and update the hash
        try (FileInputStream fis = new FileInputStream(inputFile.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Error reading file for hashing: " + e.getMessage());
        }

        // Convert the hash bytes to a hexadecimal string
        return byteArrayToHex(digest.digest());
    }

    private static String byteArrayToHex(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
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

    public static XMLFormat detectXmlFormat(XdmNode xmlDocument) {
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

