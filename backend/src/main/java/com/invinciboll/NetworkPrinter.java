package com.invinciboll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkPrinter {
    public static void print(String printerIp, Integer printerPort, String filePath) throws IOException {
        try (Socket socket = new Socket(printerIp, printerPort);
             FileInputStream fileInputStream = new FileInputStream(new File(filePath));
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
