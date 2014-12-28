package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SoapBindingCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "binding");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidVersionedServiceName() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-valid-versioned-service-name.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidRpcStyle() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-rpc-style.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        //assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        //assertEquals("Binding is not Document/Literal", collector.getErrors().get(0).getMessage());
        assertEquals("SOAP binding style is not document", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidTransport() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-transport.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("SOAP binding transport is not http://schemas.xmlsoap.org/soap/http", collector.getWarnings().get(0).getMessage());
        assertEquals("SOAP transport found [http://example.com/smtp]", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidSoap12() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-soap12.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding is not SOAP 1.1", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidMultipleBindings() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-multiple-bindings.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(1).getSeverity());
        assertEquals("WSDL contains more than one binding", collector.getErrors().get(0).getMessage());
        assertEquals("Binding name is invalid", collector.getErrors().get(1).getMessage());
        assertEquals("Binding name SecondBinding should be <serviceName>Binding where service name is {Entity}", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testInvalidNoBinding() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-no-binding.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals("WSDL does not contain binding", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidName() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-name.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding name is invalid", collector.getErrors().get(0).getMessage());
        assertEquals("Binding name InvalidEntityBinding should be <serviceName>Binding where service name is {Entity}", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidNamespacePrefix() throws Exception {
        Path path = RELATIVE_PATH.resolve("Binding-invalid-namespace-prefix.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("WSDL binding 'EntityBinding' type does not use tns namespace prefix", collector.getWarnings().get(0).getMessage());
        assertEquals("Prefix used is 'tnsinvalid'", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidEncoded() {
        //Path path = RELATIVE_PATH.resolve("Binding-invalid-encoded.wsdl");
        // TODO: Check this wsdl with WS-I - that test checks for Document/Literal
        /* String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        SoapBindingChecker.checkBindings(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding is not Document/Literal", collector.getErrors().get(0).getMessage());
        assertEquals("Binding detected as [Document/Encoded]", collector.getErrors().get(0).getDetails());
        */
    }
}