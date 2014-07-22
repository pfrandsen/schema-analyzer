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
    public void testValidFormDefault() throws Exception {
        Path path = RELATIVE_PATH_FORM_DEFAULT.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkFormDefault(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidFormDefaultElement() throws Exception {
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
    public void testInvalidFormDefaultAttribute() throws Exception {
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
    public void testValidNillable() throws Exception {
        Path path = RELATIVE_PATH_NILLABLE.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkNillable(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidNillable() throws Exception {
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
    public void testValidMinMax() throws Exception {
        Path path = RELATIVE_PATH_MIN_MAX.resolve("valid.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkMinMaxOccurs(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidMinMax() throws Exception {
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
    public void testInvalidEmbeddedTypes() throws Exception {
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
    public void testInvalidTypeNameCase() throws Exception {
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
    public void testInvalidTypeNameChars() throws Exception {
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
    public void testInvalidTypeNameDanishChars() throws Exception {
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
    public void testInvalidTypeNamePostfix() throws Exception {
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
    public void testInvalidConceptContentImportInclude() throws Exception {
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
    public void testInvalidConceptContentTypes() throws Exception {
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
    public void testValidConceptEnumUsed() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-concept-used-enum.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkConceptTypes(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidConceptEnumNotUsed() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-concept-unused-enum.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkConceptTypes(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unused enumeration in concept schema", collector.getErrors().get(0).getMessage());
        assertEquals("Enumeration 'CatEnumType'", collector.getErrors().get(0).getDetails());
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

    @Test
    public void testValidElementName() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-element-name.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkElements(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidElementName() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-element-name.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkElements(xsd, collector);
        assertEquals(4, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal element name", collector.getErrors().get(0).getMessage());
        assertEquals("Element 'elementWithWrongCaps'", collector.getErrors().get(0).getDetails());
        assertEquals("Illegal element name", collector.getErrors().get(1).getMessage());
        assertEquals("Element 'Søgestreng'", collector.getErrors().get(1).getDetails());
        assertEquals("Illegal element name", collector.getErrors().get(2).getMessage());
        assertEquals("Element 'Element-Name'", collector.getErrors().get(2).getDetails());
        assertEquals("Illegal element name", collector.getErrors().get(3).getMessage());
        assertEquals("Element 'Element_Name'", collector.getErrors().get(3).getDetails());
    }

    @Test
    public void testValidEnumValues() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-enum-values.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkEnumerationValues(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidEnumValues() throws Exception {
        String err = "Illegal enumeration value";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-enum-values.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkEnumerationValues(xsd, collector);
        assertEquals(5, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("simpleType:CategoryEnumType, value ' val1'", collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("simpleType:CategoryEnumType, value ''", collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("simpleType:CategoryEnumType, value '3 val'", collector.getErrors().get(2).getDetails());
        assertEquals(err, collector.getErrors().get(3).getMessage());
        assertEquals("simpleType:CategoryEnumType, value 'Val æøå'", collector.getErrors().get(3).getDetails());
        assertEquals(err, collector.getErrors().get(4).getMessage());
        assertEquals("element:SomeElement, value 'Val - value'", collector.getErrors().get(4).getDetails());
    }

    @Test
    public void testValidSimpleTypes() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-concept-simple-type.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSimpleTypesInConcept(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidSimpleTypes() throws Exception {
        String err = "Simple type must be in concept schema";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-concept-simple-type.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSimpleTypesInConcept(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("Type 'CategoryEnumType'", collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("Type 'CatEnumType'", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testValidServiceElementDefinition() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-service-element-type.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkServiceElementDefinition(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidServiceElementDefinition() throws Exception {
        String err = "Illegal element definition";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-service-element-type.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkServiceElementDefinition(xsd, collector);
        assertEquals(6, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("Element 'ElementOne': Illegal namespace 'http://www.w3.org/2001/XMLSchema'",
                collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("Element 'ElementTwo': Illegal namespace 'http://service.net/bankservice/v2'",
                collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("Element 'UnconstrainedElement': Unconstrained without content",
                collector.getErrors().get(2).getDetails());
        assertEquals(err, collector.getErrors().get(3).getMessage());
        assertEquals("Node 'complexType:Type2Type', element 'Service': Illegal namespace " +
                "'http://service.net/bankservice/v2'", collector.getErrors().get(3).getDetails());
        assertEquals(err, collector.getErrors().get(4).getMessage());
        assertEquals("Node 'complexType:Type2Type', element 'StringElement': Illegal namespace " +
                "'http://www.w3.org/2001/XMLSchema'", collector.getErrors().get(4).getDetails());
        assertEquals(err, collector.getErrors().get(5).getMessage());
        assertEquals("Node 'element:IllegalConstrainedElement', element 'IntElement': Illegal namespace " +
                "'http://www.w3.org/2001/XMLSchema'", collector.getErrors().get(5).getDetails());
    }

    @Test
    public void testInvalidRedefinition() throws Exception {
        String err = "Illegal redefinition";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-redefine.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkRedefinition(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("schemaLocation 'location1.xsd'", collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("schemaLocation 'location2.xsd'", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testValidSchemaUsage() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-namespace-import.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSchemaUse(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidProcessSchemaUsage() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-namespace-import-process.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSchemaUse(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidProcessSchemaUsage() throws Exception {
        String err = "Illegal process namespace usage";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-namespace-import-process.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSchemaUse(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("Imported namespace 'http://process.schemas.nykreditnet.net/abc'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidSchemaUsage() throws Exception {
        String err = "Illegal namespace usage";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-namespace-import.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkSchemaUse(xsd, collector);
        assertEquals(4, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("Imported namespace 'http://domain.net/domain/v2'", collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("Imported namespace 'http://domain.net/myconcept/v1'", collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("Imported namespace 'http://simpletype.schemas.nykreditnet.net'",
                collector.getErrors().get(2).getDetails());
        assertEquals(err, collector.getErrors().get(3).getMessage());
        assertEquals("Imported namespace 'http://technical.schemas.nykreditnet.net'",
                collector.getErrors().get(3).getDetails());
    }

    @Test
    public void testValidTargetNamespaceV1() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-tns-v1.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTargetNamespaceVersion(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidTargetNamespaceV10() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-tns-v10.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTargetNamespaceVersion(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    private void targetNamespaceHelper(String schemaName) throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve(schemaName);
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkTargetNamespaceVersion(xsd, collector);
        assertEquals("Schema " + schemaName, 1, collector.errorCount());
        assertEquals("Schema " + schemaName, 0, collector.warningCount());
        assertEquals("Schema " + schemaName, 0, collector.infoCount());
        assertEquals("Target namespace must end with version (1..)", collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidTargetNamespace1() throws Exception {
        targetNamespaceHelper("invalid-tns-1.xsd");
        // caps (/V1)
        assertEquals("Target namespace 'http://service.schemas.nykreditnet.net/domain/V1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace2() throws Exception {
        targetNamespaceHelper("invalid-tns-2.xsd");
        // missing number after /v
        assertEquals("Target namespace 'http://service.schemas.nykreditnet.net/domain/v'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace3() throws Exception {
        targetNamespaceHelper("invalid-tns-3.xsd");
        // version zero /v0
        assertEquals("Target namespace 'http://concept.schemas.nykreditnet.net/domain/v0'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace4() throws Exception {
        targetNamespaceHelper("invalid-tns-4.xsd");
        // trailing slash (/v1/)
        assertEquals("Target namespace 'http://concept.schemas.nykreditnet.net/domain/v1/'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace5() throws Exception {
        targetNamespaceHelper("invalid-tns-5.xsd");
        // missing slash (_v1)
        assertEquals("Target namespace 'http://technical.schemas.nykreditnet.net/domain_v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace6() throws Exception {
        targetNamespaceHelper("invalid-tns-6.xsd");
        // invalid version number (01)
        assertEquals("Target namespace 'http://technical.schemas.nykreditnet.net/domain/v01'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespace7() throws Exception {
        targetNamespaceHelper("invalid-tns-7.xsd");
        // trailing space
        assertEquals("Target namespace 'http://technical.schemas.nykreditnet.net/domain/v1 '",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidAnyType() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-anytype.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAnyType(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(1, collector.infoCount());
        assertEquals("Use of anyType (permitted)", collector.getInfo().get(0).getMessage());
        assertEquals("element Payload namespace 'http://service.schemas.nykreditnet.net/customer/case/concept/task/v1'",
                collector.getInfo().get(0).getDetails());
    }

    @Test
    public void testInvalidAnyTypeElement() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-anytype-element.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAnyType(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal anyType", collector.getErrors().get(0).getMessage());
        assertEquals("element ElementAny namespace 'http://service/any/v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidAnyTypeTypes() throws Exception {
        String err = "Illegal anyType";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-anytype-types.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAnyType(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("complexType ComplexAnyType namespace 'http://service/any/v1'",
                collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("complexType ComplexEmbeddedAnyType namespace 'http://service/any/v1'",
                collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("simpleType SimpleAnyType namespace 'http://service/any/v1'",
                collector.getErrors().get(2).getDetails());
    }

}
