package dk.pfrandsen.wsdl;

import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XQuery;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PortTypeChecker {
    public static final String ASSERTION_ID = "CA15-WSDL-PORTTYPE-VALIDATE";
    public static final String ASSERTION_ID_PORT_TYPE_MESSAGES = "CA16b-WSDL-PortType-Operation-Message-Validate";
    public static final String ASSERTION_ID_PORT_TYPE_FAULTS = "CA7-WSDL-PortType-Operation-Has-Fault-Message-Validate";
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
            collectException(e, collector, ASSERTION_ID);
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
            collectException(e, collector, ASSERTION_ID);

        }
    }

    public static void checkInputOutputMessagesAndFaults(String wsdl, AnalysisInformationCollector collector) {
        // check input, output and fault messages for all operations in all port types
        // fault must be specified if output message is present
        String faultNamespacePrefix = "http://technical.schemas.nykreditnet.net/fault";
        try {
            List<String> portTypes = WsdlUtil.getPortTypes(wsdl);
            for (String portType : portTypes) {
                Path xqPortType = Paths.get("wsdl", "porttype");
                String xqPortTypeOperations = XQuery.runXQuery(xqPortType, "operations.xq", wsdl, portType);
                List<Map<String, String>> portTypeOperations = Xml.parseXQueryResult(xqPortTypeOperations);
                for (Map<String, String> portTypeOperation : portTypeOperations) {
                    String operationName = portTypeOperation.get("name");
                    String inputName = portTypeOperation.get("input-local");
                    String outputName = portTypeOperation.get("output-local");
                    if (!inputName.endsWith(MessageChecker.validRequestPostfix)) {
                        collector.addError(ASSERTION_ID_PORT_TYPE_MESSAGES, "Invalid input message name",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Input message in portType '" +
                                        portType + "' operation '" + operationName + "' does not end with '" +
                                        MessageChecker.validRequestPostfix + "'");
                    }
                    if (!outputName.endsWith(MessageChecker.validResponsePostfix)) {
                        collector.addError(ASSERTION_ID_PORT_TYPE_MESSAGES, "Invalid output message name",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Output message in portType '" +
                                        portType + "' operation '" + operationName + "' does not end with '" +
                                        MessageChecker.validResponsePostfix + "'");
                    }
                    String xqFaults = XQuery.runXQuery(xqPortType, "operationFauts.xq", wsdl, portType, operationName);
                    List<Map<String, String>> faults = Xml.parseXQueryResult(xqFaults);
                    if (operationName.length() != 0 && faults.size() == 0) {
                        // should this be a warning
                        collector.addError(ASSERTION_ID_PORT_TYPE_FAULTS, "Missing fault",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Fault not defined for portType '" +
                                        portType + "', operation '" + operationName + "'");
                    }
                    for (Map<String, String> fault : faults) {
                        String faultName = fault.get("name");
                        String messageName = fault.get("message-local");

                        if (!messageName.endsWith(MessageChecker.validFaultPostfix)) {
                            collector.addError(ASSERTION_ID_PORT_TYPE_MESSAGES, "Invalid fault message name",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Fault message '" + messageName +
                                            "' in portType '" + portType + "' operation '" + operationName +
                                            "' does not end with '" + MessageChecker.validFaultPostfix + "'");
                        }
                        if (!messageName.equals(faultName)) {
                            collector.addError(ASSERTION_ID_PORT_TYPE_MESSAGES, "Invalid fault name",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Fault name '" + faultName +
                                            "' in portType '" + portType + "' operation '" + operationName +
                                            "' must be equal to message name '" + messageName + "'");
                        }
                    }
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_PORT_TYPE_MESSAGES);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking portTypes",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
