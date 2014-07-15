package dk.pfrandsen.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WsdlUtil {

    /**
     *
     * @param wsdl
     * @return list of service names in wsdl
     * @throws Exception
     */
    public static List<String> getServices(String wsdl) throws Exception {
        Path xqLocation = Paths.get("wsdl", "service");
        String services = XQuery.runXQuery(xqLocation, "service.xq", wsdl);
        return XQuery.mapResult(services, "name");
    }

    /**
     *
     * @param wsdl
     * @param serviceName
     * @return list of port  names for the given service name
     * @throws Exception
     */
    public static List<String> getPorts(String wsdl, String serviceName) throws Exception {
        Path xqLocation = Paths.get("wsdl", "port");
        String ports = XQuery.runXQuery(xqLocation, "servicePort.xq", wsdl, serviceName);
        return XQuery.mapResult(ports, "name");
    }

    public static String getTargetNamespace(String wsdl) throws Exception {
        Path xqLocation = Paths.get("wsdl", "definition");
        String namespace = XQuery.runXQuery(xqLocation, "targetNamespace.xq", wsdl);
        List<String> tns = XQuery.mapResult(namespace, "namespaceUri");
        if (tns.size() >= 1) {
            return tns.get(0);
        }
        return "";
    }

    /**
     *
     * @param wsdl
     * @return list of port info (map). Each entry in the list contains a map with keys "name" (value is port name) and
     * "service" (value is service name)
     * @throws Exception
     */
    public static List<Map<String, String>> getPorts(String wsdl) throws Exception {
        Path xqLocation = Paths.get("wsdl", "port");
        String ports = XQuery.runXQuery(xqLocation, "port.xq", wsdl);
        return XQuery.mapResult(ports, "name", "service");
    }

    /**
     *
     * @param wsdl
     * @return list of porttype names in wsdl
     * @throws Exception
     */
    public static List<String> getPortTypes(String wsdl) throws Exception {
        Path xqLocation = Paths.get("wsdl", "porttype");
        String portTypes = XQuery.runXQuery(xqLocation, "porttype.xq", wsdl);
        return XQuery.mapResult(portTypes, "name");
    }

    /**
     *
     * @param wsdl
     * @return list of binding names in wsdl
     * @throws Exception
     */
    public static List<String> getBindings(String wsdl) throws Exception {
        Path xqLocation = Paths.get("wsdl", "binding");
        String bindings = XQuery.runXQuery(xqLocation, "binding.xq", wsdl);
        return XQuery.mapResult(bindings, "name");
    }

}
