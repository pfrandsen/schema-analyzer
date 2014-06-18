package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SchemaTypesCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "types");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Types-valid.wsdl").toFile()));
        SchemaTypesChecker.checkSchemaTypes(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalid() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Types-invalid.wsdl").toFile()));
        SchemaTypesChecker.checkSchemaTypes(wsdl, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidElements() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Types-invalid.wsdl").toFile()));
        SchemaTypesChecker.checkElements(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("wsdl:types/xsd:schema contains element 'GetEntityRequest'",
                collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidComplexTypes() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Types-invalid.wsdl").toFile()));
        SchemaTypesChecker.checkComplexTypes(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("wsdl:types/xsd:schema contains complexType 'complex'",
                collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidSimpleTypes() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Types-invalid.wsdl").toFile()));
        SchemaTypesChecker.checkSimpleTypes(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("wsdl:types/xsd:schema contains simpleType 'simple'",
                collector.getErrors().get(0).getMessage());
    }

}


