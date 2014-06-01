package dk.pfrandsen.wsdl;

import java.util.List;

import com.predic8.wsdl.AbstractBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.soap11.SOAPBinding;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;

public class SoapBindingChecker {
    public static String ASSERTION_ID = "CA8-WSDL-SOAP-Binding-Validation";
    public static String DOCUMENT_LITERAL = "Document/Literal";
    public static String SOAP11_STYLE = "document";
    public static String SOAP11_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static String BINDING_NAME_POSTFIX = "Binding";
    public static String TNS_PREFIX = "tns";

    // name must be service name + "Binding"
    private static void checkBindingName(String name, Definitions definitions, AnalysisInformationCollector collector) {
        boolean validName = false;
        for (String serviceName : ServiceChecker.getServiceNames(definitions, true)) {
            if (name.equals(serviceName + BINDING_NAME_POSTFIX)) {
                validName = true;
            }
        }
        if (!validName) {
            String serviceNames = "{" + Utilities.join(",", ServiceChecker.getServiceNames(definitions, true)) + "}";
            collector.addError(ASSERTION_ID, "Binding name is invalid" ,
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding name " + name +
                            " should be <serviceName>" + BINDING_NAME_POSTFIX + " where service name is " + serviceNames);
        }
    }

    private static void checkBinding(Binding binding, Definitions definitions, AnalysisInformationCollector collector) {
        AbstractBinding innerBinding = binding.getBinding(); // soap:binding inside wsdl:binding
        if (innerBinding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding)innerBinding;
            String transport = soapBinding.getTransport();
            String style = "";
            if (soapBinding.getStyle() instanceof String) {
                style = (String)soapBinding.getStyle();
            }
            if (!SOAP11_TRANSPORT.equals(transport)) {
                collector.addWarning(ASSERTION_ID, "SOAP binding transport is not " + SOAP11_TRANSPORT,
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "SOAP transport found [" + transport + "]");
            }
            if (!SOAP11_STYLE.equals(style)) {
                collector.addWarning(ASSERTION_ID, "SOAP binding style is not " + SOAP11_STYLE,
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "SOAP binding style found [" + style + "]");
            }
        } else {
            collector.addError(ASSERTION_ID, "Binding is not SOAP 1.1",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        checkBindingName("" + binding.getName(), definitions, collector);
        if (!DOCUMENT_LITERAL.equals(binding.getStyle())) {
            collector.addError(ASSERTION_ID, "Binding is not " + DOCUMENT_LITERAL,
                    AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Binding detected as [" + binding.getStyle() + "]");
        }
        if (binding.getType() != null) {
            if (!TNS_PREFIX.equals(binding.getType().getPrefix())) {
                collector.addWarning(ASSERTION_ID, "WSDL binding type does not use " + TNS_PREFIX + " namespace prefix",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Namespace prefix used '" + binding.getType().getPrefix() + "'");
            }
        }
    }

    public static void checkBindings(Definitions definitions, AnalysisInformationCollector collector) {
        List<Binding> bindings = definitions.getLocalBindings();
        if (bindings.size() > 1) {
            collector.addError(ASSERTION_ID, "WSDL contains more than one binding",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Wsdl contains " + bindings.size() + " bindings.");
        }
        if (bindings.size() == 0) {
            collector.addError(ASSERTION_ID, "WSDL does not contain binding",
                    AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
        }
        // expecting exactly one binding, but checking all of them if there is more
        for (Binding binding : bindings) {
            checkBinding(binding, definitions, collector);
        }
    }

}