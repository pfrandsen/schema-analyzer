package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ServiceCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "service");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidService() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        ServiceChecker.checkServices(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidServiceWithVersion() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid-with-version.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        ServiceChecker.checkServices(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Version postfix found in wsdl:service name", collector.getWarnings().get(0).getMessage());
        assertEquals("Name: 'FirstV1'", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidServiceCamelCase() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-invalid-camelcase.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        ServiceChecker.checkServices(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Name in wsdl:service is not upper camel case", collector.getErrors().get(0).getMessage());
        assertEquals("Name: 'firstSvc'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidFilename() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        Path filePath = Paths.get("First.wsdl");
        ServiceChecker.checkServiceFileName(filePath, wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidFilenameExtensionCase() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        Path filePath = Paths.get("First.Wsdl");
        ServiceChecker.checkServiceFileName(filePath, wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("WSDL filename does not end with .wsdl", collector.getErrors().get(0).getMessage());
        assertEquals("Filename: First.Wsdl", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidFilenameExtension() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        Path filePath = Paths.get("First.wsl");
        ServiceChecker.checkServiceFileName(filePath, wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("WSDL filename does not end with .wsdl", collector.getErrors().get(0).getMessage());
        assertEquals("Filename: First.wsl", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidFilenameWithVersion() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid-with-version.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        Path filePath = Paths.get("some", "path", "First.wsdl");
        ServiceChecker.checkServiceFileName(filePath, wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidFilenameWithVersion() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid-with-version.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        Path filePath = Paths.get("some", "path", "FirstV1.wsdl");
        ServiceChecker.checkServiceFileName(filePath, wsdl, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("WSDL filename contains version number", collector.getErrors().get(0).getMessage());
        assertEquals("Filename: FirstV1", collector.getErrors().get(0).getDetails());
        assertEquals("Service name does not match filename", collector.getErrors().get(1).getMessage());
        assertEquals("Filename: FirstV1, service name: 'First' (FirstV1)", collector.getErrors().get(1).getDetails());
    }

}
