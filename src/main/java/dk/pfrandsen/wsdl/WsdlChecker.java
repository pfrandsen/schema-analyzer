package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XsdUtil;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

public class WsdlChecker {

    public static String ASSERTION_ID_SERVICE_NAMESPACE = "CA27-WSDL-Filename-And-Namespace-Validate";

    public static void checkServiceNamespace(String wsdl, String filename,
                                                       AnalysisInformationCollector collector) {
        String err = "Illegal service namespace";
        // namespace pattern:
        // http://service.schemas.nykreditnet.net/<domain>/[<sublevels>]/<service>/<version>
        // http://process.schemas.nykreditnet.net/<domain>/[<sublevels>]/<service>/<version>
        String prefix1 = "http://service.schemas.nykreditnet.net/";
        String prefix2 = "http://process.schemas.nykreditnet.net/";
        String basename = FilenameUtils.getBaseName(filename);
        try {
            String tns = WsdlUtil.getTargetNamespace(wsdl);
            if (!XsdUtil.isInternalServiceNamespace(tns)) {
                collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Namespace '" + tns + "'");
            } else {
                String[] parts = tns.replace(prefix1, "").replace(prefix2, "").split("/");
                if (parts.length >= 2) {
                    String service = parts[parts.length - 2];
                    if (service.matches("[a-z][\\-a-z0-9]*") && !service.endsWith("-")) {
                        if (service.contains("-")) {
                            // special case abc-def-ghi => AbcDefGhi
                            String expectedName = "";
                            String p[] = service.split("\\-");
                            for (String n : Arrays.asList(p)) {
                                expectedName += Utilities.toUpperCamelCase(n);
                            }
                            if (!basename.matches(expectedName)) {
                                collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service '" + service +
                                                "' (" + expectedName + ") does not match filename '" + basename + "'");
                            }
                        } else if (!basename.toLowerCase().matches(service)) {
                            collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service '" + service +
                                            "' does not match filename '" + basename.toLowerCase() + "'");
                        }
                        if (parts.length < 3) {
                            // context part should contain at least <domain>/<service>/<version>
                            collector.addWarning(ASSERTION_ID_SERVICE_NAMESPACE, err,
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Namespace '" + tns +
                                            "' does not match http://.../<domain>/<service>/<version>");
                        }
                    } else {
                        // name does not match pattern
                        collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service '" + service +
                                        "' does not match pattern [a-z][\\-a-z0-9]]*");
                    }
                } else {
                    // unable to find service from namespace
                    collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Service part or namespace '" + tns +
                                    "' not found");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_SERVICE_NAMESPACE);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking wsdl",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
