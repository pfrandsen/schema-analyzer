package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.XQuery;

import java.nio.file.Paths;

public class WsdlNameChecker {
    public static String ASSERTION_ID = "CA13-WSDL-Name-Validate";

    public static void checkName(String wsdl, AnalysisInformationCollector collector) {

        try {
            // get name attribute in <wsdl:definition ...> element
            String name = XQuery.mapSingleResult(XQuery.runXQuery(Paths.get("wsdl", "definition"),
                    "name.xq", wsdl), "name");
            if (!"".equals(name)) {
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
                for (String serviceName : ServiceChecker.getServiceNames(wsdl, true)) {
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
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking name attribute",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
