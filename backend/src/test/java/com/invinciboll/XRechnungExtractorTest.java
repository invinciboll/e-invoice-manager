package com.invinciboll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.invinciboll.enums.FileFormat;
import com.invinciboll.enums.XMLFormat;
import com.invinciboll.service.xrechnung.XRechnungExtractor;
import com.invinciboll.util.InvoiceLoader;

import net.sf.saxon.s9api.XdmNode;

@SpringBootTest
class XRechnungExtractorTest {

    @Autowired
    private XRechnungExtractor extractor; // the Spring-managed bean

    /**
     * Uses InvoiceLoader to supply all test files automatically.
     * Tests file format detection for various invoice file types.
     */
    @ParameterizedTest(name = "Test {index}: {0} → expecting {1}")
    @MethodSource("com.invinciboll.util.InvoiceLoader#getAllTestFiles")
    void testDetectFileFormat(InvoiceLoader.TestFile testFile) {
        // Build path to the test file in resources
        Path path = Path.of("src/test/resources/testfiles", testFile.category, testFile.fileName);
        assertTrue(path.toFile().exists(), "Test file should exist: " + path);
        try {
            var format = extractor.detectFileFormat(path);

            switch (testFile.fileType) {
                case "xml":
                    assertEquals(FileFormat.XML, format, "File should be detected as XML: " + testFile.fileName + " Detected: " + format);
                    break;
                case "factur-x":
                    assertTrue(format == FileFormat.ZF_PDF,
                            "Factur-X files should be detected as ZF_PDF: " + testFile.fileName + " Detected: " + format);
                    break;
                case "plain-pdf":
                    assertEquals(FileFormat.PDF, format, "Invalid files should be detected as INVALID: " + testFile.fileName);
                    break;
                default:
                    fail("Unknown file_type in metadata: " + testFile.fileType);
            }

        } catch (Exception e) {
            fail("Exception while detecting file format for " + testFile.fileName + ": " + e.getMessage());
        }
    }

    /**
     * Uses InvoiceLoader to supply all valid (XML/ZF) test files automatically.
     * Tests XML format detection for various invoice XML types.
     */
    @ParameterizedTest(name = "Test {index}: {0} → expecting {1}")
    @MethodSource("com.invinciboll.util.InvoiceLoader#getAllValidInvoiceTestFiles")
    void testDetectXmlFormat(InvoiceLoader.TestFile testFile) {
        XMLFormat expectedXmlFormat = XMLFormat.fromMetadata(testFile.xmlType);

        Path path = Path.of("src/test/resources/testfiles", testFile.category, testFile.fileName);
        FileFormat fileFormat = FileFormat.fromMetadata(testFile.fileType);

        try {
            assertTrue(path.toFile().exists(), "Test file should exist: " + path);

            XdmNode xmlDocument = extractor.parseXmlContent(path, fileFormat);
            assertNotNull(xmlDocument, "Parsed XML document should not be null");

            XMLFormat detectedFormat = extractor.detectXmlFormat(xmlDocument);
            assertEquals(expectedXmlFormat, detectedFormat,
                    "Detected XML format should match expected for " + testFile.fileName);
        } catch (Exception e) {
            fail("Exception while parsing XML content for " + testFile.fileName + ": " + e.getMessage());
        }
    }
}
