package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;

import java.util.ArrayList;
import java.util.List;

public class PortBindingNameChecker {
    public static final String ASSERTION_ID = "CA40-WSDL-Validate-Port-Binding-Name";

    public static void checkNames(String serviceName, String wsdl, AnalysisInformationCollector collector) {
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add(Utilities.removeVersion(serviceName)); // use service name without version
        checkPortTypeServiceName(serviceNames, wsdl, collector);
        checkBindingServiceName(serviceNames, wsdl, collector);
        checkPortServiceName(serviceNames, wsdl, collector);
    }

    public static void checkNames(String wsdl, AnalysisInformationCollector collector) {
        try {
            // get service name(s) without version
            List<String> serviceNames = ServiceChecker.getServiceNames(wsdl, true);
            checkPortTypeServiceName(serviceNames, wsdl, collector);
            checkBindingServiceName(serviceNames, wsdl, collector);
            checkPortServiceName(serviceNames, wsdl, collector);
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    // Check correlation between service name and portType name; each service name must have a corresponding portType
    // name where portType name == service name + "Service"
    public static void checkPortTypeServiceName(List<String> serviceNames, String wsdl,
                                                AnalysisInformationCollector collector) {
        try {
            List<String> ports = WsdlUtil.getPortTypes(wsdl);
            for (String serviceName : serviceNames) {
                String expected = serviceName + PortTypeChecker.PORT_TYPE_POSTFIX;
                if (!ports.contains(expected)) {
                    collector.addError(ASSERTION_ID, "No matching wsdl:portType name for service '" + serviceName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                            "Found portType names [" + Utilities.join(",", ports) + "]; expected '" + expected + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void checkBindingServiceName(List<String> serviceNames, String wsdl,
                                               AnalysisInformationCollector collector) {
        try {
            List<String> bindings = WsdlUtil.getBindings(wsdl);
            for (String serviceName : serviceNames) {
                String expected = serviceName + SoapBindingChecker.BINDING_NAME_POSTFIX;
                if (!bindings.contains(expected)) {
                    collector.addError(ASSERTION_ID, "No matching wsdl:binding name for service '" + serviceName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                            "Found binding names [" + Utilities.join(",", bindings) + "]; expected '" + expected + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void checkPortServiceName(List<String> serviceNames, String wsdl,
                                            AnalysisInformationCollector collector) {
        try {
            for (String serviceName : serviceNames) {
                String expected = serviceName + PortChecker.PORT_NAME_POSTFIX;
                List<String> ports = WsdlUtil.getPorts(wsdl, serviceName);
                if (ports.size() > 0) {
                    if (ports.size() > 1) {
                        collector.addError(ASSERTION_ID, "Multiple ports defined for service '" + serviceName + "'",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                                "Found port names [" + Utilities.join(",", ports) + "]");
                    }
                    if (!ports.contains(expected)) {
                        collector.addError(ASSERTION_ID, "No matching wsdl:port name for service '" + serviceName + "'",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                                "Found port names [" + Utilities.join(",", ports) + "]; expected '" + expected + "'");
                    }
                } else {
                    collector.addError(ASSERTION_ID, "No port defined for service '" + serviceName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector) {
        collector.addInfo(ASSERTION_ID, "Exception while checking portTypes",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }
}