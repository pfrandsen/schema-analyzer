package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PortTypeCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "porttype");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidName() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-valid.wsdl").toFile()));
        PortTypeChecker.checkName(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidCardinality() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-valid.wsdl").toFile()));
        PortTypeChecker.checkCardinality(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidServiceName() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-valid.wsdl").toFile()));
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("First");
        PortBindingNameChecker.checkPortTypeServiceName(serviceNames, wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidName() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-invalid.wsdl").toFile()));
        PortTypeChecker.checkName(wsdl, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Name in wsdl:portType is invalid; must be upper camel case ascii",
                collector.getErrors().get(0).getMessage());
        assertEquals("Name in wsdl:portType is invalid; must end with \"Service\"",
                collector.getErrors().get(1).getMessage());
        assertEquals("WSDL must include exactly one portType element", collector.getErrors().get(2).getMessage());
        assertEquals("Found [firstService,Second]", collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testInvalidCardinality() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-invalid.wsdl").toFile()));
        PortTypeChecker.checkCardinality(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("WSDL must include exactly one portType element", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidServiceName() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-invalid.wsdl").toFile()));
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("First");
        PortBindingNameChecker.checkPortTypeServiceName(serviceNames, wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("No matching wsdl:portType name for service 'First'", collector.getErrors().get(0).getMessage());
    }

}
