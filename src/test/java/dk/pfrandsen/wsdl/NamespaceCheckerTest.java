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

public class NamespaceCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "namespace");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkNamespace(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalid() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-invalid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkNamespace(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(4, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Unused namespace prefix",
                collector.getErrors().get(0).getMessage());
        assertEquals("Prefix 'unused', namespace 'unused.com'",
                collector.getErrors().get(0).getDetails());
        assertEquals("Namespace must be in lowercase (http://Service.schemas/domain/service/v1)",
                collector.getWarnings().get(1).getMessage());
        assertEquals("Namespace must start with http:// (unused.com)",
                collector.getWarnings().get(2).getMessage());
        assertEquals("Namespace prefix must be in lowercase (Soapsoap)",
                collector.getWarnings().get(3).getMessage());
    }

    @Test
    public void testInvalidTns() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-invalid-tns.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkNamespace(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Namespace 'tns' not found top level <wsdl> element.",
                collector.getErrors().get(0).getMessage());
    }

    @Test
    public void testInvalidMultipleDefinitions() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-invalid-multiple-definitions.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkNamespace(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Duplicate namespaces found",
                collector.getWarnings().get(0).getMessage());
        assertEquals("Prefixes: tn,tns,xs,xsd",
                collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidUnusedImports() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-unused-imports.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkUnusedImports(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Namespace 'http://namespace5' imported but not used",
                collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace 'http://namespace3' imported but not used",
                collector.getWarnings().get(1).getMessage());
    }

    @Test
    public void testInvalidImports() throws IOException {
        Path path = RELATIVE_PATH.resolve("Namespace-invalid-imports.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        NamespaceChecker.checkInvalidImports(wsdl, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Namespace import not allowed", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://invalidNamespaceGlobal' not in target namespace or " +
                        "'http://technical.schemas.nykreditnet.net/*'", collector.getErrors().get(0).getDetails());
        assertEquals("Namespace import not allowed", collector.getErrors().get(1).getMessage());
        assertEquals("Namespace 'http://invalidNamespaceTypes' not in target namespace or " +
                        "'http://technical.schemas.nykreditnet.net/*'",  collector.getErrors().get(1).getDetails());
    }

}
