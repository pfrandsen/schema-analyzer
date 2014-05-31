package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class WsdlNameCheckerTest {
    private static String RELATIVE_PATH = "src/test/resources/wsdl/name";
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
        String uri = fileUri + "/Name-valid.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
    }

    @Test
    public void testValidNoName() {
        String uri = fileUri + "/Name-valid-no-name.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
    }

    @Test
    public void testValidWithVersion() {
        String uri = fileUri + "/Name-valid-with-version.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(0, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
    }

    @Test
    public void testInvalidCaps() {
        String uri = fileUri + "/Name-invalid-caps.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag is invalid; must be upper camel case ascii", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidChars() {
        String uri = fileUri + "/Name-invalid-chars.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(0, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag is invalid; must be upper camel case ascii", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidWithVersion() {
        String uri = fileUri + "/Name-invalid-with-version.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        WsdlNameChecker.checkName(definition, collector);
        assertEquals(1, collector.getErrorCount());
        assertEquals(1, collector.getWarningCount());
        assertEquals(0, collector.getInfoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Name in wsdl:definitions start tag contains version information", collector.getErrors().get(0).getMessage());
        assertEquals("Name in wsdl:definitions start tag does not match service name", collector.getWarnings().get(0).getMessage());
    }
}
