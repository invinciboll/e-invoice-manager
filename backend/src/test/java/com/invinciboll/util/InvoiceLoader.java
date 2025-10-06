package com.invinciboll.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceLoader {

    private static Map<String, Map<String, Map<String, String>>> metadata;

    // Lazy-load metadata
    private static void loadMetadata() {
        if (metadata != null) return;
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource("testfiles/invoice_metadata.json").getInputStream()) {
            metadata = mapper.readValue(
                    is,
                    new TypeReference<Map<String, Map<String, Map<String, String>>>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load invoice metadata", e);
        }
    }

    public static Map<String, Map<String, String>> getCategory(String category) {
        loadMetadata();
        return metadata.get(category);
    }

    public static Map<String, Map<String, Map<String, String>>> getAllMetadata() {
        loadMetadata();
        return metadata;
    }

    public static String getFileDescription(String category, String fileName) {
        loadMetadata();
        return metadata.get(category).get(fileName).get("description");
    }

    public static String getXmlType(String category, String fileName) {
        loadMetadata();
        return metadata.get(category).get(fileName).get("xml_type");
    }

    public static String getFileType(String category, String fileName) {
        loadMetadata();
        return metadata.get(category).get(fileName).get("file_type");
    }

    // -----------------------------
    // New: TestFile helper class
    // -----------------------------
    public static class TestFile {
        public final String category;
        public final String fileName;
        public final String fileType;
        public final String xmlType;

        public TestFile(String category, String fileName, String fileType, String xmlType) {
            this.category = category;
            this.fileName = fileName;
            this.fileType = fileType;
            this.xmlType = xmlType;
        }

        @Override
        public String toString() {
            return category + "/" + fileName;
        }
    }

    public static List<TestFile> getAllTestFiles() {
        loadMetadata();
        return metadata.entrySet().stream()
                .flatMap(categoryEntry ->
                        categoryEntry.getValue().entrySet().stream()
                                .map(fileEntry -> new TestFile(
                                        categoryEntry.getKey(),
                                        fileEntry.getKey(),
                                        fileEntry.getValue().get("file_type"),
                                        fileEntry.getValue().get("xml_type")
                                ))
                )
                .collect(Collectors.toList());
    }

    public static List<TestFile> getAllValidInvoiceTestFiles() {
        loadMetadata();
        return metadata.entrySet().stream()
                .flatMap(categoryEntry ->
                        categoryEntry.getValue().entrySet().stream()
                                .filter(fileEntry -> {
                                    String fileType = fileEntry.getValue().get("file_type");
                                    return "xml".equals(fileType) || "factur-x".equals(fileType);
                                })
                                .map(fileEntry -> new TestFile(
                                        categoryEntry.getKey(),
                                        fileEntry.getKey(),
                                        fileEntry.getValue().get("file_type"),
                                        fileEntry.getValue().get("xml_type")
                                ))
                )
                .collect(Collectors.toList());
    }
}
