package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DocumentationCheckerTest {
    private static String RELATIVE_PATH = "src/test/resources/wsdl/documentation";
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
        String uri = fileUri + "/Documentation-valid.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidChars() {
        String uri = fileUri + "/Documentation-invalid-chars.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Danish characters found in WSDL element (top level)", collector.getErrors().get(0).getMessage());
        assertEquals("Non ASCII characters found in WSDL element (top level)", collector.getWarnings().get(0).getMessage());

    }

    @Test
    public void testInvalidDanishChars() {
        String uri = fileUri + "/Documentation-invalid-danish-chars.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Danish characters found in WSDL element (top level)", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidOnlyWhitespace() {
        String uri = fileUri + "/Documentation-invalid-only-whitespace.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(1).getSeverity());
        assertEquals("No documentation for WSDL element (top level)", collector.getWarnings().get(0).getMessage());
        assertEquals("No documentation for portType [EntityService] operation [getEntity]", collector.getWarnings().get(1).getMessage());
    }

    @Test
    public void testInvalidNotPresent() {
        String uri = fileUri + "/Documentation-invalid-not-present.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(1).getSeverity());
        assertEquals("No documentation for WSDL element (top level)", collector.getWarnings().get(0).getMessage());
        assertEquals("No documentation for portType [EntityService] operation [getEntity]", collector.getWarnings().get(1).getMessage());
    }

    @Test
    public void testInvalidTooLong() {
        String uri = fileUri + "/Documentation-invalid-too-long.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Documentation for WSDL element (top level) exceed limit (700)", collector.getWarnings().get(0).getMessage());
    }

    @Test
    public void testInvalidTooLongOperation() {
        String uri = fileUri + "/Documentation-invalid-too-long-operation.wsdl";
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        DocumentationChecker.checkDocumentation(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Documentation for portType [EntityService] operation [getEntity] exceed limit (700)", collector.getWarnings().get(0).getMessage());
    }
}
