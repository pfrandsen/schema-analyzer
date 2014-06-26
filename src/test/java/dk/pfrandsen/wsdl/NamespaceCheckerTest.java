package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class NamespaceCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "namespace");
    private AnalysisInformationCollector collector;
    private WSDLParser parser;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
        parser = new WSDLParser();
    }

    private String getFileUri(String filename) {
        return RELATIVE_PATH.resolve(filename).toFile().toURI().toString();
    }

    @Test
    public void testValid() {
        String uri = getFileUri("Namespace-valid.wsdl");
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        String xml = definition.getAsString();
        System.out.println(xml);
        NamespaceChecker.checkNamespace(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalid() {
        String uri = getFileUri("Namespace-invalid.wsdl");
        Definitions definition = parser.parse(uri);
        assertTrue(definition != null);
        NamespaceChecker.checkNamespace(definition, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(5, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidUnusedImports() throws IOException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "namespace", "Namespace-unused-imports.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        NamespaceChecker.checkUnusedImports(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Namespace 'http://namespace5' imported but not used",
                collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace 'http://namespace3' imported but not used",
                collector.getWarnings().get(1).getMessage());

    }

}
