package com.invinciboll.service.xrechnung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.entities.KeyInformation;
import com.invinciboll.exceptions.ParserException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;


@Component
public class XRechnungParser {
    private Processor processor;

    @Autowired
    public void setProcessor(Processor injectedProcessor) {
        this.processor = injectedProcessor;
    }

    /**
     * Extracts key information from the provided XRechnung XR content.
     *
     * @param xrContent The XdmNode representing the XRechnung XR content.
     * @return A KeyInformation object containing extracted details.
     * @throws ParserException If any parsing or extraction error occurs.
     */
    public KeyInformation extractKeyInformation(XdmNode xrContent) throws ParserException {
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
        KeyInformation keyInformation = new KeyInformation(invoiceReference, sellerName, invoiceTypeCode, issuedDate, totalSum);
        return keyInformation;
    }


    public String extractStringValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            XdmValue result = xpathCompiler.evaluate(expression, xrContent);
            return result.size() > 0 ? result.itemAt(0).getStringValue() : null;
        } catch (SaxonApiException e) {
            throw new ParserException("Error extracting string value for expression: " + expression, e);
        }
    }

    public Integer extractIntegerValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : Integer.MIN_VALUE;
        } catch (NumberFormatException e) {
            throw new ParserException("Invalid integer format for expression: " + expression, e);
        }
    }

    public LocalDate extractLocalDateValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? LocalDate.parse(value) : LocalDate.MIN;
        } catch (DateTimeParseException e) {
            throw new ParserException("Invalid date format for expression: " + expression, e);
        }
    }

    public BigDecimal extractBigDecimalValue(XPathCompiler xpathCompiler, XdmNode xrContent, String expression) throws ParserException {
        try {
            String value = extractStringValue(xpathCompiler, xrContent, expression);
            return value != null && !value.isEmpty() ? new BigDecimal(value) : BigDecimal.valueOf(-1);
        } catch (NumberFormatException e) {
            throw new ParserException("Invalid BigDecimal format for expression: " + expression, e);
        }
    }
}
