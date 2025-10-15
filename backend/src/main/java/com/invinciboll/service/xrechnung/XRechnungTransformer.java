package com.invinciboll.service.xrechnung;

import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.exceptions.runtime.TransformerException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

//TODO: Dont convert xsl everytime

@Component
public class XRechnungTransformer {
    private Processor processor;
    private AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig, Processor injectedProcessor) {
        this.appConfig = appConfig;
        this.processor = injectedProcessor;
    }

    public XdmNode xmlToXr(XdmNode inputXmlDoc, XMLFormat xmlFormat) {
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
        XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(xslToXR));
        } catch (SaxonApiException e) {
            throw new TransformerException("Failed to compile XSLT for format: " + xmlFormat, e);
        }

        // Set up the transformer
        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(inputXmlDoc); // Set the input XML document as context

        // Set up the destination for the transformed result
        XdmDestination destination = new XdmDestination();
        transformer.setDestination(destination);
        try {
            transformer.transform();
        } catch (SaxonApiException e) {
            throw new TransformerException("Failed to transform XML to intermediate XR", e);
        }

        return destination.getXdmNode();
    }


    public XdmNode xrToFo(XdmNode xrContent) {
        String xslToFO = appConfig.getXrToFo();
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(xslToFO));
        } catch (SaxonApiException e) {
            throw new TransformerException("Failed to compile XSLT for XR to FO transformation", e);
        }

        XsltTransformer transformer = executable.load();
        transformer.setInitialContextNode(xrContent);

        XdmDestination destination = new XdmDestination();

        transformer.setDestination(destination);
        try {
            transformer.transform();
        } catch (SaxonApiException e) {
            throw new TransformerException("Failed to transform XR to FO", e);
        }

        return destination.getXdmNode();
    }
}
