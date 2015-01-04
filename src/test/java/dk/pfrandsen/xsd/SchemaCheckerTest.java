package dk.pfrandsen.xsd;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.net.URL;
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

    @Test
    public void testValidAnyAttribute() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-anyattribute.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAnyAttribute(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidAnyAttribute() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-anyattribute.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAnyAttribute(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal anyAttribute", collector.getErrors().get(0).getMessage());
        assertEquals("complexType NonTechnicalType namespace 'http://service.schemas.nykreditnet.net/header/v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidAny() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-any.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAny(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(1, collector.infoCount());
        assertEquals("Use of any (permitted)", collector.getInfo().get(0).getMessage());
        assertEquals("element DokumentData namespace 'http://service.schemas.nykreditnet.net/di/dokumentdannelse/v1'",
                collector.getInfo().get(0).getDetails());
    }

    @Test
    public void testInvalidAny() throws Exception {
        String err = "Illegal any";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-any.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkAny(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("element ElementWithAny namespace 'http://service/domain/v1'",
                collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("complexType TypeWithAny namespace 'http://service/domain/v1'",
                collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testValidIdenticalElements() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-element-same-name.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkIdenticalElementNames(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidIdenticalElements() throws Exception {
        String err = "Illegal repeated element name";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-element-same-name.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkIdenticalElementNames(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("complexType Type2Type child node 'sequence', repeated [ServiceConcept]",
                collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("complexType Type2Type child node 'sequence', repeated [ConceptTwo,ConceptThree]",
                collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("element Element4 child node 'choice', repeated [ConceptTwo]",
                collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testValidImportIncludeSchemaLocation() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-import-include-schema-location.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkImportAndIncludeLocation(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidImportIncludeSchemaLocation() throws Exception {
        String err = "Illegal schema location";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-import-include-schema-location.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkImportAndIncludeLocation(xsd, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals("Import location 'rel.url.location/loc1', namespace 'http://domain.net/domain/v1'",
                collector.getErrors().get(0).getDetails());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals("Import location '../rel.url.location/loc2', namespace 'http://domain.net/domain/v2'",
                collector.getErrors().get(1).getDetails());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals("Include location 'rel.url.location/2'",
                collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testValidSchemaFileNames() {
        SchemaChecker.checkSchemaFilename("Name10.xsd", collector);
        SchemaChecker.checkSchemaFilename("Name1Name.xsd", collector);
        SchemaChecker.checkSchemaFilename("Name.xsd", collector);
        SchemaChecker.checkSchemaFilename("abc/def/Name1.xsd", collector);
        SchemaChecker.checkSchemaFilename("http://abc/def/Name1.xsd", collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidSchemaFileNames() {
        String extension = "Illegal filename extension. Must be .xsd";
        String basename = "Illegal filename. Must be upper camel case ascii";
        SchemaChecker.checkSchemaFilename("Name.xs", collector);
        SchemaChecker.checkSchemaFilename("Name1Name.XSD", collector);
        SchemaChecker.checkSchemaFilename("name.xsd", collector);
        SchemaChecker.checkSchemaFilename("_Name.xsd", collector);
        SchemaChecker.checkSchemaFilename("10Name.xsd", collector);
        SchemaChecker.checkSchemaFilename("Name .xsd", collector);
        SchemaChecker.checkSchemaFilename("Søg.xsd", collector);
        assertEquals(7, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(extension, collector.getErrors().get(0).getMessage());
        assertEquals(extension, collector.getErrors().get(1).getMessage());
        assertEquals(basename, collector.getErrors().get(2).getMessage());
        assertEquals(basename, collector.getErrors().get(3).getMessage());
        assertEquals(basename, collector.getErrors().get(4).getMessage());
        assertEquals(basename, collector.getErrors().get(5).getMessage());
        assertEquals(basename, collector.getErrors().get(6).getMessage());
    }

    private String nsDocHelper(String namespace) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ");
        builder.append("targetNamespace=\"");
        builder.append(namespace);
        builder.append("\">");
        builder.append("</xs:schema>");
        return builder.toString();
    }

    @Test
    public void testInvalidTargetNamespaceCase() {
        String xsd = nsDocHelper("http://tns.With.Caps/V1");
        SchemaChecker.checkTargetNamespaceCase(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Target namespace must be lower case", collector.getErrors().get(0).getMessage());
        assertEquals("Target namespace 'http://tns.With.Caps/V1'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespacePrefix() {
        String xsd = nsDocHelper("http:/tns.with.invalid.prefix/v1");
        SchemaChecker.checkTargetNamespacePrefix(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Target namespace must start with http://", collector.getErrors().get(0).getMessage());
        assertEquals("Target namespace 'http:/tns.with.invalid.prefix/v1'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceCharacters() {
        String err = "Target namespace contains illegal characters";
        String ns1 = "http://tns-with.invalid.chars/v1";     // char:  '-'
        String ns2 = "http://tns.with.invalid.chars/æøå/v1"; // chars:  'æ', 'ø', 'å'
        String ns3 = "http://tns.with.invalid.chars\\v1";    // char:  '\'
        String ns4 = "http://tns.with.invalid.chars/v1 ";    // space
        SchemaChecker.checkTargetNamespaceCharacters(nsDocHelper(ns1), collector);
        SchemaChecker.checkTargetNamespaceCharacters(nsDocHelper(ns2), collector);
        SchemaChecker.checkTargetNamespaceCharacters(nsDocHelper(ns3), collector);
        SchemaChecker.checkTargetNamespaceCharacters(nsDocHelper(ns4), collector);
        assertEquals(4, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(err, collector.getErrors().get(0).getMessage());
        assertEquals(err, collector.getErrors().get(1).getMessage());
        assertEquals(err, collector.getErrors().get(2).getMessage());
        assertEquals(err, collector.getErrors().get(3).getMessage());
        assertEquals("Target namespace '" + ns1 + "'", collector.getErrors().get(0).getDetails());
        assertEquals("Target namespace '" + ns2 + "'", collector.getErrors().get(1).getDetails());
        assertEquals("Target namespace '" + ns3 + "'", collector.getErrors().get(2).getDetails());
        assertEquals("Target namespace '" + ns4 + "'", collector.getErrors().get(3).getDetails());
    }

    @Test
    public void testValidEnterpriseConceptTargetNamespace() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://concept.schemas.nykreditnet.net/domain/sublevel/" + concept.toLowerCase() + "/v1";
        SchemaChecker.checkEnterpriseConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidEnterpriseConceptTargetNamespaceName() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://concept.schemas.nykreditnet.net/domain/sublevel/concept/v1";
        SchemaChecker.checkEnterpriseConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'concept' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidEnterpriseConceptTargetNamespaceNameCase() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://concept.schemas.nykreditnet.net/domain/sublevel/" + concept + "/v1";
        SchemaChecker.checkEnterpriseConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'conceptName' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidEnterpriseConceptTargetNamespaceNoConcept() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://concept.schemas.nykreditnet.net/domain/v1";
        SchemaChecker.checkEnterpriseConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'domain' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Illegal concept namespace", collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace 'http://concept.schemas.nykreditnet.net/domain/v1' does not match http://concept." +
                "schemas.nykreditnet.net/<domain>/<concept>/<version>", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testValidServiceConceptTargetNamespace() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://servive.schemas.nykreditnet.net/domain/concept/" + concept.toLowerCase() + "/v1";
        SchemaChecker.checkServiceConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidServiceConceptTargetNamespaceName() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://service.schemas.nykreditnet.net/domain/service/concept/concept/v1";
        SchemaChecker.checkServiceConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'concept' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidServiceConceptTargetNamespaceNameCase() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://service.schemas.nykreditnet.net/domain/service/concept/" + concept + "/v1";
        SchemaChecker.checkServiceConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'conceptName' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidServiceConceptTargetNamespaceNoConcept() {
        String concept = "conceptName";
        String filename = "a/b/" + concept + ".xsd";
        String tns = "http://service.schemas.nykreditnet.net/domain/service/concept/v1";
        SchemaChecker.checkServiceConceptNamespace(nsDocHelper(tns), filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service concept namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Concept 'concept' does not match filename 'conceptname'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Illegal service concept namespace", collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace 'http://service.schemas.nykreditnet.net/domain/service/concept/v1' does not match " +
                        "http://service.schemas.nykreditnet.net/<domain>/<service>/concept/<concept>/<version>",
                collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testValidDeprecated() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-deprecated.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkDeprecated(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidDeprecatedNs1() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-deprecated-ns1.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkDeprecated(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidDeprecatedNs2() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-deprecated-ns2.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkDeprecated(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidDeprecated() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-deprecated.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkDeprecated(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Deprecated schema", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://domain.net/domain/v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testValidUnusedNamespacePrefix() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-namespace-prefix.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedNamespacePrefix(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidUnusedNamespacePrefix() throws Exception {
        String msg = "Unused namespace prefix";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-namespace-prefix.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedNamespacePrefix(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(msg, collector.getErrors().get(0).getMessage());
        assertEquals(msg, collector.getErrors().get(1).getMessage());
        assertEquals(msg, collector.getWarnings().get(0).getMessage());
        assertEquals("Prefix 'xsd', namespace 'http://www.w3.org/2001/XMLSchema'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Prefix 'ns3', namespace 'http://service.net/bankservice/concept/v2'",
                collector.getErrors().get(1).getDetails());
        assertEquals("Prefix 'tns', namespace 'http://domain.net/service/v1'",
                collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidUnusedNamespacePrefixWsdl() throws Exception {
        String msg = "Unused namespace prefix";
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-namespace-prefix.wsdl");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedNamespacePrefix(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(msg, collector.getErrors().get(0).getMessage());
        assertEquals(msg, collector.getErrors().get(1).getMessage());
        assertEquals("Prefix 'xs', namespace 'http://www.w3.org/2001/XMLSchema'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Prefix 'soap', namespace 'http://schemas.xmlsoap.org/wsdl/soap/'",
                collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testValidUnusedImport() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("valid-import-used.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedImport(xsd, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidUnusedImport() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-import-used.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedImport(xsd, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unused import", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://service.net/bankservice/concept/v2'", collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidUnusedImportTwo() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-import-used2.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedImport(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unused import", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://service.net/bankservice/concept/v2'", collector.getErrors().get(0).getDetails());
        assertEquals("Unused import", collector.getErrors().get(1).getMessage());
        assertEquals("Namespace 'http://concept.net/bank/v2'", collector.getErrors().get(1).getDetails());
    }

    @Test
    public void testInvalidUnusedImportWsdl() throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("invalid-import-used.wsdl");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        SchemaChecker.checkUnusedImport(xsd, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unused import", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://namespace5'", collector.getErrors().get(0).getDetails());
        assertEquals("Unused import", collector.getErrors().get(1).getMessage());
        assertEquals("Namespace 'http://namespace3'", collector.getErrors().get(1).getDetails());
    }

    private String targetNamespaceXsd(String namespace) throws Exception {
        Path path = RELATIVE_PATH_TYPES.resolve("targetNamespaceTemplate.xsd");
        String xsd = IOUtils.toString(new FileInputStream(path.toFile()));
        return xsd.replace("@TNS@", namespace);
    }

    @Test
    public void testValidPathMatchesTargetNamespaceUrl() throws Exception {
        String tns = "http://service.schemas.nykreditnet.net/domain/service/v1";
        String xsd = targetNamespaceXsd(tns);
        SchemaChecker.checkPathAndTargetNamespace(xsd, new URL(tns), collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidPathMatchesTargetNamespacePath() throws Exception {
        String tns = "http://service.schemas.nykreditnet.net/businessdomain/service/v1";
        String xsd = targetNamespaceXsd(tns);
        Path path = Paths.get("businessdomain", "service", "v1");
        String domain = "service.schemas.nykreditnet.net";
        SchemaChecker.checkPathAndTargetNamespace(xsd, domain, path, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidPathMatchesTargetNamespaceUrl() throws Exception {
        String tns = "http://service.schemas.nykreditnet.net/domain/service/v1";
        String location = "http://service.schemas.nykreditnet.net/service/v1";
        String xsd = targetNamespaceXsd(tns);
        SchemaChecker.checkPathAndTargetNamespace(xsd, new URL(location), collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Target namespace must match path", collector.getErrors().get(0).getMessage());
        assertEquals("Target namespace 'http://service.schemas.nykreditnet.net/domain/service/v1', path " +
                        "'http://service.schemas.nykreditnet.net/service/v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidPathMatchesTargetNamespacePath() throws Exception {
        String tns = "http://service.schemas.nykreditnet.net/businessdomain/service/v1";
        String xsd = targetNamespaceXsd(tns);
        Path path = Paths.get("businessdomain", "service", "v1");
        String domain = "domain";
        SchemaChecker.checkPathAndTargetNamespace(xsd, domain, path, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Target namespace must match path", collector.getErrors().get(0).getMessage());
        assertEquals("Target namespace 'http://service.schemas.nykreditnet.net/businessdomain/service/v1', path " +
                "'http://domain/businessdomain/service/v1'", collector.getErrors().get(0).getDetails());
    }

}
