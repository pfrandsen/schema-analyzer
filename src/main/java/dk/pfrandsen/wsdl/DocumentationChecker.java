package dk.pfrandsen.wsdl;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.WsdlUtil;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.util.XsdUtil;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class DocumentationChecker {
    public static String ASSERTION_ID_WSDL_DOC = "CA6-WSDL-Documentation-Validation";
    public static String ASSERTION_ID_XSD_DOC = "CA17-XSD-Documentation-Validation";
    private static int TEXT_LENGTH_MAX = 700;
    private static int TEXT_LENGTH_MIN = 5;

    private static boolean isAscii(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDanish(int ch) {
        int[] danish = {0xE6, 0xF8, 0xE5, 0xC6, 0xD8, 0xC5}; // æ, ø, å, Æ, Ø, Å
        for (int val : danish) {
            if (ch == val) {
                return true;
            }
        }
        return false;
    }

    private static String removeDanish(String text) {
        // Should be as simple as replaceAll("[æøåÆØÅ]", "") - but no luck
        StringBuffer retVal = new StringBuffer(text.length());
        for (int i = 0; i < text.length(); i++) {
            if (! isDanish(text.charAt(i))) {
                retVal.append(text.charAt(i));
            }
        }
        return retVal.toString();
    }

    private static void checkText(String text, String element, int maxLength, int minLength,
                                  AnalysisInformationCollector collector, String assertion) {
        String txt = text.trim();
        if (txt.length() == 0) {
            collector.addWarning(assertion, "No documentation",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element: " + element);
        } else if (txt.length() < minLength) {
            collector.addWarning(assertion, "Documentation below limit (" + minLength + ")",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element: " + element);
        }
        if (txt.length() > maxLength) {
            collector.addWarning(assertion, "Documentation exceed limit (" + maxLength + ")",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Element: " + element);
        }
        // check valid characters
        String noDanish = removeDanish(txt);
        if (txt.length() > noDanish.length()) {
            collector.addError(assertion, "Danish characters found",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element: " + element);
        }
        if (!isAscii(noDanish)) {
            collector.addWarning(assertion, "Non ASCII characters found",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element: " + element);
        }
        // check for "to do" text
        if (text.toLowerCase().contains("todo")) {
            collector.addWarning(assertion, "TODO found",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element: " + element);
        }
    }

    public static void checkWsdlDocumentation(String wsdl, AnalysisInformationCollector collector) {
        try {
            // get top level documentation
            String wsdlDoc = XQuery.mapSingleResult(XQuery.runXQuery(Paths.get("wsdl", "definition"),
                    "documentation.xq", wsdl), "documentation");
            checkText(wsdlDoc, "WSDL element (top level)", TEXT_LENGTH_MAX, TEXT_LENGTH_MIN, collector,
                    ASSERTION_ID_WSDL_DOC);
            // get operation documentation
            String operationsDoc = XQuery.runXQuery(Paths.get("wsdl", "operation"), "documentation.xq", wsdl);
            List<Map<String, String>> operations = XQuery.mapResult(operationsDoc, "name", "documentation", "portType");
            for (Map<String, String> operation : operations) {
                String operationName = operation.get("name");
                String operationDoc = operation.get("documentation");
                String element = "portType '" + operation.get("portType") + "' operation '" + operationName + "'";
                checkText(operationDoc, element, TEXT_LENGTH_MAX, TEXT_LENGTH_MIN, collector, ASSERTION_ID_WSDL_DOC);
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_WSDL_DOC);
        }
    }

    public static void checkConceptSchemaDocumentation(String xsd, AnalysisInformationCollector collector) {
        try {
            String tns = XsdUtil.getTargetNamespace(xsd);
            if (tns.startsWith("http://concept.")) { // only concept schemas requires documentation
                String elementsDoc = XQuery.runXQuery(Paths.get("xsd"), "documentation.xq", xsd);
                List<Map<String, String>> elements = XQuery.mapResult(elementsDoc, "name", "documentation");
                for (Map<String, String> element : elements) {
                    String elementName = element.get("name");
                    String elementDoc = element.get("documentation");
                    String elem = "'" + elementName + "'";
                    checkText(elementDoc, elem, TEXT_LENGTH_MAX, TEXT_LENGTH_MIN, collector, ASSERTION_ID_WSDL_DOC);
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_XSD_DOC);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking documentation",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }

}