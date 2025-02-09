package com.invinciboll.enums;

import java.util.HashMap;
import java.util.Map;

import com.invinciboll.configuration.AppConfig;

public enum ErrorCode {
    ERR001("ERR001"),
    ERR002("ERR002"),
    ERR003("ERR003"),
    ERR004("ERR004"),
    ERR005("ERR005"),
    WARN001("WARN001"),
    WARN002("WARN002");

    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        // English messages
        Map<String, String> enMessages = new HashMap<>();
        enMessages.put("ERR001", "File format is invalid, must be PDF or XML.");
        enMessages.put("ERR002", "PDF contains embedded XML, but the XML is not compliant to EN16931. It is not a valid e-invoice.");
        enMessages.put("ERR003", "XML is not compliant to EN16931. It is not a valid e-invoice.");
        enMessages.put("ERR004", "Internal server error. Please retry and contact the admin if the issue persists.");
        enMessages.put("ERR005", "File is corrupted.");
        enMessages.put("WARN001", "PDF appears to be ZUGFeRD/Factur-X, but embedded XML is deviating from EN16931.");
        enMessages.put("WARN002", "XML appears to be XRechnung, but the XML is deviating from EN16931.");
        messages.put("en", enMessages);

        // German messages
        Map<String, String> deMessages = new HashMap<>();
        deMessages.put("ERR001", "Dateiformat ist ungültig, muss PDF oder XML sein.");
        deMessages.put("ERR002", "PDF enthält eingebettetes XML, aber das XML entspricht nicht EN16931. Es ist keine gültige E-Rechnung.");
        deMessages.put("ERR003", "XML entspricht nicht EN16931. Es ist keine gültige E-Rechnung.");
        deMessages.put("ERR004", "Interner Serverfehler. Bitte erneut versuchen und bei anhaltenden Problemen den Administrator kontaktieren.");
        deMessages.put("ERR005", "Datei ist beschädigt.");
        deMessages.put("WARN001", "PDF scheint ZUGFeRD/Factur-X zu sein, aber das eingebettete XML weicht von EN16931 ab.");
        deMessages.put("WARN002", "XML scheint XRechnung zu sein, aber das XML weicht von EN16931 ab.");
        messages.put("de", deMessages);
    }

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        // String lang = AppConfig.getInstance().getLanguage();
        return messages.getOrDefault("en", messages.get("en")).getOrDefault(code, "Unknown error");
    }
}
