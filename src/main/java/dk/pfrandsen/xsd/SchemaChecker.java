package dk.pfrandsen.xsd;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.util.XsdUtil;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SchemaChecker {
    public static String ASSERTION_ID_FORM_DEFAULT = "CA20-XSD-Form-Default";
    public static String ASSERTION_ID_NILLABLE = "CA19-XSD-Nillable";
    public static String ASSERTION_ID_MIN_MAX = "CA54-XSD-Redundant-Min-Max-Occurs";


    public static void checkFormDefault(String xsd, AnalysisInformationCollector collector) {
        // elementFormDefault = 'qualified' attributeFormDefault = 'unqualified'
        try {
            String formAttr = XQuery.runXQuery(Paths.get("xsd"), "formDefault.xq", xsd);
            String element = XQuery.mapSingleResult(formAttr, "element");
            String attribute = XQuery.mapSingleResult(formAttr, "attribute");
            if (!"qualified".equals(element)) {
                collector.addError(ASSERTION_ID_FORM_DEFAULT,
                        "Value of attribute elementFormDefault must be 'qualified'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
            if (!"unqualified".equals(attribute)) {
                collector.addError(ASSERTION_ID_FORM_DEFAULT,
                        "Value of attribute attributeFormDefault must be 'unqualified'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_FORM_DEFAULT);
        }
    }

    public static void checkNillable(String xsd, AnalysisInformationCollector collector) {
        // nillable attribute must not be used
        try {
            String nill = XQuery.runXQuery(Paths.get("xsd"), "nillable.xq", xsd);
            List<Map<String,String>> elements = XQuery.mapResult(nill, "name", "nillable");
            for (Map<String,String> element : elements) {
                String elementName = element.get("name");
                String elementValue = element.get("nillable");
                collector.addError(ASSERTION_ID_NILLABLE, "Element must must not have nillable attribute",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Element '" + elementName +
                "', nillable='" + elementValue + "'");
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_NILLABLE);
        }
    }

    public static void checkMinMaxOccurs(String xsd, AnalysisInformationCollector collector) {
        try {
            String minMax = XQuery.runXQuery(Paths.get("xsd"), "minMaxOccurs.xq", xsd);
            List<Map<String,String>> items = XQuery.mapResult(minMax, "name", "node");
            for (Map<String,String> item : items) {
                String name = item.get("name");
                if ("".equals(name)) {
                    name = "<anonymous>";
                }
                String node = item.get("node");
                collector.addError(ASSERTION_ID_MIN_MAX, "Redundant minOccurs/maxOccurs='1'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Node '" + name + "' (" + node + ")");
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_MIN_MAX);
        }
    }

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking documentation",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }
}
