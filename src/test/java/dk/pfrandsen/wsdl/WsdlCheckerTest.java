package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class WsdlCheckerTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl");
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    private String targetNamespaceWsdl(String namespace) throws Exception {
        Path path = RELATIVE_PATH.resolve("targetNamespaceTemplate.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()));
        return wsdl.replace("@TNS@", namespace);
    }

    @Test
    public void testValidTargetNamespace() throws Exception {
        String service = "ServiceName";
        String tns = "http://service.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidTargetNamespaceProcess() throws Exception {
        String service = "ServiceName";
        String tns = "http://process.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidTargetNamespaceWithDashes() throws Exception {
        String service = "Service-Name-X";
        String tns = "http://service.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service.replace("-", "") + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testInvalidTargetNamespacePrefix() throws Exception {
        String service = "ServiceName";
        String tns = "http://svc.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Namespace 'http://svc.schemas.nykreditnet.net/domain/servicename/v1'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceNoDomain() throws Exception {
        String service = "ServiceName";
        String tns = "http://service.schemas.nykreditnet.net/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getWarnings().get(0).getMessage());
        assertEquals("Namespace 'http://service.schemas.nykreditnet.net/servicename/v1' does not match http://.../" +
                        "<domain>/<service>/<version>", collector.getWarnings().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceDashPostfix() throws Exception {
        String service = "ServiceName-";
        String tns = "http://service.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service.replace("-", "") + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Service 'servicename-' does not match pattern [a-z][\\-a-z0-9]]*",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceDashCapsFilename() throws Exception {
        String service = "service-Name";
        String tns = "http://service.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "/v1";
        String filename = "a/b/" + service.replace("-", "") + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Service 'service-name' (ServiceName) does not match filename 'serviceName'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceDashCaps() throws Exception {
        String service = "Service-Name";
        String tns = "http://service.schemas.nykreditnet.net/domain/" + service.toLowerCase() + "-x/v1";
        String filename = "a/b/" + service.replace("-", "") + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Service 'service-name-x' (ServiceNameX) does not match filename 'ServiceName'",
                collector.getErrors().get(0).getDetails());
    }

    @Test
    public void testInvalidTargetNamespaceServiceNotFound() throws Exception {
        String service = "serviceName";
        String tns = "http://service.schemas.nykreditnet.net/v1";
        String filename = "a/b/" + service + ".wsdl";
        String wsdl = targetNamespaceWsdl(tns);
        WsdlChecker.checkServiceNamespace(wsdl, filename, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Illegal service namespace", collector.getErrors().get(0).getMessage());
        assertEquals("Service part or namespace 'http://service.schemas.nykreditnet.net/v1' not found",
                collector.getErrors().get(0).getDetails());
    }

}
