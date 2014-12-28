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

public class BetaNamespaceCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "namespace");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Namespace-valid.wsdl").toFile()));
        BetaNamespaceChecker.checkBetaNamespace(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalid() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Namespace-invalid-beta.wsdl").toFile()));
        BetaNamespaceChecker.checkBetaNamespace(wsdl, collector);
        assertEquals(4, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidDefinitions() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Namespace-invalid-beta.wsdl").toFile()));
        BetaNamespaceChecker.checkBetaNamespaceDefinitions(wsdl, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidImports() throws IOException {
        String wsdl = IOUtils.toString(new FileInputStream(RELATIVE_PATH.resolve("Namespace-invalid-beta.wsdl").toFile()));
        BetaNamespaceChecker.checkBetaNamespaceImports(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(0).getSeverity());
        assertEquals("wsdl:types references beta namespace in import 'http://beta-service.net/service/v1'",
                collector.getErrors().get(0).getMessage());
    }

}
