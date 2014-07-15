package dk.pfrandsen.xsd;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SchemaCheckerTest {
    private static Path RELATIVE_PATH_FORM_DEFAULT = Paths.get("src", "test", "resources", "xsd", "formDefault");
    private static Path RELATIVE_PATH_NILLABLE = Paths.get("src", "test", "resources", "xsd", "nillable");
    private static Path RELATIVE_PATH_MIN_MAX = Paths.get("src", "test", "resources", "xsd", "minMaxOccurs");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidFormDefault()throws Exception {
        Path path = RELATIVE_PATH_FORM_DEFAULT.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkFormDefault(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidFormDefaultElement()throws Exception {
        Path path = RELATIVE_PATH_FORM_DEFAULT.resolve("invalid-element.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkFormDefault(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Value of attribute elementFormDefault must be 'qualified'",
                collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidFormDefaultAttribute()throws Exception {
        Path path = RELATIVE_PATH_FORM_DEFAULT.resolve("invalid-attribute.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkFormDefault(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Value of attribute attributeFormDefault must be 'unqualified'",
                collector.getErrors().get(0).getMessage());
    }


    @Test
    public void testValidNillable()throws Exception {
        Path path = RELATIVE_PATH_NILLABLE.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkNillable(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidNillable()throws Exception {
        Path path = RELATIVE_PATH_NILLABLE.resolve("invalid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkNillable(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Element must must not have nillable attribute", collector.getErrors().get(0).getMessage());
        assertEquals("Element 'ElementName', nillable='false'", collector.getErrors().get(0).getDetails());
        assertEquals("Element must must not have nillable attribute", collector.getErrors().get(1).getMessage());
        assertEquals("Element 'Balance', nillable='true'", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testValidMinMax()throws Exception {
        Path path = RELATIVE_PATH_MIN_MAX.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkMinMaxOccurs(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidMinMax()throws Exception {
        Path path = RELATIVE_PATH_MIN_MAX.resolve("invalid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkMinMaxOccurs(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Redundant minOccurs/maxOccurs='1'", collector.getErrors().get(0).getMessage());
        assertEquals("Node '<anonymous>' (sequence)", collector.getErrors().get(0).getDetails());
        assertEquals("Redundant minOccurs/maxOccurs='1'", collector.getErrors().get(1).getMessage());
        assertEquals("Node 'Balance' (element)", collector.getErrors().get(1).getDetails());
        assertEquals("Redundant minOccurs/maxOccurs='1'", collector.getErrors().get(2).getMessage());
        assertEquals("Node '<anonymous>' (choice)", collector.getErrors().get(2).getDetails());
    }

}
