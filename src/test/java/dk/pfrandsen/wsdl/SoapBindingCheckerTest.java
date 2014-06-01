package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoapBindingCheckerTest {
    private static String RELATIVE_PATH = "src/test/resources/wsdl/binding";
    private String fileUri;
    private AnalysisInformationCollector collector;
    private WSDLParser parser;

    @Before
    public void setUp() {
        fileUri = new File(RELATIVE_PATH).toURI().toString();
        collector = new AnalysisInformationCollector();
        parser = new WSDLParser();
    }

    @Test
    public void testValid() {
        String uri = fileUri + "/Binding-valid.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
    }

    @Test
    public void testValidVersionedServiceName() {
        String uri = fileUri + "/Binding-valid-versioned-service-name.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
    }

    @Test
    public void testInvalidRpcStyle() {
        String uri = fileUri + "/Binding-invalid-rpc-style.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(1, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Binding is not Document/Literal", collector.getErrors().get(0).getMessage());
        assertEquals("SOAP binding style is not document", collector.getWarnings().get(0).getMessage());
    }

    @Test
    public void testInvalidTransport() {
        String uri = fileUri + "/Binding-invalid-transport.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(1, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("SOAP binding transport is not http://schemas.xmlsoap.org/soap/http", collector.getWarnings().get(0).getMessage());
        assertEquals("SOAP transport found [http://example.com/smtp]", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidSoap12() {
        String uri = fileUri + "/Binding-invalid-soap12.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding is not SOAP 1.1", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidMultipleBindings() {
        String uri = fileUri + "/Binding-invalid-multiple-bindings.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(2, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(1).getSeverity());
        assertEquals("WSDL contains more than one binding", collector.getErrors().get(0).getMessage());
        assertEquals("Binding name is invalid", collector.getErrors().get(1).getMessage());
        assertEquals("Binding name SecondBinding should be <serviceName>Binding where service name is {Entity}", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testInvalidNoBinding() {
        String uri = fileUri + "/Binding-invalid-no-binding.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals("WSDL does not contain binding", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidName() {
        String uri = fileUri + "/Binding-invalid-name.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding name is invalid", collector.getErrors().get(0).getMessage());
        assertEquals("Binding name InvalidEntityBinding should be <serviceName>Binding where service name is {Entity}", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidNamespacePrefix() {
        String uri = fileUri + "/Binding-invalid-namespace-prefix.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(1, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("WSDL binding type does not use tns namespace prefix", collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace prefix used 'tnsinvalid'", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidEncoded() {
        String uri = fileUri + "/Binding-invalid-encoded.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        SoapBindingChecker.checkBindings(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, collector.getErrors().get(0).getSeverity());
        assertEquals("Binding is not Document/Literal", collector.getErrors().get(0).getMessage());
        assertEquals("Binding detected as [Document/Encoded]", collector.getErrors().get(0).getDetails());
    }
}