package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XsdUtil;
import org.apache.commons.io.FilenameUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

public class WsdlChecker {
    public static String ASSERTION_ID_SERVICE_NAMESPACE = "CA27-WSDL-Filename-And-Namespace-Validate";
    public static String ASSERTION_ID_PATH = "CA37-WSDL-File-Path-Validate";
    public static String ASSERTION_ID_NAMESPACE_MATCH_PATH = "CA39-WSDL-Namespace-Match-Path-Validate";

    private static String removePrefix(String targetNamespace) {
        String servicePrefix = "http://service.schemas.nykreditnet.net/";
        String processPrefix = "http://process.schemas.nykreditnet.net/";
        if (targetNamespace.startsWith(servicePrefix)) {
            return targetNamespace.replace(servicePrefix, "");
        }
        if (targetNamespace.startsWith(processPrefix)) {
            return targetNamespace.replace(processPrefix, "");
        }
        return targetNamespace;
    }

    public static void checkServiceNamespace(String wsdl, String filename,
                                                       AnalysisInformationCollector collector) {
        String err = "Illegal service namespace";
        // namespace pattern:
        // http://service.schemas.nykreditnet.net/<domain>/[<sublevels>]/<service>/<version>
        // http://process.schemas.nykreditnet.net/<domain>/[<sublevels>]/<service>/<version>
        String basename = FilenameUtils.getBaseName(filename);
        try {
            String tns = WsdlUtil.getTargetNamespace(wsdl);
            if (!XsdUtil.isInternalServiceNamespace(tns)) {
                collector.addError(ASSERTION_ID_SERVICE_NAMESPACE, err,
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Namespace '" + tns + "'");
            } else {
                String[] parts = removePrefix(tns).split("/");
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

    public static void checkPathCharacters(String path, AnalysisInformationCollector collector) {
        String regexp = "(?i)[a-z0-9\\:\\/\\.]+";
        if (!path.matches(regexp)) {
            String illegal = "[" + path.replaceAll(regexp, "") + "]";
            collector.addError(ASSERTION_ID_PATH, "Invalid characters in path",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Path '" + path + "', illegal " + illegal);
        }
    }

    public static void checkPath(Path relativePath, AnalysisInformationCollector collector) {
        checkPathCharacters(relativePath.toString().replace("\\", "/"), collector);
    }

    public static void checkPath(URL url, AnalysisInformationCollector collector) {
        checkPathCharacters(url.toString(), collector);

    }

    private static void checkPathAndTargetNamespace(String wsdl, String path, AnalysisInformationCollector collector) {
        try {
            String tns = WsdlUtil.getTargetNamespace(wsdl);
            if (!path.equals(tns)) {
                collector.addError(ASSERTION_ID_NAMESPACE_MATCH_PATH, "Target namespace must match path",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Target namespace '" + tns + "', path '" +
                                path + "'");
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_NAMESPACE_MATCH_PATH);
        }
    }

    public static void checkPathAndTargetNamespace(String wsdl, String domain, Path relativePath,
                                                   AnalysisInformationCollector collector) {
        checkPathAndTargetNamespace(wsdl, Utilities.pathToNamespace(domain, relativePath), collector);
    }

    public static void checkPathAndTargetNamespace(String wsdl, URL url, AnalysisInformationCollector collector) {
        checkPathAndTargetNamespace(wsdl, url.toString(), collector);

    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking wsdl",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
