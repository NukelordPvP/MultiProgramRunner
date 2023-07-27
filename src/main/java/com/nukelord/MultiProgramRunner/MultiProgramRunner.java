import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MultiProgramRunner {
    public static void main(String[] args) {
        try {
            ProgramConfig config = loadConfig("MultiProgramRunner.txt");

            List<JarFileEntry> jarList = config.jarList;
            int globalDelay = config.globalDelay;

            BufferedWriter logWriter = new BufferedWriter(new FileWriter("MultiProgramRunner.log", true));

            for (JarFileEntry jarEntry : jarList) {
                String jarFileName = jarEntry.jarFileName;
                int jarDelay = jarEntry.delayBetweenJars;

                File jarFile = new File(jarFileName);
                if (!jarFile.exists() || !jarFile.isFile()) {
                    logWriter.write("Error: " + jarFileName + " not found or is not a valid JAR file.\n");
                    logWriter.flush();
                    continue;
                }

                runJar(jarFileName);
                int delay = (jarDelay > 0) ? jarDelay : globalDelay;
                if (delay > 0) {
                    Thread.sleep(delay * 1000); // Convert seconds to milliseconds for Thread.sleep()
                }
            }

            logWriter.close();
        } catch (IOException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ProgramConfig loadConfig(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        int globalDelay = 0;
        List<JarFileEntry> jarList = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("GLOBAL_DELAY:")) {
                globalDelay = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            } else if (line.startsWith("JARS:")) {
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    String[] parts = line.split(";");
                    String jarFileName = parts[0].trim();
                    int jarDelay = (parts.length > 1 && !parts[1].trim().isEmpty()) ? Integer.parseInt(parts[1].trim()) : 0;
                    jarList.add(new JarFileEntry(jarFileName, jarDelay));
                }
            }
        }

        reader.close();

        return new ProgramConfig(jarList, globalDelay);
    }

    private static void runJar(String jarFileName) throws IOException {
        String javaCmd = System.getProperty("java.home") + "/bin/java";
        String command = javaCmd + " -jar " + jarFileName;

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ProgramConfig {
        List<JarFileEntry> jarList;
        int globalDelay;

        ProgramConfig(List<JarFileEntry> jarList, int globalDelay) {
            this.jarList = jarList;
            this.globalDelay = globalDelay;
        }
    }

    private static class JarFileEntry {
        String jarFileName;
        int delayBetweenJars;

        JarFileEntry(String jarFileName, int delayBetweenJars) {
            this.jarFileName = jarFileName;
            this.delayBetweenJars = delayBetweenJars;
        }
    }
}