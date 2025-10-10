package com.invinciboll.service.xrechnung;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;

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
    private final Configuration config;
    private final Check validator;

    @Autowired
    public XRechnungValidator(AppConfig appConfig) {
        Path scenarios = Paths.get(appConfig.getValidatorScenarios());
        this.config = Configuration.load(scenarios.toUri()).build(ProcessorProvider.getProcessor());
        this.validator = new DefaultCheck(config);
    }

    public boolean validate(XdmNode xmlContent, String name) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return false;
        }

        Input document = InputFactory.read(xmlContent, name);
        Result report = validator.checkInput(document);

        // examine the result here
        List<String> issues = new ArrayList<>();

        // Processing errors
        issues.addAll(report.getProcessingErrors());

        // Schema violations
        for (XmlError violation : report.getSchemaViolations()) {
            issues.add("Schema violation: " + violation.getMessage());
        }

        // Schematron failed asserts
        for (FailedAssert fa : report.getFailedAsserts()) {
            issues.add("Schematron error: " + fa.getText());
        }

        // Log all issues
        for (String issue : issues) {
            System.out.println(issue);
        }
        return report.isAcceptable();
    }
}

