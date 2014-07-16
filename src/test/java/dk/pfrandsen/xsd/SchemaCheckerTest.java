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
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "xsd");
    private static Path RELATIVE_PATH_FORM_DEFAULT = RELATIVE_PATH.resolve("formDefault");
    private static Path RELATIVE_PATH_NILLABLE = RELATIVE_PATH.resolve("nillable");
    private static Path RELATIVE_PATH_MIN_MAX = RELATIVE_PATH.resolve("minMaxOccurs");
    private static Path RELATIVE_PATH_TYPES = RELATIVE_PATH.resolve("types");
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

    @Test
    public void testInvalidEmbeddedTypes()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-embedded-types.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTypes(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Embedded (anonymous) type found", collector.getErrors().get(0).getMessage());
        assertEquals("Node 'ElementName' (element)", collector.getErrors().get(0).getDetails());
        assertEquals("Embedded (anonymous) type found", collector.getErrors().get(1).getMessage());
        assertEquals("Node 'AccountType' (complexType)", collector.getErrors().get(1).getDetails());
        assertEquals("Embedded (anonymous) type found", collector.getErrors().get(2).getMessage());
        assertEquals("Node 'AccountCategoryType' (complexType)", collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testInvalidTypeNameCase()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-type-name-case.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTypes(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal type name", collector.getErrors().get(0).getMessage());
        assertEquals("Type 'balanceType' (simpleType)", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTypeNameChars()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-type-name-chars.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTypes(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal type name", collector.getErrors().get(0).getMessage());
        assertEquals("Type 'Account_Type' (complexType)", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTypeNameDanishChars()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-type-name-danish-chars.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTypes(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal type name", collector.getErrors().get(0).getMessage());
        assertEquals("Type 'AccåntType' (complexType)", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTypeNamePostfix()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-type-name-postfix.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTypes(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal type name", collector.getErrors().get(0).getMessage());
        assertEquals("Type 'Account' (complexType)", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidConceptContentImportInclude()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-concept-import-include.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkConceptTypes(xsd, collector);
        assertEquals(4, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(0).getMessage());
        assertEquals("Node 'import' (http://domain.net/domain/v1)", collector.getErrors().get(0).getDetails());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(1).getMessage());
        assertEquals("Node 'import' (http://domain.net/domain/v2)", collector.getErrors().get(1).getDetails());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(2).getMessage());
        assertEquals("Node 'include' (http://location/1)", collector.getErrors().get(2).getDetails());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(3).getMessage());
        assertEquals("Node 'include' (http://location/2)", collector.getErrors().get(3).getDetails());
    }

    @Test
    public void testInvalidConceptContentTypes()throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-concept-types.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkConceptTypes(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(0).getMessage());
        assertEquals("Node 'AccountCategoryType' (complexType)", collector.getErrors().get(0).getDetails());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(1).getMessage());
        assertEquals("Node 'SomeSimpleType' (simpleType)", collector.getErrors().get(1).getDetails());
        assertEquals("Illegal content in concept schema", collector.getErrors().get(2).getMessage());
        assertEquals("Node 'ComplexElement' (element)", collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testInvalidBetaNamespace() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-beta-namespace.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkBetaNamespace(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Namespace containing 'beta-' found", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://beta-domain.net/domain/v1'", collector.getErrors().get(0).getDetails());
        assertEquals("Namespace containing 'beta-' found", collector.getErrors().get(1).getMessage());
        assertEquals("Namespace 'http://beta-domain.net/domain/v2'", collector.getErrors().get(1).getDetails());
        assertEquals("Namespace containing 'beta-' found", collector.getErrors().get(2).getMessage());
        assertEquals("Namespace 'http://beta-domain.net/domain/v3'", collector.getErrors().get(2).getDetails());
    }

}
