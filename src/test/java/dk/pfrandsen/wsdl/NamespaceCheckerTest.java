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
        assertEquals(0, collector.errorCount());
        assertEquals(5, collector.warningCount());
        assertEquals(0, collector.infoCount());
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
