package dk.pfrandsen.wsdl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.mxquery.exceptions.MXQueryException;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Service;
import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.wsdl.util.Wsdl;

public class ServiceChecker {
    public static final String ASSERTION_ID = "CA??-WSDL-Validate-Service";
    public static final String ASSERTION_ID_SERVICE_ENDPOINT = "CA53-WSDL-Validate-Endpoint";
    // public static final String SERVICE_PORT_POSTFIX = "WS";

    public static List<String> getServiceNames(Definitions definitions, boolean removeVersion) {
        List<String> serviceNames = new ArrayList<String>();
        for (Service service : definitions.getLocalServices()) {
            String name = service.getName();
            if (removeVersion) {
                name = Utilities.removeVersion(name);
            }
            serviceNames.add(name);
        }
        return serviceNames;
    }

    public static void checkServices(String wsdl, AnalysisInformationCollector collector) {
        try {
            List<String> serviceNames = getServiceNames(wsdl, false);
            checkCardinality(serviceNames, collector);
            for (String serviceName : serviceNames) {
                if (!serviceName.equals(Utilities.removeVersion(serviceName))) {
                    collector.addWarning(ASSERTION_ID, "Version postfix found in wsdl:service name",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Name: '" + serviceName + "'");
                }
                if (!Utilities.isUpperCamelCaseAscii(serviceName)) {
                    collector.addError(ASSERTION_ID, "Name in wsdl:service is not upper camel case",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Name: '" + serviceName + "'");
                }
                checkEndpoints(wsdl, serviceName, collector);
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID);
        }
    }

    public static void checkEndpoints(String wsdl, String serviceName, AnalysisInformationCollector collector) {
        Path xqPort = Paths.get("wsdl", "port");
        try {
            String portsXml = XQuery.runXQuery(xqPort, "servicePort.xq", wsdl, serviceName);
            List<String> portNames = XQuery.mapResult(portsXml, "name");
            for (String portName : portNames) {
                String locationXml = XQuery.runXQuery(xqPort, "soapAddress.xq", wsdl, serviceName, portName);
                List<String> locations = XQuery.mapResult(locationXml, "location");
                if (locations.size() != 1) {
                    collector.addError(ASSERTION_ID_SERVICE_ENDPOINT, "Service must define exactly one endpoint",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service: '" + serviceName + "', " +
                                    "port: '" + portName + "', location: [" + Utilities.join(", ", locations) + "]");
                } else {
                    String location = locations.get(0);
                    String tns = WsdlUtil.getTargetNamespace(wsdl);
                    // check pattern
                    String prefix = "^http[s]?://[^/]+/";
                    if (location.matches(prefix + ".*")) {
                        String loc = location.replaceFirst(prefix, "");
                        tns = tns.replaceFirst("^http://[^/]+/", "");
                        if (tns.startsWith("enterprise/")) {
                            tns = tns.substring("enterprise/".length());
                        }
                        String[] components = tns.split("/");
                        if (components.length != 3) {
                            collector.addError(ASSERTION_ID_SERVICE_ENDPOINT, "Service location must have 3 " +
                                            "components <domain>, <service name>, and <version>",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service: '" + serviceName +
                                            "', " + "port: '" + portName + "', location: '" + location + "', " +
                                            "components: [" + Utilities.join(", ", Arrays.asList(components)) + "]");
                        }
                        int idx = tns.lastIndexOf('/');
                        tns = tns.replace('/', '-');
                        if (idx >= 0)  {
                            char[] chars = tns.toCharArray();
                            chars[idx] = '/';
                            tns = String.valueOf(chars);
                        }
                        String expected = "ws-" + tns;
                        if (!expected.equals(loc)) {
                            collector.addError(ASSERTION_ID_SERVICE_ENDPOINT, "Service location endpoint does not match " +
                                    "http[s]://<server>[:port]/ws-<domain>-<service name>/<version>",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service: '" + serviceName +
                                    "', " + "port: '" + portName + "', location: '" + location + "', expected: '" +
                            expected + "'");
                        }
                    } else {
                        collector.addError(ASSERTION_ID_SERVICE_ENDPOINT, "Service location endpoint does not match " +
                                prefix, AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service: '" + serviceName +
                                "', " + "port: '" + portName + "', location: '" + location + "'");
                    }
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_SERVICE_ENDPOINT);
        }

    }

    public static void checkServiceFileName(Path fileName, String wsdl, AnalysisInformationCollector collector) {
        // filename must not contain version number
        String fName = fileName.getFileName().toString();
        if (!fName.endsWith(".wsdl")) {
            collector.addError(ASSERTION_ID, "WSDL filename does not end with .wsdl",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Filename: " + fName);
        } else {
            fName = fName.substring(0, fName.lastIndexOf("."));
            if (!fName.equals(Utilities.removeVersion(fName))) {
                collector.addError(ASSERTION_ID, "WSDL filename contains version number",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Filename: " + fName);
            }
            try {
                List<String> serviceNames = getServiceNames(wsdl, false);
                for (String serviceName : serviceNames) {
                    String nameWithoutVersion = Utilities.removeVersion(serviceName);
                    if (!fName.equals(nameWithoutVersion)) {
                        String name = "'" + nameWithoutVersion + "'" +
                                (serviceName.equals(nameWithoutVersion) ? "" : " (" + serviceName + ")");
                        collector.addError(ASSERTION_ID, "Service name does not match filename",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Filename: " + fName +
                                        ", service name: " + name);
                    }
                }
            } catch (Exception e) {
                collectException(e, collector, ASSERTION_ID);
            }
        }
    }

    public static void checkCardinality(List<String> serviceNames, AnalysisInformationCollector collector) {
        if (serviceNames.size() > 1) {
            collector.addError(ASSERTION_ID, "Found more than one wsdl:service name",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                    "Found service names [" + Utilities.join(",", serviceNames) + "]");
        }
        if (serviceNames.size() == 0) {
            collector.addError(ASSERTION_ID, "No wsdl:service name found",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
    }

    public static List<String> getServiceNames(String wsdl, boolean removeVersion) throws Exception {
        List<String> serviceNames = WsdlUtil.getServices(wsdl);
        if (!removeVersion) {
            return serviceNames;
        }
        List<String> serviceNamesNoVersion = new ArrayList<>();
        for (String name : serviceNames) {
            serviceNamesNoVersion.add(Utilities.removeVersion(name));
        }
        return serviceNamesNoVersion;
    }


    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking services",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}