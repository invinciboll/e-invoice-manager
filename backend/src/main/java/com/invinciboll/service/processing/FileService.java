package com.invinciboll.service.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.invinciboll.configuration.AppConfig;
import com.invinciboll.service.printing.NetworkPrinter;
import com.invinciboll.util.Utils;

@Service
public class FileService {
    private final NetworkPrinter printer;

    @Autowired
    public FileService(NetworkPrinter printer, AppConfig appConfig) {
        this.printer = printer;

        String tempfiles = appConfig.getTempfilesDir();
        var tempFilesPath = Paths.get(System.getProperty("user.dir"), tempfiles);
        if (!Files.exists(tempFilesPath)) {
            try {
                Files.createDirectories(tempFilesPath);
            } catch (IOException e) {
                throw new RuntimeException("Error creating tempfiles directory: " + e.getMessage(), e);
            }
        }
    }

    public String computeFileHash(Path filePath) throws IOException {
        return Utils.computeFileHash(filePath);
    }

    public void print(Path filePath) throws IOException {
        printer.print(filePath);
    }

    public void transferFile(MultipartFile file, Path filePath) throws IOException {
        file.transferTo(filePath);
    }

}
