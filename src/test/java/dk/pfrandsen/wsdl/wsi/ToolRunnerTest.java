package dk.pfrandsen.wsdl.wsi;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.Utilities;
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

    @Before
    public void setUp() {
        outputPath = Paths.get("target", "tool-runner");
        dk.pfrandsen.util.Utilities.mkDirs(outputPath);
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

}
