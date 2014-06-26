package dk.pfrandsen.wsdl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Service;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;

public class ServiceChecker {
    public static final String ASSERTION_ID = "CA??-WSDL-Validate-Service";
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
            }

            // TODO: check wsdl endpoint WSSUPPORT-52

        } catch (Exception e) {
            collectException(e, collector);
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
                collectException(e, collector);
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

    private static void collectException(Exception e, AnalysisInformationCollector collector) {
        collector.addInfo(ASSERTION_ID, "Exception while checking services",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}