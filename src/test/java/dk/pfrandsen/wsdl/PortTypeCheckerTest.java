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

    @Test
    public void testValidMessages() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("PortType-valid-messages-simple.wsdl").
                toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidMessagesInputPostfix() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.
                resolve("PortType-invalid-messages-input-simple.wsdl").toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Invalid input message name", collector.getErrors().get(0).getMessage());
        assertEquals("Input message in portType 'FirstService' operation 'operationTwo' does not end with 'Request'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidMessagesOutputPostfix() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.
                resolve("PortType-invalid-messages-output-simple.wsdl").toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Invalid output message name", collector.getErrors().get(0).getMessage());
        assertEquals("Output message in portType 'SecondService' operation 'operationOne' does not end with 'Response'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidMessagesFaultPostfix() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.
                resolve("PortType-invalid-messages-fault-simple.wsdl").toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Invalid fault message name", collector.getErrors().get(0).getMessage());
        assertEquals("Fault message 'firstFlt' in portType 'FirstService' operation 'operationTwo' does not end with " +
                "'Fault'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidMessagesFaultName() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.
                resolve("PortType-invalid-messages-fault-name-simple.wsdl").toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Invalid fault name", collector.getErrors().get(0).getMessage());
        assertEquals("Fault name 'firstFlt' in portType 'FirstService' operation 'operationOne' must be equal to " +
                "message name 'firstFault'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidMessagesNoFault() throws Exception {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.
                resolve("PortType-invalid-messages-no-fault-simple.wsdl").toFile()));
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Missing fault", collector.getWarnings().get(0).getMessage());
        assertEquals("Fault not defined for portType 'FirstService', operation 'operationOne'",
                collector.getWarnings().get(0).getDetails());
    }
}
