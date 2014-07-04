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

public class OperationCheckerTest {
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidOperationNames() throws IOException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "operation", "Operations-valid-name.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        OperationChecker.checkOperationNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidOperationCase() throws IOException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "operation", "Operations-invalid-name-case.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        OperationChecker.checkOperationNames(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Operation name must be lower camel case", collector.getErrors().get(0).getMessage());
        assertEquals("Operation 'ArchiveEntity', portType 'EntityServiceTwo'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidOperationCardinality() throws IOException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "operation",
                "Operations-invalid-name-cardinality.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        OperationChecker.checkOperationNames(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Operation name must contain <action><Object>", collector.getErrors().get(0).getMessage());
        assertEquals("Operation 'remove' in portType 'EntityServiceTwo' only has action or object",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidOperationUnknownAction() throws IOException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "operation", "Operations-invalid-name-action.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        OperationChecker.checkOperationNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Operation <action> is unknown", collector.getWarnings().get(0).getMessage());
        assertEquals("Action 'unknownaction' in operation 'unknownactionEntity' in portType 'EntityServiceTwo' is not" +
                " in set of previously defined actions", collector.getWarnings().get(0).getDetails());
    }

}