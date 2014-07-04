package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class BindingCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "binding");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidSoapAction() throws Exception{
        Path path = RELATIVE_PATH.resolve("Binding-valid-soap-action-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkSoapAction(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidSoapActionEmptySoapAction() throws Exception{
        Path path = RELATIVE_PATH.resolve("Binding-invalid-soap-action-empty-action-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkSoapAction(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Missing or empty soapAction", collector.getErrors().get(0).getMessage());
        assertEquals("Binding 'FirstBinding', operation 'opTwo'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidSoapActionMissingPortTypeOperation() throws Exception{
        Path path = RELATIVE_PATH.resolve("Binding-invalid-soap-action-missing-porttype-operation-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkSoapAction(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("No matching portType operation found", collector.getErrors().get(0).getMessage());
        assertEquals("Binding 'FirstBinding', operation 'opTwo'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidSoapActionInvalidUri() throws Exception{
        Path path = RELATIVE_PATH.resolve("Binding-invalid-soap-action-invalid-uri-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkSoapAction(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Invalid soapAction", collector.getErrors().get(0).getMessage());
        assertEquals("Binding 'FirstBinding', operation 'opOne', expected soapAction "+
                "'http://service.schemas/domain/service/v1/opOneRequest' " +
                "(found 'http://service.schemas/domain/service/v1/opOneResponse')",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidOperationFaults() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-valid-operation-faults-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkFaults(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidOperationFaultsName() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-operation-faults-name-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Fault name and soap fault name differ", collector.getErrors().get(0).getMessage());
        assertEquals("Binding 'FirstBinding', operation 'opOne', fault 'faultNameB' <> soap fault 'faultNameBad'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidOperationFaultsUse() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-operation-faults-use-simple.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        BindingChecker.checkFaults(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Fault use attribute not \"literal\"", collector.getErrors().get(0).getMessage());
        assertEquals("Binding 'FirstBinding', operation 'opTwo', fault 'faultNameC' use='encoded'",
                collector.getErrors().get(0).getDetails());
    }
}
