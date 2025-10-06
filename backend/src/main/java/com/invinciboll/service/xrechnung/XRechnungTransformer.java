package com.invinciboll.service.xrechnung;

import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.enums.XMLFormat;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

@Component
public class XRechnungTransformer {
    private Processor processor;
    private AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig, Processor injectedProcessor) {
        this.appConfig = appConfig;
        this.processor = injectedProcessor;
    }

    public XdmNode xmlToXr(XdmNode inputXmlDoc, XMLFormat xmlFormat) throws SaxonApiException {
        String xslToXR;
        System.out.println("Transforming XML to XR format: " );
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

        System.out.println("inputXmlDoc" + inputXmlDoc.toString());

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


    public XdmNode xrToFo(XdmNode xrContent) throws SaxonApiException {
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
}
