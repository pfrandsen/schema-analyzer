package dk.pfrandsen.wsdl;

import ch.ethz.mxquery.exceptions.MXQueryException;
import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.XQuery;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/*
  ComplexType, simpleType and element definitions must not be defined in the WSDL.
  Elements must be imported/included to the WSDL.
 */

public class SchemaTypesChecker {
    public static final String ASSERTION_ID = "CA11-WSDL-SCHEMA-TYPE-AND-USAGE";
    private static Path xqLocation = Paths.get("wsdl", "types");

    public static void checkSchemaTypes(String wsdl, AnalysisInformationCollector collector) {
        checkElements(wsdl, collector);
        checkComplexTypes(wsdl, collector);
        checkSimpleTypes(wsdl, collector);
    }

    public static void checkElements(String wsdl, AnalysisInformationCollector collector) {
        try {
            String elements = XQuery.runXQuery(xqLocation, "element.xq", wsdl);
            List<Map<String, String>> result = Xml.parseXQueryResult(elements);
            if (result.size() > 0) {
                for (Map<String, String> element : result) {
                    // the keys corresponds to the xml tags in the XQuery source file
                    String elementName = element.get("name");
                    String elementValue = element.get("element");
                    collector.addError(ASSERTION_ID, "wsdl:types/xsd:schema contains element '" + elementName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, elementValue);
                }
            }
        } catch (IOException e) {
            collector.addInfo(ASSERTION_ID, "IOException while checking elements",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        } catch (MXQueryException e) {
            collector.addInfo(ASSERTION_ID, "XQuery exception while checking elements",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        }
    }

    public static void checkComplexTypes(String wsdl, AnalysisInformationCollector collector) {
        try {
            String complexTypes = XQuery.runXQuery(xqLocation, "complexType.xq", wsdl);
            List<Map<String, String>> result = Xml.parseXQueryResult(complexTypes);
            if (result.size() > 0) {
                for (Map<String, String> complexType : result) {
                    // the keys corresponds to the xml tags in the XQuery source file
                    String typeName = complexType.get("name");
                    String typeValue = complexType.get("complexType");
                    collector.addError(ASSERTION_ID, "wsdl:types/xsd:schema contains complexType '" + typeName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, typeValue);
                }
            }
        } catch (IOException e) {
            collector.addInfo(ASSERTION_ID, "IOException while checking complexTypes",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        } catch (MXQueryException e) {
            collector.addInfo(ASSERTION_ID, "XQuery exception while checking complexTypes",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        }
    }

    public static void checkSimpleTypes(String wsdl, AnalysisInformationCollector collector) {
        try {
            String simpleTypes = XQuery.runXQuery(xqLocation, "simpleType.xq", wsdl);
            List<Map<String, String>> result = Xml.parseXQueryResult(simpleTypes);
            if (result.size() > 0) {
                for (Map<String, String> simpleType : result) {
                    // the keys corresponds to the xml tags in the XQuery source file
                    String typeName = simpleType.get("name");
                    String typeValue = simpleType.get("simpleType");
                    collector.addError(ASSERTION_ID, "wsdl:types/xsd:schema contains simpleType '" + typeName + "'",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, typeValue);
                }
            }
        } catch (IOException e) {
            collector.addInfo(ASSERTION_ID, "IOException while checking simpleTypes",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        } catch (MXQueryException e) {
            collector.addInfo(ASSERTION_ID, "XQuery exception while checking simpleTypes",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        }
    }

}
