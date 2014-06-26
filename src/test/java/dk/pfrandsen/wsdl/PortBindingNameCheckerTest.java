package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.WsdlUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class PortBindingNameCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "service");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-valid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        PortBindingNameChecker.checkNames(wsdl, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalid() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-invalid.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        PortBindingNameChecker.checkNames(wsdl, collector);
        assertEquals(3, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("No matching wsdl:portType name for service 'FirstSvc'",
                collector.getErrors().get(0).getMessage());
        assertEquals("Found portType names [FirstService]; expected 'FirstSvcService'",
                collector.getErrors().get(0).getDetails());
        assertEquals("No matching wsdl:binding name for service 'FirstSvc'", collector.getErrors().get(1).getMessage());
        assertEquals("Found binding names [FirstBinding]; expected 'FirstSvcBinding'",
                collector.getErrors().get(1).getDetails());
        assertEquals("No matching wsdl:port name for service 'FirstSvc'", collector.getErrors().get(2).getMessage());
        assertEquals("Found port names [FirstWS]; expected 'FirstSvcWS'", collector.getErrors().get(2).getDetails());
    }

    @Test
    public void testInvalidMultiplePorts() throws Exception {
        Path path = RELATIVE_PATH.resolve("Service-Port-Binding-Name-invalid-multiple-ports.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        PortBindingNameChecker.checkNames(wsdl, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Multiple ports defined for service 'First'", collector.getErrors().get(0).getMessage());
        assertEquals("Found port names [FirstWS,SecondWS]", collector.getErrors().get(0).getDetails());
    }

}
