package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class DocumentationCheckerTest {
    private static Path RELATIVE_PATH_WSDL = Paths.get("src", "test", "resources", "wsdl", "documentation");
    private static Path RELATIVE_PATH_SXD = Paths.get("src", "test", "resources", "xsd", "documentation");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidChars() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-chars.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Danish characters found", collector.getErrors().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getErrors().get(0).getDetails());
        assertEquals("Non ASCII characters found", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidDanishChars() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-danish-chars.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("Danish characters found", collector.getErrors().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidOnlyWhitespace() throws Exception{
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-only-whitespace.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(1).getSeverity());
        assertEquals("No documentation", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
        assertEquals("No documentation", collector.getWarnings().get(1).getMessage());
        assertEquals("Element: portType 'EntityService' operation 'getEntity'",
                collector.getWarnings().get(1).getDetails());
    }

    @Test
    public void testInvalidNotPresent() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-not-present.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(1).getSeverity());
        assertEquals("No documentation", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
        assertEquals("No documentation", collector.getWarnings().get(1).getMessage());
        assertEquals("Element: portType 'EntityService' operation 'getEntity'",
                collector.getWarnings().get(1).getDetails());
    }

    @Test
    public void testInvalidTooLong() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-too-long.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Documentation exceed limit (700)", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidTooLongOperation() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-too-long-operation.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Documentation exceed limit (700)", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: portType 'EntityService' operation 'getEntity'",
                collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidTooShort() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-too-short.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("Documentation below limit (5)", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidTodo() throws Exception {
        Path path = RELATIVE_PATH_WSDL.resolve("Documentation-invalid-todo.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(0).getSeverity());
        assertEquals("TODO found", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: WSDL element (top level)", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testSchemaValid() throws Exception {
        Path path = RELATIVE_PATH_SXD.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkConceptSchemaDocumentation(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testSchemaInvalidDanishChars() throws Exception {
        Path path = RELATIVE_PATH_SXD.resolve("invalid-danish-characters.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkConceptSchemaDocumentation(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Danish characters found", collector.getErrors().get(0).getMessage());
        assertEquals("Element: 'ElementNameTwo'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testSchemaInvalidCharsAndEmpty() throws Exception {
        Path path = RELATIVE_PATH_SXD.resolve("invalid-characters-and-empty.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        DocumentationChecker.checkConceptSchemaDocumentation(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Danish characters found", collector.getErrors().get(0).getMessage());
        assertEquals("Element: 'ElementNameOne'", collector.getErrors().get(0).getDetails());
        assertEquals("Non ASCII characters found", collector.getWarnings().get(0).getMessage());
        assertEquals("Element: 'ElementNameOne'", collector.getWarnings().get(0).getDetails());
        assertEquals("No documentation", collector.getWarnings().get(1).getMessage());
        assertEquals("Element: 'ElementNameTwo'", collector.getWarnings().get(1).getDetails());
    }
}
