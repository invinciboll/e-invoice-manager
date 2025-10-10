package com.invinciboll.service.printing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.invinciboll.configuration.AppConfig;

@Component
public class NetworkPrinter {
    private final String printerIp;
    private final Integer printerPort;

    @Autowired
    public NetworkPrinter(AppConfig appConfig) {
        this.printerIp = appConfig.getPrinterIp();
        this.printerPort = appConfig.getPrinterPort();
    }

    public void print(Path filePath) throws IOException {
        try (Socket socket = new Socket(printerIp, printerPort);
             FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("Failed to connect to printer at '" + printerIp + ":" + printerPort +"'. " + e.getMessage());
        }
    }
}
