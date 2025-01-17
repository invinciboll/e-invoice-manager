package com.invinciboll;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkPrinter {
    public static void print(String filePath) {
        String printerIp = "192.168.2.134"; // Replace with your printer's static IP
        int printerPort = 9100; // Default raw printing port

        try (Socket socket = new Socket(printerIp, printerPort);
             FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Send the file contents to the printer
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Print job sent to printer at " + printerIp);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send print job.");
        }
    }
}
