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
        serviceNames.add(serviceName);

        checkPortTypeServiceName(serviceNames, wsdl, collector);
        checkBindingServiceName(serviceNames, wsdl, collector);
        // check port name

    }

    // Check correlation between service name and portType name; each service name must have a corresponding portType
    // name where portType name == service name + "Service"
    public static void checkPortTypeServiceName(List<String> serviceNames, String wsdl,
                                                AnalysisInformationCollector collector) {
        try {
            List<String> ports = WsdlUtil.getPortTypes(wsdl);
            for (String serviceName : serviceNames) {
                if (!ports.contains(serviceName + PortTypeChecker.PORT_TYPE_POSTFIX)) {
                    collector.addError(ASSERTION_ID, "No matching wsdl:portType name for service '" + serviceName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                            "Found portType names [" + Utilities.join(",", ports) + "]");
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    public static void checkBindingServiceName(List<String> serviceNames, String wsdl,
                                               AnalysisInformationCollector collector) {
        try {
            List<String> bindings = WsdlUtil.getBindings(wsdl);
            for (String serviceName : serviceNames) {
                if (!bindings.contains(serviceName + SoapBindingChecker.BINDING_NAME_POSTFIX)) {
                    collector.addError(ASSERTION_ID, "No matching wsdl:binding name for service '" + serviceName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                            "Found binding names [" + Utilities.join(",", bindings) + "]");
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