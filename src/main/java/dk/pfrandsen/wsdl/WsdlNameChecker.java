package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;

public class WsdlNameChecker {
    public static String ASSERTION_ID = "CA13-WSDL-Name-Validate";

    public static void checkName(Definitions definitions, AnalysisInformationCollector collector) {
        String name = definitions.getName();
        if (name != null) {
            if (!Utilities.isUpperCamelCaseAscii(name)) {
                collector.addError(ASSERTION_ID, "Name in wsdl:definitions start tag is invalid; must be upper camel case ascii",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Name found is '" + name + "'");
            }
            if (!name.equals(Utilities.removeVersion(name))) {
                collector.addError(ASSERTION_ID, "Name in wsdl:definitions start tag contains version information",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Name found is '" + name + "'");
            }
            // should name match service? with or without version postfix?
            // Here warning is emitted if matching service name (with version removed) is not found in wsdl
            boolean match = false;
            for (String serviceName : ServiceChecker.getServiceNames(definitions, true)) {
                if (serviceName.equals(name)) {
                    match = true;
                }
            }
            if (!match) {
                collector.addWarning(ASSERTION_ID, "Name in wsdl:definitions start tag does not match service name",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                        "No service with name '" + name + "{V[0-9]+}' found");
            }
        }
    }
}
