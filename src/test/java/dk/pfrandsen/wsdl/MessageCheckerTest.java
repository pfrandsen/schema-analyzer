package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MessageCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "message");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidNames() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-valid-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidParts() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-valid-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageParts(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidElementMessageName() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-invalid-element-message-name-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageParts(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Element name (in lowerCamelCase) must match message name",
                collector.getErrors().get(0).getMessage());
        assertEquals("Message 'getDataRequest' part 'getSomeDataRequest' element 'GetSomeDataRequest'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidFaultPart() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-invalid-fault-part-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageParts(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Element name (in lowerCamelCase) must match part name",
                collector.getErrors().get(0).getMessage());
        assertEquals("Message 'uncategorizedFault' part 'parameters' element 'UncategorizedFault'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidHeaderPart() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-invalid-header-part-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageParts(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Element name (in lowerCamelCase) must match part name",
                collector.getErrors().get(0).getMessage());
        assertEquals("Message 'applications' part 'parameters' element 'Applications'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidUsesType() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-invalid-uses-type-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageParts(wsdl, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Message part must not use type, use element", collector.getErrors().get(0).getMessage());
        assertEquals("Found type 'GetDataRequest' in part 'getDataRequest' of message 'getDataRequest'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Message part must include element", collector.getErrors().get(1).getMessage());
        assertEquals("No element in part 'getDataRequest' of message 'getDataRequest'",
                collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testUnknownFaultPart() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-unknown-fault-part-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unknown fault message", collector.getWarnings().get(0).getMessage());
        assertEquals("Message 'customFault', [validationFault,notFoundFault,staleDataFault,uncategorizedFault]",
                collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testUnknownHeaderPart() throws Exception {
        Path path = RELATIVE_PATH.resolve("Message-unknown-header-part-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        MessageChecker.checkMessageNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unknown header message", collector.getWarnings().get(0).getMessage());
        assertEquals("Message 'customHeader', [applications,user,proxy,serviceConsumer,filtering,logging," +
                "requestorIdentity]", collector.getWarnings().get(0).getDetails());
    }

}
