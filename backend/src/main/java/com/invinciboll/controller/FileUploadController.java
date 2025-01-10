package com.invinciboll.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.Invoice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        System.out.println("File type: " + contentType);
        // Create a temporary file path to save the uploaded file
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");  // Define the directory where files will be saved
        try {
            // Ensure the directory exists
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Save the file to the directory
            Path filePath = uploadDir.resolve(file.getOriginalFilename());
            file.transferTo(filePath.toFile());

            // Pass the Path to the Invoice class
            Invoice invoice = new Invoice(filePath);

            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the file.");
        }
    }
}
