package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class WsdlNameCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "name");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidNoName() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-valid-no-name.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidWithVersion() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-valid-with-version.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidCaps() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-invalid-caps.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag is invalid; must be upper camel case ascii", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidChars() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-invalid-chars.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag is invalid; must be upper camel case ascii", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidWithVersion() throws Exception {
        Path path = RELATIVE_PATH.resolve("Name-invalid-with-version.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        WsdlNameChecker.checkName(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag contains version information", collector.getErrors().get(0).getMessage());
        assertEquals("Name in wsdl:definitions start tag does not match service name", collector.getWarnings().get(0).getMessage());
    }
}
