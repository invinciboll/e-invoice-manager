package com.invinciboll;

import java.io.File;


public class App 
{
    public static void main(String[] args) {
        try {
            // Load configuration
            AppConfig config = AppConfig.getInstance("config.properties");


            String folderPath = config.getProperty("testfiles.xrechnung");
            // String folderPath = config.getProperty("testfiles.zugferd");

            // Process all XML files in the folder
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            processFile(file);
                        }
                        break;
                    }
                } else {
                    System.out.println("No XML files found in the folder: " + folderPath);
                }
            } else {
                System.out.println("Invalid folder path: " + folderPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processFile(File file) {

        Invoice invoice = new Invoice(file.toPath());
    }

}
