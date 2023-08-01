package com.nukelord.MultiProgramRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MultiProgramRunner {
    private static final String CONFIG_FILE_PATH = "MultiProgramRunner.txt";
    private static final String LOG_FILE_PATH = "MultiProgramRunner.log";

    public static void main(String[] args) {
        List<String> files = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();
        int globalDelay = 0;

        try {
            // Clear the log file at the beginning of each run
            clearLogFile();
            // Read the configuration file and extract files and delays
            BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_PATH));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("FILE:")) {
                    String[] parts = line.split(":");
                    files.add(parts[1]);
                    delays.add(Integer.parseInt(parts[2]));
                } else if (line.startsWith("GLOBAL_DELAY:")) {
                    globalDelay = Integer.parseInt(line.split(":")[1]);
                }
            }
            reader.close();

            // Execute files with delays
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(LOG_FILE_PATH));
            for (int i = 0; i < files.size(); i++) {
                String file = files.get(i);
                int delay = delays.get(i);

                // If the individual file delay is specified, use it; otherwise, use the global delay
                int actualDelay = (delay > 0) ? delay : globalDelay;

                logWriter.write("Executing " + file + " with delay: " + actualDelay + " seconds\n");
                System.out.println("Executing " + file + " with delay: " + actualDelay + " seconds");

                // Check if the file exists before executing
                File fileToExecute = new File(file);
                if (fileToExecute.exists() && fileToExecute.isFile()) {
                    executeFile(fileToExecute);
                } else {
                    logWriter.write("Error: File not found " + file + "\n");
                    System.out.println("Error: File not found " + file);
                    // Log that the delay was skipped
                    logWriter.write("Skipped delay for " + file + "\n");
                    System.out.println("Skipped delay for " + file);
                    // Skip the delay if the file is not found
                    continue;
                }

                // Wait for the specified delay
                try {
                    Thread.sleep(actualDelay * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            logWriter.close();
            System.out.println("All files executed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeFile(File fileToExecute) {
        try {
            if (fileToExecute.isFile()) {
                // Get the absolute file path to handle spaces and special characters in the path
                String absoluteFilePath = fileToExecute.getAbsolutePath();

                // Execute the file using the default application association on the system
                Process process = Runtime.getRuntime().exec(absoluteFilePath);

                // Optional: Capture and log any output from the process (e.g., if the file is a command-line script)
                BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = outputReader.readLine()) != null) {
                    System.out.println("Process Output: " + line);
                }

                // Wait for the process to finish
                process.waitFor();
            } else {
                System.out.println("Error: Not a valid file to execute: " + fileToExecute.getName());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while executing file: " + fileToExecute.getName());
            e.printStackTrace();
        }
    }
    private static void clearLogFile() {
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(LOG_FILE_PATH));
            logWriter.write(""); // Clear the log file by writing an empty string
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
