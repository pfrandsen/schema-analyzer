package dk.pfrandsen.wsdl;

import ch.ethz.mxquery.exceptions.MXQueryException;
import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XQuery;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MessageChecker {
    public static final String ASSERTION_ID = "CA16a-WSDL-Message-Name-Validate";
    public static final String validRequestPostfix = "Request";
    public static final String validResponsePostfix = "Response";
    public static final String validFaultPostfix = "Fault";

    public static List<String> getKnownFaultNames() {
        String [] faults = new String[] {
                "ValidationFault", "NotFoundFault", "StaleDataFault", "UncategorizedFault"
        };
        return Arrays.asList(faults);
    }

    public static List<String> getKnownFaultNamespaces() {
        String [] faults = new String[] {
                "http://technical.schemas.nykreditnet.net/fault/v1"
        };
        return Arrays.asList(faults);
    }

    public static String getValidFaultNamespacePrefix() {
        return "http://technical.schemas.nykreditnet.net/fault";
    }

    public static List<String> getKnownHeaderNames() {
        String [] headers = new String[] {
                "Applications",
                "User", "Proxy", "ServiceConsumer", // caller
                "Filtering", "Logging", "RequestorIdentity"
        };
        return Arrays.asList(headers);
    }

    public static List<String> getKnownHeaderNamespaces() {
        String [] headers = new String[] {
                "http://technical.schemas.nykreditnet.net/header/application/v1",
                "http://technical.schemas.nykreditnet.net/header/caller/v1",
                "http://technical.schemas.nykreditnet.net/header/filtering/v1",
                "http://technical.schemas.nykreditnet.net/header/logging/v1",
                "http://technical.schemas.nykreditnet.net/header/requestoridentity/v1"
        };
        return Arrays.asList(headers);
    }

    // get all messages - list of message names
    // for each message get parts (name, element, type) - check only one part, only element used

    public static void checkMessageNames(String wsdl, AnalysisInformationCollector collector) {
        Path xq = Paths.get("wsdl", "message");
        try {
            String xml = XQuery.runXQuery(xq, "messages.xq", wsdl);
            List<String> messages = XQuery.mapResult(xml, "name");
            for (String message : messages) {
                if (!Utilities.isLowerCamelCaseAscii(message)) {
                    collector.addError(ASSERTION_ID, "Message name must be lower camel case",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR,
                            "Message '" + message + "'");
                }
                // check for known faults and headers
                if (message.endsWith(validFaultPostfix)) {
                    if (!Utilities.toLowerCamelCase(getKnownFaultNames()).contains(message)) {
                        collector.addWarning(ASSERTION_ID, "Unknown fault message",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" + message + "', " +
                                "[" + Utilities.join(",", Utilities.toLowerCamelCase(getKnownFaultNames())) + "]");
                    }
                    // check namespace
                    //  getValidFaultNamespacePrefix

                }
                if ( (!message.endsWith(validRequestPostfix)) && (!message.endsWith(validResponsePostfix)) &&
                        (!message.endsWith(validFaultPostfix)) ) {
                    // must be header message
                    if (!Utilities.toLowerCamelCase(getKnownHeaderNames()).contains(message)) {
                        collector.addWarning(ASSERTION_ID, "Unknown header message",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" + message + "', [" +
                                        Utilities.join(",", Utilities.toLowerCamelCase(getKnownHeaderNames())) + "]");
                    }
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    public static void checkMessageParts(String wsdl, AnalysisInformationCollector collector) {
        // there should be only one part, element should be defined and not type
        // element name should be upper camel case
        // name of part should be lower camel case (of element name)

        Path xq = Paths.get("wsdl", "message");
        try {
            String xml = XQuery.runXQuery(xq, "messages.xq", wsdl);
            List<String> messages = XQuery.mapResult(xml, "name");
            for (String message : messages) {
                checkPart(message, wsdl, collector);
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void checkPart(String messageName, String wsdl, AnalysisInformationCollector collector) {
        Path xq = Paths.get("wsdl", "message");
        try {
            String xml = XQuery.runXQuery(xq, "message.xq", wsdl, messageName);
            List<Map<String, String>> result = Xml.parseXQueryResult(xml);
            if (result.size() == 0) {
                collector.addError(ASSERTION_ID, "Message '" + messageName + "', does not contain part",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" + messageName + "', " +
                                "[" + Utilities.join(",", Utilities.toLowerCamelCase(getKnownFaultNames())) + "]");
            }
            if (result.size() > 1) {
                List<String> parts = new ArrayList<>();
                for (Map<String, String> part : result) {
                    parts.add(part.get("name"));
                }
                collector.addError(ASSERTION_ID, "Message contains multiple parts",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Parts found " +
                                "[" + Utilities.join(",", parts) + "]");
            }
            for (Map<String, String> part : result) {
                String partName = part.get("name");
                String typeName = part.get("type-local");
                String elementName = part.get("element-local");
                if (typeName.length() > 0) {
                    collector.addError(ASSERTION_ID, "Message part must not use type, use element",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Found type '" +
                                    typeName + "' in part '" + partName + "' of message '" + messageName + "'");
                }
                if (elementName.length() > 0) {
                    if (!Utilities.isUpperCamelCaseAscii(elementName)) {
                        collector.addError(ASSERTION_ID, "Element name must be UpperCamelCase",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" +
                                        messageName + "' part '" + partName + "' element '" + elementName + "'");
                    }
                    if (!Utilities.isLowerCamelCaseAscii(partName)) {
                        collector.addError(ASSERTION_ID, "Part name must be lowerCamelCase",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" +
                                        messageName + "' part '" + partName + "'");
                    }
                    if (!messageName.equals(Utilities.toLowerCamelCase(elementName))) {
                        collector.addError(ASSERTION_ID, "Element name (in lowerCamelCase) must match message name",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" +
                                        messageName + "' part '" + partName + "' element '" + elementName + "'");
                    }
                    // part name must match lower case version of element name
                    if (!partName.equals(Utilities.toLowerCamelCase(elementName))) {
                        collector.addError(ASSERTION_ID, "Element name (in lowerCamelCase) must match part name",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Message '" +
                                        messageName + "' part '" + partName + "' element '" + elementName + "'");
                    }
                    if (messageName.endsWith(validFaultPostfix)) {
                        String elementNamespace = part.get("element-namespace");
                        if (!elementNamespace.startsWith(getValidFaultNamespacePrefix())) {
                            collector.addError(ASSERTION_ID, "Element namespace not valid",
                                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Fault message '" +
                                            messageName + "' part '" + partName + "' element '" + elementName + "' " +
                            "must be in namespace under '" + getValidFaultNamespacePrefix() + "', namespace found '" +
                            elementNamespace + "'");
                        } else {
                            if (!getKnownFaultNamespaces().contains(elementNamespace)) {
                                collector.addWarning(ASSERTION_ID, "Unknown fault namespace",
                                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Namespace '" +
                                elementNamespace + "' not in known namespaces [" +
                                                Utilities.join(",", getKnownFaultNamespaces()) + "]");
                            }
                        }
                    }
                } else {
                    collector.addError(ASSERTION_ID, "Message part must include element",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "No element in part '" +
                                    partName + "' of message '" + messageName + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector) {
        collector.addInfo(ASSERTION_ID, "Exception while checking messages",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}
