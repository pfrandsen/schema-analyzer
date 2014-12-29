package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.xsd.SchemaChecker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NamespaceChecker {
    public static final String ASSERTION_ID = "CA28A-WSDL-NAMESPACE-NAMING-AND-USAGE";
    public static final String NS_WSDL = "wsdl";
    public static final String NS_WSDL_VALUE = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_SOAP = "soap";
    public static final String NS_SOAP_VALUE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String NS_XS = "xs";
    public static final String NS_XSD = "xsd";
    public static final String NS_XSD_VALUE = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_PREFIX = "http://";
    private static final String TNS_PREFIX = "tns";
    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String XML_NS_NAMESPACE = "http://www.w3.org/XML/1998/namespace"; // implicit namespace

    public static void checkUnusedImports(String wsdl, AnalysisInformationCollector collector) {
        Path xq = Paths.get("wsdl", "namespace");
        try {
            String xqResult = XQuery.runXQuery(xq, "unusedImports.xq", wsdl);
            List<String> unused = XQuery.mapResult(xqResult, "namespace");
            for (String ns : unused) {
                collector.addWarning(ASSERTION_ID, "Namespace '" + ns + "' imported but not used",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    public static void checkInvalidImports(String wsdl, AnalysisInformationCollector collector) {
        Path xq = Paths.get("wsdl", "namespace");
        try {
            String xqResult = XQuery.runXQuery(xq, "invalidImports.xq", wsdl);
            List<String> invalid = XQuery.mapResult(xqResult, "namespace");
            for (String ns : invalid) {
                collector.addError(ASSERTION_ID, "Namespace import not allowed",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Namespace '" + ns +
                                "' not in target namespace or 'http://technical.schemas.nykreditnet.net/*'");
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }


    public static void checkNamespace(String wsdl, AnalysisInformationCollector collector) {
        Path xq = Paths.get("wsdl", "definition");
        try {
            String targetNamespace = "<unknown>";
            String namespaceXml = XQuery.runXQuery(xq, "namespace.xq", wsdl);
            List<Map<String, String>> ns = XQuery.mapResult(namespaceXml, "prefix", "namespaceUri");
            Map<String, String> namespaces = new HashMap<>();
            for (Map<String, String> namespace : ns) {
                String prefix = namespace.get("prefix");
                String uri = namespace.get("namespaceUri");
                if (!XML_NS_NAMESPACE.equals(uri)) { // ignore implicit xml ns namespace
                    if (TARGET_NAMESPACE.equals(prefix)) {
                        targetNamespace = uri;
                    } else {
                        // copy all namespace definitions except implicit xml ns and targetNamespace
                        namespaces.put(prefix, uri);
                    }
                }
            }
            checkStandardWsdlElementNS(namespaces, targetNamespace, collector);
            SchemaChecker.checkUnusedNamespacePrefix(wsdl, collector);
            checkCase(namespaces, collector);
            checkDuplicate(namespaces, collector);
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void checkStandardWsdlElementNS(Map<String, String> namespaces, String targetNamespace,
                                                   AnalysisInformationCollector collector) {
        // tns namespace must be defined and uri must be equal to target namespace
        if (namespaces.containsKey(TNS_PREFIX)) {
            if (!namespaces.get(TNS_PREFIX).equals(targetNamespace)) {
                collector.addError(ASSERTION_ID, "Namespace tns '" + namespaces.get("tns") +
                                "' does not match targetNamespace '" + targetNamespace + "'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        } else {
            collector.addError(ASSERTION_ID, "Namespace 'tns' not found top level <wsdl> element.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        // check that correct names are used
        // xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
        String wsdlNS = Utilities.getKeyForValue(NS_WSDL_VALUE, namespaces);
        if (!NS_WSDL.equals(wsdlNS)) {
            if (namespaces.containsValue(NS_WSDL_VALUE)) {
                collector.addWarning(ASSERTION_ID, "Expected namespace '" + NS_WSDL + "' but found '" + wsdlNS +
                                "' (" + NS_WSDL_VALUE + ")",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            } else {
                collector.addWarning(ASSERTION_ID, "Namespace '" + NS_WSDL_VALUE +
                                "' not found top level <wsdl> element.",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            }
        }
        // xmlns:soap="http://schemas.xmlsoap.org/wsdl/"
        String soapNS = Utilities.getKeyForValue(NS_SOAP_VALUE, namespaces);
        if (!NS_SOAP.equals(soapNS)) {
            if (namespaces.containsValue(NS_SOAP_VALUE)) {
                collector.addWarning(ASSERTION_ID, "Expected namespace '" + NS_SOAP + "' but found '" + soapNS +
                                "' (" + NS_SOAP_VALUE + ")",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            } else {
                collector.addWarning(ASSERTION_ID, "Namespace '" + NS_SOAP_VALUE +
                                "' not found top level <wsdl> element.",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            }
        }
        // xmlns:xsd="http://www.w3.org/2001/XMLSchema" or xmlns:xs="http://www.w3.org/2001/XMLSchema"
        String xsdNS = Utilities.getKeyForValue(NS_XSD_VALUE, namespaces);
        if (!(xsdNS.equals(NS_XSD) || xsdNS.equals(NS_XS))) {
            if (namespaces.containsValue(NS_XSD_VALUE)) {
                collector.addWarning(ASSERTION_ID, "Expected namespace '" + NS_XSD + "' or '" + NS_XS + "' but found '"
                                + xsdNS + "' (" + NS_XSD_VALUE + ")",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            } else {
                collector.addWarning(ASSERTION_ID, "Namespace '" + NS_XSD_VALUE +
                                "' not found top level <wsdl> element.",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            }
        }
    }

    private static void checkCase(Map<String, String> namespaces, AnalysisInformationCollector collector) {
        for (String key : namespaces.keySet()) {
            String value = namespaces.get(key);
            // check case
            if (!(NS_XSD_VALUE.equals(value) || NS_WSDL_VALUE.equals(value) || NS_SOAP_VALUE.equals(value))) {
                if (!value.equals(value.toLowerCase())) {
                    collector.addWarning(ASSERTION_ID, "Namespace must be in lowercase (" + value + ")",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
                }
            }
            if (!key.equals(key.toLowerCase())) {
                collector.addWarning(ASSERTION_ID, "Namespace prefix must be in lowercase (" + key + ")",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            }
            if (!value.toLowerCase().startsWith(NS_PREFIX)) {
                collector.addWarning(ASSERTION_ID, "Namespace must start with " + NS_PREFIX + " (" + value + ")",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
            }
        }
    }

    private static void checkDuplicate(Map<String, String> namespaces, AnalysisInformationCollector collector) {
        // check if namespace uris are duplicated - collect and report prefixes for these
        List<String> duplicate = new ArrayList<>();
        for (String key : namespaces.keySet()) {
            int count = 0;
            String value = namespaces.get(key);
            for (String key2 : namespaces.keySet()) {
                if (value.equals(namespaces.get(key2))) {
                    count++;
                }
            }
            if (count > 1) {
                duplicate.add(key);
            }
        }
        if (duplicate.size() > 0) {
            Collections.sort(duplicate);
            collector.addWarning(ASSERTION_ID, "Duplicate namespaces found",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Prefixes: " + Utilities.join(",", duplicate));
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector) {
        collector.addInfo(ASSERTION_ID, "Exception while checking namespaces",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
