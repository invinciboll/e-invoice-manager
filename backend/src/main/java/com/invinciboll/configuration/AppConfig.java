package com.invinciboll.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
@Getter
@Component
public class AppConfig {

    // XSLT Stylesheets
    @Value("${xsl.ubl-invoice.to.xr}")
    private String ublInvoiceToXR;

    @Value("${xsl.ubl-creditnote.to.xr}")
    private String ublCreditNoteToXR;

    @Value("${xsl.cii.to.xr}")
    private String ciiToXR;

    @Value("${xsl.xr.to.fo}")
    private String xrToFo;

    @Value("${xsl.schema}")
    private String schemaXSL;

    // Output path
    @Value("${output.dir}")
    private String outputDir;

    @Value("${tempfiles.dir}")
    private String tempfilesDir;

    // Testfiles xrechnung (from xrechnung-visualization repo)
    @Value("${testfiles.xrechnung}")
    private String testfilesXrechnung;

    // Testfiles zugferd / factur-x(from feRD package)
    @Value("${testfiles.zugferd}")
    private String testfilesZugferd;

    // en or de
    @Value("${language}")
    private String language;

    @Value("${frontend.host}")
    private String frontendHost;

    @Value("${frontend.port}")
    private String frontendPort;

    @Value("${server.host}")
    private String backendHost;

    @Value("${server.port}")
    private String backendPort;

    @Value("${printer.ip}")
    private String printerIp;

    @Value("${printer.port}")
    private int printerPort;
}