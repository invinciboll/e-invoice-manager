package com.invinciboll.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.App;
import com.invinciboll.AppConfig;
import com.invinciboll.Invoice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(
            @RequestParam("file") MultipartFile file) {

        // Validate the file type
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/pdf") &&
             !contentType.equals("application/xml") &&
             !contentType.equals("text/xml"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only PDF and XML files are allowed.");
        }
        // Create a temporary file path to save the uploaded file
        String tempfiles = "tempfiles"; // Default directory
        try {
            // Load configuration
            tempfiles = AppConfig.getInstance("config.properties").getProperty("tempfiles.dir");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Path tempfilesDir = Paths.get(System.getProperty("user.dir"), tempfiles);  // Define the directory where files will be saved

        try {
            // Ensure the directory exists
            if (!Files.exists(tempfilesDir)) {
                Files.createDirectories(tempfilesDir);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = tempfilesDir.resolve(newFileName);
            file.transferTo(filePath.toFile()); // Save the file to the directory

            Invoice invoice = new Invoice(filePath, originalFileName);
            invoice.save();
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the file.");
        }
    }
}
