package dk.pfrandsen.util;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.GenerateConfigFile;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.wsdl.wsi.BasicProfileConfig;
import dk.pfrandsen.wsdl.wsi.WsiProfile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class WsiUtil {

    public static final String CONFIG_FILENAME = "wsi-config.xml";
    public static final String REPORT_FILENAME = "wsi-report.xml";
    public static final String SUMMARY_FILENAME = "wsi-summary.json";

    private static String generateName(String base, String filename) {
        String[] parts = base.split("\\.");
        return parts[0] + "-" + filename + "." + parts[1];
    }

    public static String getConfigFilename(String wsdlName) {
        return generateName(CONFIG_FILENAME, wsdlName);
    }

    public static String getReportFilename(String wsdlName) {
        return generateName(REPORT_FILENAME, wsdlName);
    }

    public static String getSummaryFilename(String wsdlName) {
        return generateName(SUMMARY_FILENAME, wsdlName);
    }

    public static boolean generateConfigurationFile(Path toolRoot, Path wsdl, Path report, Path config) {
        GenerateConfigFile tool = new GenerateConfigFile();
        String template = tool.getDefaultTemplate();
        Path stylesheet = BasicProfileConfig.appendDefaultStylesheet(toolRoot);
        String profileFilename = BasicProfileConfig.profileTemplateFilename(WsiProfile.BASIC_PROFILE_11_SOAP_10.name());
        String description = BasicProfileConfig.profileDescription(WsiProfile.BASIC_PROFILE_11_SOAP_10.name());
        Path assertionsFile = BasicProfileConfig.appendProfile(toolRoot, profileFilename);
        return tool.generateBindingConfigFileFromTemplate(template, toolRoot, wsdl.toString(), 0, config, report,
                stylesheet, assertionsFile, description);
    }

    /**
     *
     * @param toolJar path to wsi-checker jar file
     * @param root directory toolJar is extracted to; will contain directory hierarchy of unpacked toolJar
     * @return true if child process terminated normally; false in case of abnormal termination or exception
     */
    public static boolean unpackCheckerTool(Path toolJar, Path root) {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", toolJar.toString(),
                "-unpackTool", "-root", root.toString());
        Process process = null;
        try {
            process = builder.start();
            return process.waitFor() == 0;
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     *
     * @param toolJar path to wsi-checker jar file
     * @param toolRoot root directory where wsi checker tool is located
     * @param wsdl path to the wsdl file to generate config file for
     * @param report path to location of report file generated by wsi analyzer
     * @param config path to location of config file generated by this method
     * @return true if child process terminated normally; false in case of abnormal termination or exception
     */
    public static boolean generateConfigurationFile(Path toolJar, Path toolRoot, Path wsdl, Path report, Path config) {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", toolJar.toString(),
                "-generateConfig", "-root", toolRoot.toString(), "-wsdl", wsdl.toString(),
                "-report", report.toString(), "-output", config.toString());
        Process process = null;
        try {
            process = builder.start();
            return process.waitFor() == 0;
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     *
     * @param toolJar path to wsi-checker jar file
     * @param toolRoot root directory where wsi checker tool is located
     * @param config path to location of config file specifying wsdl location, binding, and other analysis parameters
     * @param summary path to location of analysis summary file (json)
     * @param collector errors/warnings/info messages from the analysis are added to collector
     * @param verbose if true, then output from the child process is echoed to stdout
     * @return true if child process terminated normally; false in case of abnormal termination or exception
     */
    public static boolean runAnalyzer(Path toolJar, Path toolRoot, Path config, Path summary,
                                  AnalysisInformationCollector collector, boolean verbose) {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", toolJar.toString(),
                "-analyze", "-root", toolRoot.toString(), "-config", config.toString(),
                "-summary", summary.toString());
        boolean success = false;
        Process process = null;
        try {
            process = builder.start();
            InputStream fromProcess = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fromProcess));
            String line;
            while ((line = reader.readLine()) != null) {
                if (verbose) {
                    System.out.println(">>> " + line);
                }
                if (line.startsWith("WSDL analysis completed with status: SUCCESS")) {
                    success = true;
                }
            }
            AnalysisInformationCollector summaryCollector = JSON.std.beanFrom(AnalysisInformationCollector.class,
                    new FileInputStream(summary.toFile()));
            collector.add(summaryCollector);
        } catch (Exception ignore) {
        }
        return success;
    }

    public static boolean runAnalyzer(Path toolJar, Path toolRoot, Path config, Path summary,
                                      AnalysisInformationCollector collector) {
        return runAnalyzer(toolJar, toolRoot, config, summary, collector, false);
    }
}