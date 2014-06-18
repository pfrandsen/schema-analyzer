package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;

import java.util.HashMap;
import java.util.Map;

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

    public static void checkNamespace(Definitions definitions, AnalysisInformationCollector collector) {
        String tns = definitions.getTargetNamespacePrefix();
        String targetNamespace = definitions.getTargetNamespace();

        // find all the wsdl element namespaces
        Map<String, String> wsdlElementNS = getWsdlElementNamespaces(definitions);
        checkStandardWsdlElementNS(wsdlElementNS, definitions, collector);
        checkUsageAndCase(wsdlElementNS, definitions, collector);
    }

    private static void checkStandardWsdlElementNS(Map<String, String> namespaces, Definitions definitions,
                                                   AnalysisInformationCollector collector) {
        // tns namespace should be defined
        if (namespaces.containsKey("tns")) {
            if (!namespaces.get("tns").equals(definitions.getTargetNamespace())) {
                collector.addError(ASSERTION_ID, "Namespace tns '" + namespaces.get("tns") +
                                "' does not match targetNamespace '" + definitions.getTargetNamespace() + "'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        } else {
            collector.addError(ASSERTION_ID, "Namespace 'tns' not found top level <wsdl> element.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        // check that correct names are used
        // xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
        String wsdlNS = Utilities.getKeyForValue(NS_WSDL_VALUE, namespaces);
        if (! NS_WSDL.equals(wsdlNS)) {
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
        if (! NS_SOAP.equals(soapNS)) {
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

    private static void checkUsageAndCase(Map<String, String> namespaces, Definitions definitions,
                                          AnalysisInformationCollector collector) {
        // find the namespaces that are defined but not used by matching strings in the text representation of the wsdl
        // and check that all namespace values are in lowercase

        String wsdlNS = Utilities.getKeyForValue(NS_WSDL_VALUE, namespaces);
        String xsdNS = Utilities.getKeyForValue(NS_XSD_VALUE, namespaces);

        String xml = definitions.getAsString();
        for (String key : namespaces.keySet()) {
            String value = namespaces.get(key);
            // the prefix wsdl is stripped from the string representation so do not try to match this; and the xml
            // schema namespace is represented as xsd:
            if (!key.equals(wsdlNS)) {
                String message = "Namespace xmlns:" + key + "='" + value + "' not used.";
                if (key.equals(xsdNS)) {
                    key = "xsd"; // the string representation of the wsdl uses xsd: for http://www.w3.org/2001/XMLSchema
                }
                String toMatch = "(?s).*['<]" + key + ":(?s).*";  // 'ns:' or '<ns:'
                if (!xml.matches(toMatch)) {
                    collector.addWarning(ASSERTION_ID, message, AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
                }
            }
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

    public static Map<String, String> getWsdlElementNamespaces(Definitions definitions) {
        return getNamespaceMap(definitions.getNamespaceContext());
    }

    /*
    Extract namespaces map defined as Map<String, String> in com.predic8.soamodel.XMLElement (Groovy class)
    The issue with the definition i Groovy is that the return value in un-typed
     */
    private static Map<String, String> getNamespaceMap(Object context) {
        Map<String, String> namespaces = new HashMap<String, String>();
        if (context instanceof Map) {
            Map source = (Map)context;
            for (Object key : source.keySet()) {
                Object value = source.get(key);
                if (key instanceof String && value instanceof String) {
                    namespaces.put((String)key, (String)value);
                }
            }
        }
        return namespaces;
    }

}
