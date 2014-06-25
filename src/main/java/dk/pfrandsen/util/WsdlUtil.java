package dk.pfrandsen.util;

import dk.pfrandsen.Xml;

import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
        return mapResult(services, "name");
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
        return mapResult(ports, "name");
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
        return mapResult(ports, getKeyList("name", "service"));
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
        return mapResult(portTypes, "name");
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
        return mapResult(bindings, "name");
    }

    private static List<String> mapResult(String xqResult, String key) {
        List<Map<String, String>> result = Xml.parseXQueryResult(xqResult);
        List<String> retVal = new ArrayList<>();
        if (result.size() > 0) {
            for (Map<String, String> element : result) {
                retVal.add(element.get(key));
            }
        }
        return retVal;
    }

    private static List<Map<String, String>> mapResult(String xqResult, List<String> keys) {
        List<Map<String, String>> result = Xml.parseXQueryResult(xqResult);
        List<Map<String, String>> retVal = new ArrayList<>();
        if (result.size() > 0) {
            for (Map<String, String> element : result) {
                Map<String, String> map = new HashMap<>();
                for (String key : keys) {
                    map.put(key, "" + element.get(key));
                }
                result.add(map);
            }
        }
        return retVal;
    }

    private static List<String> getKeyList(String... keys) {
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            list.add(key);
        }
        return list;
    }
}
