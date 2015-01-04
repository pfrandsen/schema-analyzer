package dk.pfrandsen.wsdl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import ch.ethz.mxquery.exceptions.MXQueryException;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.XQuery;

public class SoapBindingChecker {
    public static String ASSERTION_ID = "CA8-WSDL-SOAP-Binding-Validation";
    public static String DOCUMENT_STYLE = "document";
    public static String SOAP11_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static String BINDING_NAME_POSTFIX = "Binding";
    public static String TNS_PREFIX = "tns";

    // name must be <service name> + "Binding" postfix
    private static void checkBindingName(String name, String wsdl, AnalysisInformationCollector collector) {
        boolean validName = false;
        try {
            List<String> serviceNames = ServiceChecker.getServiceNames(wsdl, true);
            for (String serviceName : ServiceChecker.getServiceNames(wsdl, true)) {
                if (name.equals(serviceName + BINDING_NAME_POSTFIX)) {
                    validName = true;
                }
            }
            if (!validName) {
                String svcNames = "{" + Utilities.join(",", serviceNames) + "}";
                collector.addError(ASSERTION_ID, "Binding name is invalid" ,
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding name " + name +
                                " should be <serviceName>" + BINDING_NAME_POSTFIX + " where service name is " + svcNames);
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID);
        }
    }

    private static void checkBinding(String bindingName, String wsdl, AnalysisInformationCollector collector) throws IOException, MXQueryException {
        Path xqLocation = Paths.get("wsdl", "binding");
        String bindingXml = XQuery.runXQuery(xqLocation, "soapbinding.xq", wsdl, bindingName);
        List<Map<String, String>> bindings = XQuery.mapResult(bindingXml, "style", "transport", "version");
        if (bindings.size() == 1) {
            String style = bindings.get(0).get("style");
            String transport = bindings.get(0).get("transport");
            String version = bindings.get(0).get("version");
            if ("1.1".equals(version)) {
                if (!SOAP11_TRANSPORT.equals(transport)) {
                    collector.addWarning(ASSERTION_ID, "SOAP binding transport is not " + SOAP11_TRANSPORT,
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "SOAP transport found [" + transport + "]");
                }
                if (!DOCUMENT_STYLE.equals(style)) {
                    collector.addError(ASSERTION_ID, "SOAP binding style is not " + DOCUMENT_STYLE,
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "SOAP binding style found [" + style + "]");
                }
            } else {
                    collector.addError(ASSERTION_ID, "Binding is not SOAP 1.1",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Version detected is '" + version + "'.");
        }
    } else {
            collector.addError(ASSERTION_ID, "Error retrieving binding definition",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding name '" + bindingName + "'.");
        }
    }

    public static void checkBindings(String wsdl, AnalysisInformationCollector collector) {
        try {
            Path xqLocation = Paths.get("wsdl", "binding");
            String bindingsXml = XQuery.runXQuery(xqLocation, "binding.xq", wsdl);
            List<Map<String, String>> bindings = XQuery.mapResult(bindingsXml, "name", "type-pre");
            if (bindings.size() > 1) {
                collector.addError(ASSERTION_ID, "WSDL contains more than one binding",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Wsdl contains " + bindings.size() + " bindings.");
            }
            if (bindings.size() == 0) {
                collector.addError(ASSERTION_ID, "WSDL does not contain binding",
                        AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
            }
            // expecting exactly one binding, but checking all of them if there is more
            for (Map<String, String> binding : bindings) {
                String bindingName = binding.get("name");
                String typePrefix = binding.get("type-pre");
                checkBindingName(bindingName, wsdl, collector);
                checkBinding(bindingName, wsdl, collector);
                if (!TNS_PREFIX.equals(typePrefix)) {
                    collector.addWarning(ASSERTION_ID, "WSDL binding '" + bindingName + "' type does not use " +
                                    TNS_PREFIX + " namespace prefix",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Prefix used is '" + typePrefix + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking bindings",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}