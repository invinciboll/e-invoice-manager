package com.invinciboll.service.xrechnung;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.enums.FileFormat;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.xml.ProcessorProvider;
import net.sf.saxon.s9api.XdmNode;

@Component
public class XRechnungValidator {
    private final Check validator_xrechnung;
    private final Check validator_zugferd;

    @Autowired
    public XRechnungValidator(AppConfig appConfig) {
        Path scenarios = Paths.get(appConfig.getValidatorScenariosXrechnung());
        var config = Configuration.load(scenarios.toUri()).build(ProcessorProvider.getProcessor());
        this.validator_xrechnung = new DefaultCheck(config);

        scenarios = Paths.get(appConfig.getValidatorScenariosZugferd());
        config = Configuration.load(scenarios.toUri()).build(ProcessorProvider.getProcessor());
        this.validator_zugferd = new DefaultCheck(config);
    }

    public boolean validate(XdmNode xmlContent, FileFormat format, String name) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            System.out.println("[Validator] XML content is empty.");
            return false;
        }

        var validator = switch (format) {
            case ZF_PDF -> this.validator_zugferd;
            default ->  this.validator_xrechnung;
        };

        Input document = InputFactory.read(xmlContent, name);
        Result report = validator.checkInput(document);

        System.out.println("=== VALIDATION REPORT for: " + name + " ===");
        System.out.println("Processing successful: " + report.isProcessingSuccessful());
        System.out.println("Acceptable: " + report.isAcceptable());
        System.out.println("Schema valid: " + report.isSchemaValid());
        System.out.println("Schematron valid: " + report.isSchematronValid());

        // 1. Processing Errors
        if (!report.getProcessingErrors().isEmpty()) {
            System.out.println("---- Processing Errors ----");
            report.getProcessingErrors().forEach(System.out::println);
        }

        // 2. Schema Violations
        List<XmlError> schemaViolations = report.getSchemaViolations();
        if (schemaViolations != null && !schemaViolations.isEmpty()) {
            System.out.println("---- Schema Violations ----");
            schemaViolations.forEach(violation -> {
                System.out.println(
                    "Line " + violation.getRowNumber() +
                    ", Column " + violation.getColumnNumber() +
                    " - " + violation.getMessage()
                );
            });
        }

        // 3. Failed Schematron Asserts
        List<FailedAssert> failedAsserts = report.getFailedAsserts();
        if (failedAsserts != null && !failedAsserts.isEmpty()) {
            System.out.println("---- Schematron Failed Asserts ----");
            failedAsserts.forEach(assertion -> {
                System.out.println(
                    "Location: " + assertion.getLocation() +
                    ", Test: " + assertion.getTest() +
                    ", Text: " + assertion.getText()
                );
            });
        }
        System.out.println("=== END VALIDATION REPORT ===");
        return report.isProcessingSuccessful();
    }

}

