package com.invinciboll.service.xrechnung;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.stereotype.Component;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

@Component
public class XRechnungVisualizer {
    private static final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI()); // FOP Factory

    /**
     * Renders an XDM node containing XSL-FO content to a PDF file.
     *
     * @param foInput       The XDM node containing the XSL-FO content.
     * @param outputPDFPath The path where the output PDF should be saved.
     * @throws IOException   If an I/O error occurs during file operations.
     * @throws FOPException  If an error occurs during PDF rendering.
     * @throws SaxonApiException If an error occurs during serialization of the XDM node.
     */
    public void renderPDF(XdmNode foInput, String outputPDFPath) throws IOException, FOPException, SaxonApiException {
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
