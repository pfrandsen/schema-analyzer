package dk.pfrandsen.wsdl.wsi;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.Utilities;
import dk.pfrandsen.util.WsiUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToolRunnerTest {
    private Path outputPath;
    private Path toolJar;
    private Path toolRoot;

    @Before
    public void setUp() {
        outputPath = Paths.get("target", "tool-runner");
        dk.pfrandsen.util.Utilities.mkDirs(outputPath);
        toolJar = Paths.get("lib", "wsi-checker-1.0-SNAPSHOT.jar");
        toolRoot = outputPath.resolve("unpacked");
    }

    @After
    public void cleanUp() {
        if (outputPath.toString().startsWith("target")) {
            Utilities.deleteFolder(outputPath);
        }
    }

    @Test
    public void testRunner() throws IOException {
        Path summary = outputPath.resolve(Paths.get("wsi-summary.json"));
        Path jar = Paths.get("lib", "wsi-checker-1.0-SNAPSHOT.jar");
        Path root = Paths.get("target", "test-classes", "wsdl", "wsi");
        Path config = root.resolve("config.xml");
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", jar.toString(),
                "-analyze", "-root", root.toString(),
                "-config", config.toString(),
                "-summary", summary.toString());
        Process process = builder.start();
        InputStream fromProcess = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(fromProcess));
        String line;
        boolean success = false;
        while ((line = reader.readLine()) != null) {
            System.out.println(">>> " + line);
            if (line.startsWith("WSDL analysis completed with status: SUCCESS")) {
                success = true;
            }
        }
        System.out.println(success ? "Success" : "failure");
        assertTrue(success);
        // read summary file
        AnalysisInformationCollector collector = JSON.std.beanFrom(AnalysisInformationCollector.class,
                new FileInputStream(summary.toFile()));
        assertEquals(collector.errorCount(), 0);
        assertEquals(collector.warningCount(), 0);
        assertEquals(collector.infoCount(), 1);
        assertEquals("Analyzer status code 0", collector.getInfo().get(0).getMessage());
    }

    @Test
    public void testUnpackTool() throws IOException {
        boolean status = WsiUtil.unpackCheckerTool(toolJar, toolRoot);
        assertTrue("Expected unpack process to return true (successful run)", status);
        Path toolLocalRoot = toolRoot.resolve("common");
        assertTrue("Tool root '" + toolLocalRoot + "' directory not found.", toolLocalRoot.toFile().isDirectory());
        Path subDir = toolLocalRoot.resolve("profiles");
        assertTrue("'" + subDir + "' directory not found.", subDir.toFile().isDirectory());
        subDir = toolLocalRoot.resolve("schemas");
        assertTrue("'" + subDir + "' directory not found.", subDir.toFile().isDirectory());
        subDir = toolLocalRoot.resolve("xsl");
        assertTrue("'" + subDir + "' directory not found.", subDir.toFile().isDirectory());
    }

    @Test
    public void testGenerateConfigFile() throws IOException {
        boolean status = WsiUtil.unpackCheckerTool(toolJar, toolRoot);
        assertTrue("Expected unpack process to return true (successful run)", status);
        // now the tool needed while generating the config file is available, so generate config file
        Path wsdl = Paths.get("target", "test-classes", "wsdl", "wsi", "wsdl", "wsdl_1.wsdl");
        Path report = outputPath.resolve(WsiUtil.REPORT_FILENAME);
        Path config = outputPath.resolve("cfg").resolve(WsiUtil.CONFIG_FILENAME);
        status = WsiUtil.generateConfigurationFile(toolJar, toolRoot, wsdl, report, config);
        assertTrue("Expected config generator to return true (successful run)", status);
        assertTrue("config file '" + config + "' not found.", config.toFile().exists());
    }

    @Test
    public void testGenerateConfigFileAndRunAnalyzer() throws IOException {
        boolean status = WsiUtil.unpackCheckerTool(toolJar, toolRoot);
        assertTrue("Expected unpack process to return true (successful run)", status);
        // now the tool needed while generating config file and running analysis is available, so generate config file
        Path wsdl = Paths.get("target", "test-classes", "wsdl", "wsi", "wsdl", "wsdl_1.wsdl");
        Path relOutputPath = Paths.get("rel", "path");
        Path report = outputPath.resolve(relOutputPath).resolve(WsiUtil.REPORT_FILENAME);
        Path config = outputPath.resolve(relOutputPath).resolve(WsiUtil.CONFIG_FILENAME);
        status = WsiUtil.generateConfigurationFile(toolJar, toolRoot, wsdl, report, config);
        assertTrue("Expected config generator to return true (successful run)", status);
        assertTrue("config file '" + config + "' not found.", config.toFile().exists());
        // tool and config file ready, so run the analyzer
        Path summary = outputPath.resolve(relOutputPath).resolve(WsiUtil.SUMMARY_FILENAME);
        AnalysisInformationCollector collector = new AnalysisInformationCollector();
        boolean analysisStatus = WsiUtil.runAnalyzer(toolJar, toolRoot, config, summary, collector);
        assertTrue("Expected wsi analyzer to return true (successful run)", analysisStatus);
        assertTrue("report file '" + report + "' not found.", report.toFile().exists());
        assertTrue("summary file '" + summary + "' not found.", summary.toFile().exists());
        assertEquals(collector.errorCount(), 0);
        assertEquals(collector.warningCount(), 0);
        assertEquals(collector.infoCount(), 1);
        assertEquals("Analyzer status code 0", collector.getInfo().get(0).getMessage());
    }

}
