package dk.pfrandsen.xsd;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.util.XsdUtil;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SchemaChecker {
    public static String ASSERTION_ID_FORM_DEFAULT = "CA20-XSD-Form-Default";


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

    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking documentation",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }
}
