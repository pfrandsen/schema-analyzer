package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;

import java.util.List;

public class PortTypeChecker {
    public static final String ASSERTION_ID = "CA15-WSDL-PORTTYPE-VALIDATE";
    public static final String PORT_TYPE_POSTFIX = "Service";

    public static void checkCardinality(String wsdl, AnalysisInformationCollector collector) {
        try {
            List<String> ports = WsdlUtil.getPortTypes(wsdl);
            if (ports.size() == 0 || ports.size() > 1) {
                String details = ports.size() == 0 ? "No portType" : "Found [" + Utilities.join(",", ports) + "]";
                collector.addError(ASSERTION_ID, "WSDL must include exactly one portType element",
                        AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, details);
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    public static void checkName(String wsdl, AnalysisInformationCollector collector) {
        // port name must be upper camel case and must have postfix "Service"
        try {
            List<String> ports = WsdlUtil.getPortTypes(wsdl);
            for (String port : ports) {
                if (!Utilities.isUpperCamelCaseAscii(port)) {
                    collector.addError(ASSERTION_ID, "Name in wsdl:portType is invalid; must be upper camel case ascii",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Name found is '" + port + "'");
                }
                if (!port.endsWith(PORT_TYPE_POSTFIX)) {
                    collector.addError(ASSERTION_ID,
                            "Name in wsdl:portType is invalid; must end with \"" + PORT_TYPE_POSTFIX + "\"",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Name found is '" + port + "'");
                }
            }
            if (ports.size() == 0 || ports.size() > 1) {
                String details = ports.size() == 0 ? "No portType" : "Found [" + Utilities.join(",", ports) + "]";
                collector.addError(ASSERTION_ID, "WSDL must include exactly one portType element",
                        AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, details);
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
