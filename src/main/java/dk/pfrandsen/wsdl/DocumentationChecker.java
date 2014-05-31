package dk.pfrandsen.wsdl;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Documentation;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.PortType;
import dk.pfrandsen.check.AnalysisInformationCollector;

public class DocumentationChecker {
    public static String ASSERTION_ID = "CA6-WSDL-Documentation-Validation";
    private static int TEXT_LENGTH = 700;

    public static boolean isAscii(String text) {
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

    private static void checkText(String text, String element, int maxLength, AnalysisInformationCollector collector) {
        String txt = text.trim();
        if (txt.length() == 0) {
            collector.addWarning(ASSERTION_ID, "No documentation for " + element, AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        if (txt.length() > maxLength) {
            collector.addWarning(ASSERTION_ID, "Documentation for " + element + " exceed limit (" + maxLength + ")",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
        }
        // check valid characters
        String noDanish = removeDanish(txt);
        if (txt.length() > noDanish.length()) {
            collector.addError(ASSERTION_ID, "Danish characters found in " + element, AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        if (!isAscii(noDanish)) {
            collector.addWarning(ASSERTION_ID, "Non ASCII characters found in " + element, AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
    }

    public static void checkDocumentation(Definitions definitions, AnalysisInformationCollector collector) {
        Documentation wsdlDocumentation = definitions.getDocumentation();
        String wsdlDoc = wsdlDocumentation != null ? wsdlDocumentation.getContent() : "";
        checkText(wsdlDoc, "WSDL element (top level)", TEXT_LENGTH, collector);
        for (PortType portType : definitions.getLocalPortTypes()) {
            // portType.getDocumentation(); -- no rule for documentation on port type
            String portTypeName = portType.getName();
            for (Operation operation : portType.getOperations()) {
                String operationName = operation.getName();
                Documentation operationDocumentation = operation.getDocumentation();
                String element = "portType [" + portTypeName + "] operation [" + operationName + "]";
                String opDoc = operationDocumentation != null ? operationDocumentation.getContent() : "";
                checkText(opDoc, element, TEXT_LENGTH, collector);
            }
        }
    }
}