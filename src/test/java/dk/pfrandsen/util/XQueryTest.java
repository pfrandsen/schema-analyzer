package dk.pfrandsen.util;

import ch.ethz.mxquery.exceptions.MXQueryException;
import dk.pfrandsen.Xml;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XQueryTest {

    @Test
    public void testBinding() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "binding", "Binding-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "binding");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String binding = XQuery.runXQuery(xq, "binding.xq", wsdl);
        List<Map<String, String>> result = Xml.parseXQueryResult(binding);
        assertEquals(1, result.size());
        assertEquals("FirstBinding", result.get(0).get("name"));
        assertEquals("tns:FirstService", result.get(0).get("type"));
    }

    @Test
    public void testSoapBinding() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "binding", "Binding-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "binding");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String binding = XQuery.runXQuery(xq, "soapbinding.xq", wsdl, "FirstBinding");
        List<Map<String, String>> result = Xml.parseXQueryResult(binding);
        assertEquals(1, result.size());
        assertEquals("document", result.get(0).get("style"));
        assertEquals("http://schemas.xmlsoap.org/soap/http", result.get(0).get("transport"));
    }

    @Test
    public void testService() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "service", "Service-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "service");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String services = XQuery.runXQuery(xq, "service.xq", wsdl);
        List<Map<String, String>> result = Xml.parseXQueryResult(services);
        assertEquals(1, result.size());
        assertEquals("SvcName", result.get(0).get("name"));
    }

    @Test
    public void testPort() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "port", "Port-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "port");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String ports = XQuery.runXQuery(xq, "port.xq", wsdl);
        List<Map<String, String>> result = Xml.parseXQueryResult(ports);
        assertEquals(1, result.size());
        assertEquals("SvcNameWS", result.get(0).get("name"));
        assertEquals("SvcName", result.get(0).get("service"));
    }

    @Test
    public void testPortMultiple() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "port", "Port-multiple.wsdl");
        Path xq = Paths.get("wsdl", "port");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String ports = XQuery.runXQuery(xq, "port.xq", wsdl);
        List<Map<String, String>> result = Xml.parseXQueryResult(ports);
        assertEquals(3, result.size());
        assertEquals("SvcNameOneWS", result.get(0).get("name"));
        assertEquals("SvcNameOne", result.get(0).get("service"));
        assertEquals("SvcNameTwoWS", result.get(1).get("name"));
        assertEquals("SvcNameTwo", result.get(1).get("service"));
        assertEquals("SvcNameThreeWS", result.get(2).get("name"));
        assertEquals("SvcNameTwo", result.get(2).get("service"));
    }

    @Test
    public void testServicePort() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "port", "Port-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "port");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String services = XQuery.runXQuery(xq, "servicePort.xq", wsdl, "SvcName");
        List<Map<String, String>> result = Xml.parseXQueryResult(services);
        assertEquals(1, result.size());
        assertEquals("SvcNameWS", result.get(0).get("name"));
    }

    @Test
    public void testServicePortMultiple() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "port", "Port-multiple.wsdl");
        Path xq = Paths.get("wsdl", "port");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String services = XQuery.runXQuery(xq, "servicePort.xq", wsdl, "SvcNameTwo");
        List<Map<String, String>> result = Xml.parseXQueryResult(services);
        assertEquals(2, result.size());
        assertEquals("SvcNameTwoWS", result.get(0).get("name"));
        assertEquals("SvcNameThreeWS", result.get(1).get("name"));
    }

    @Test
    public void testUnusedImports() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "namespace", "Namespace-unused-imports.wsdl");
        //Path xq = Paths.get("wsdl", "namespace");
        Path xq = Paths.get("");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String xqResult = XQuery.runXQuery(xq, "unusedImport.xq", wsdl);
        List<Map<String, String>> result = Xml.parseXQueryResult(xqResult);
        System.out.println(xqResult);
        assertEquals(2, result.size());
        List<String> unusedNamespaces = new ArrayList<>();
        unusedNamespaces.add(result.get(0).get("namespace"));
        unusedNamespaces.add(result.get(1).get("namespace"));
        assertTrue("Expected unused: http://namespace5", unusedNamespaces.contains("http://namespace5"));
        assertTrue("Expected unused: http://namespace3", unusedNamespaces.contains("http://namespace3"));
    }

    @Test
    public void testMessageNames() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "message", "Message-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "message");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String messages = XQuery.runXQuery(xq, "messages.xq", wsdl);
        List<String> result = XQuery.mapResult(messages, "name");
        assertEquals(4, result.size());
        assertTrue("Expected uncategorizedFault message", result.contains("uncategorizedFault"));
        assertTrue("Expected applications message", result.contains("applications"));
        assertTrue("Expected getDataRequest message", result.contains("getDataRequest"));
        assertTrue("Expected getDataResponse message", result.contains("getDataResponse"));
    }

    @Test
    public void testMessageWithName() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "message", "Message-valid-simple.wsdl");
        Path xq = Paths.get("wsdl", "message");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String message = XQuery.runXQuery(xq, "message.xq", wsdl, "getDataRequest");
        List<Map<String, String>> result = Xml.parseXQueryResult(message);
        assertEquals(1, result.size());
        assertEquals("getDataRequest", result.get(0).get("name"));
        assertEquals("GetDataRequest", result.get(0).get("element-local"));
        assertEquals("http://service.schemas/domain/service/v1", result.get(0).get("element-namespace"));
        assertEquals("", result.get(0).get("type-local"));
        assertEquals("", result.get(0).get("type-namespace"));
    }

    @Test
    public void testMessageWithNameInvalidUsesType() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "message",
                "Message-invalid-uses-type-simple.wsdl");
        Path xq = Paths.get("wsdl", "message");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String message = XQuery.runXQuery(xq, "message.xq", wsdl, "getDataRequest");
        List<Map<String, String>> result = Xml.parseXQueryResult(message);
        assertEquals(1, result.size());
        assertEquals("getDataRequest", result.get(0).get("name"));
        assertEquals("", result.get(0).get("element-local"));
        assertEquals("", result.get(0).get("element-namespace"));
        assertEquals("GetDataRequest", result.get(0).get("type-local"));
        assertEquals("http://service.schemas/domain/service/v1", result.get(0).get("type-namespace"));
    }

    @Test
    public void testSoapAction() throws IOException, MXQueryException {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "binding",
                "Binding-valid-soap-action-xq-simple.wsdl");
        Path xq = Paths.get("wsdl", "binding");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String message = XQuery.runXQuery(xq, "bindingSoapAction.xq", wsdl, "FirstBinding");
        List<Map<String, String>> result = Xml.parseXQueryResult(message);
        assertEquals(3, result.size());
        assertEquals("opOne", result.get(0).get("name"));
        assertEquals("operationOneSoapAction", result.get(0).get("soapAction"));
        assertEquals("opTwo", result.get(1).get("name"));
        assertEquals("operationTwoSoapAction", result.get(1).get("soapAction"));
        assertEquals("opThree", result.get(2).get("name"));
        assertEquals("", result.get(2).get("soapAction"));
    }

    @Test
    public void testOperationFaults() throws Exception {
        Path resource = Paths.get("src", "test", "resources", "wsdl", "porttype",
                "PortType-valid-fault-xq.wsdl");
        Path xq = Paths.get("wsdl", "porttype");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String message = XQuery.runXQuery(xq, "operationFauts.xq", wsdl, "FirstService", "operationOne");
        List<Map<String, String>> result = Xml.parseXQueryResult(message);
        assertEquals(2, result.size());
        assertEquals("firstFault", result.get(0).get("name"));
        assertEquals("tns:firstFault", result.get(0).get("message"));
        assertEquals("firstFault", result.get(0).get("message-local"));
        assertEquals("http://service.schemas/domain/service/v1", result.get(0).get("message-ns"));
        assertEquals("tns", result.get(0).get("message-pre"));
        assertEquals("secondFault", result.get(1).get("name"));
        assertEquals("tns:secondFault", result.get(1).get("message"));
        assertEquals("secondFault", result.get(1).get("message-local"));
        assertEquals("http://service.schemas/domain/service/v1", result.get(1).get("message-ns"));
        assertEquals("tns", result.get(1).get("message-pre"));
    }

    @Test
    public void testUsedMessages() throws Exception {
        String [] expected = {"inputMessageRequest", "outputMessageResponse", "firstFault" ,"secondFault",
                "Applications", "Logging", "SomeHeader"};
        Path resource = Paths.get("src", "test", "resources", "wsdl", "message",
                "Message-used-xq.wsdl");
        Path xq = Paths.get("wsdl", "message");
        String wsdl = IOUtils.toString(new FileInputStream(resource.toFile()));
        String used = XQuery.runXQuery(xq, "used.xq", wsdl);
        List<String> result = XQuery.mapResult(used, "msg-local");
        assertEquals(7, result.size());
        assertTrue("Result [" + Utilities.join(", ", result) + "] expected [" +
                Utilities.join(", ", Arrays.asList(expected)) + "]", result.containsAll(Arrays.asList(expected)));
    }
}
