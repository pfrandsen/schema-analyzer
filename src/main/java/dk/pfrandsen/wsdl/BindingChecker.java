package dk.pfrandsen.wsdl;

import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XQuery;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class BindingChecker {
    public static final String ASSERTION_ID_SOAP_ACTION = "CA12-WSDL-Binding-SoapAction-Validate";
    public static final String ASSERTION_ID_OPERATION_FAULTS = "CA12-WSDL-Binding-SoapAction-Validate";

    public static void checkSoapAction(String wsdl, AnalysisInformationCollector collector) {
        Path xqLocation = Paths.get("wsdl", "binding");
        // get all bindings; for each get all operations and and check that soapAction matches namespace and message
        // local name for the corresponding portType operation input message
        try {
            String xq = XQuery.runXQuery(xqLocation, "binding.xq", wsdl);
            List<Map<String, String>> bindings = Xml.parseXQueryResult(xq);
            for (Map<String, String>  binding : bindings) {
                String bindingName = binding.get("name");
                String portTypeName = binding.get("type-local");
                checkSoapActionForBinding(bindingName, portTypeName, wsdl, collector);
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_SOAP_ACTION);
        }
    }

    public static void checkFaults(String wsdl, AnalysisInformationCollector collector) {
        Path xqLocation = Paths.get("wsdl", "binding");
        try {
            List<String> bindings = WsdlUtil.getBindings(wsdl);
            for (String binding : bindings) {
                String xq = XQuery.runXQuery(xqLocation, "operations.xq", wsdl, binding);
                List<String> operations = XQuery.mapResult(xq, "name");
                for (String operation : operations) {
                    xq = XQuery.runXQuery(xqLocation, "operationFaults.xq", wsdl, binding, operation);
                    List<Map<String, String>> faults = Xml.parseXQueryResult(xq);
                    for (Map<String, String>  fault : faults) {
                        String faultName = fault.get("name");
                        String soapFaultName = fault.get("soap-fault-name");
                        String soapFaultUse= fault.get("soap-fault-use");
                        if (!faultName.equals(soapFaultName)) {
                            collector.addError(ASSERTION_ID_OPERATION_FAULTS, "Fault name and soap fault name differ",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding '" + binding +
                                            "', operation '" + operation + "', fault '" + faultName +
                                            "' <> soap fault '" + soapFaultName + "'");
                        }
                        if (!"literal".equals(soapFaultUse)) {
                            collector.addError(ASSERTION_ID_OPERATION_FAULTS, "Fault use attribute not \"literal\"",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding '" + binding +
                                            "', operation '" + operation + "', fault '" + faultName +
                                            "' use='" + soapFaultUse + "'");
                        }
                    }
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_OPERATION_FAULTS);
        }
    }

    private static void checkSoapActionForBinding(String bindingName, String portTypeName, String wsdl,
                                                 AnalysisInformationCollector collector) {
        Path xqBinding = Paths.get("wsdl", "binding");
        Path xqPortType = Paths.get("wsdl", "porttype");
        try {
            String xqBindingOperations = XQuery.runXQuery(xqBinding, "bindingSoapAction.xq", wsdl, bindingName);
            List<Map<String, String>> bindingOperations = Xml.parseXQueryResult(xqBindingOperations);
            String xqPortTypeOperations = XQuery.runXQuery(xqPortType, "operations.xq", wsdl, portTypeName);
            List<Map<String, String>> portTypeOperations = Xml.parseXQueryResult(xqPortTypeOperations);
            for (Map<String, String> operation : bindingOperations) {
                String operationName = operation.get("name");
                String soapAction = operation.get("soapAction");
                if (soapAction.length() == 0) {
                    collector.addError(ASSERTION_ID_SOAP_ACTION, "Missing or empty soapAction",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding '" + bindingName +
                                    "', operation '" + operationName + "'");
                } else {
                    // find matching portType operation
                    Map<String, String> matchingOperation = null;
                    for (Map<String, String> portTypeOperation : portTypeOperations) {
                        if (operationName.equals(portTypeOperation.get("name"))) {
                            matchingOperation = portTypeOperation;
                        }
                    }
                    if (matchingOperation == null) {
                        collector.addError(ASSERTION_ID_SOAP_ACTION, "No matching portType operation found",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding '" + bindingName +
                                        "', operation '" + operationName + "'");
                    } else {
                        String expectedSoapAction = matchingOperation.get("input-ns") + "/" +
                                matchingOperation.get("input-local");
                        if (!soapAction.equals(expectedSoapAction)) {
                            collector.addError(ASSERTION_ID_SOAP_ACTION, "Invalid soapAction",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Binding '" + bindingName +
                                            "', operation '" + operationName + "', expected soapAction '" +
                            expectedSoapAction + "' (found '" + soapAction + "')");
                        }
                    }
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_SOAP_ACTION);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking bindings",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
