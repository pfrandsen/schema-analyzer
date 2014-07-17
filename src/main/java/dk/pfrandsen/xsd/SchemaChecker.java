package dk.pfrandsen.xsd;

import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.XQuery;
import dk.pfrandsen.util.XsdUtil;

import java.nio.file.Paths;
import java.util.*;

public class SchemaChecker {
    public static String ASSERTION_ID_FORM_DEFAULT = "CA20-XSD-Form-Default";
    public static String ASSERTION_ID_NILLABLE = "CA19-XSD-Nillable";
    public static String ASSERTION_ID_MIN_MAX = "CA54-XSD-Redundant-Min-Max-Occurs";
    public static String ASSERTION_ID_TYPE = "CA24-XSD-Type-Validate";
    public static String ASSERTION_ID_CONCEPT = "CA34-XSD-Illegal-Content-In-Concept-Scheme";


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

    public static void checkConceptTypes(String xsd, AnalysisInformationCollector collector) {
        try {
            String tns = XsdUtil.getTargetNamespace(xsd);
            if (XsdUtil.isConcept(tns)) {
                // find illegal include, import, simpleType, complexType
                String illegal = XQuery.runXQuery(Paths.get("xsd"), "illegalConceptTypes.xq", xsd);
                List<Map<String,String>> items = XQuery.mapResult(illegal, "name", "node");
                for (Map<String,String> item : items) {
                    String name = item.get("name");
                    String node = item.get("node");
                    collector.addError(ASSERTION_ID_CONCEPT, "Illegal content in concept schema",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Node '" + name + "' (" + node + ")");
                }
                // check for unused legal top-level simpleType definitions (enumerations)
                String unused = XQuery.runXQuery(Paths.get("xsd"), "unusedEnumeration.xq", xsd);
                for (String name : XQuery.mapResult(unused, "name")) {
                    collector.addError(ASSERTION_ID_CONCEPT, "Unused enumeration in concept schema",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Enumeration '" + name + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_CONCEPT);
        }
    }

    public static void checkTypes(String xsd, AnalysisInformationCollector collector) {
        try {
            String embedded = XQuery.runXQuery(Paths.get("xsd"), "anonymousEnumeration.xq", xsd);
            List<Map<String,String>> items = XQuery.mapResult(embedded, "name", "node");
            for (Map<String,String> item : items) {
                String name = item.get("name");
                if ("".equals(name)) {
                    name = "<anonymous>";
                }
                String node = item.get("node");
                collector.addError(ASSERTION_ID_TYPE, "Embedded (anonymous) type found",
                        AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Node '" + name + "' (" + node + ")");
            }
            // check name for all top level types
            String topLevel = XQuery.runXQuery(Paths.get("xsd"), "topLevelTypes.xq", xsd);
            items = XQuery.mapResult(topLevel, "name", "node");
            for (Map<String,String> item : items) {
                String name = item.get("name");
                String node = item.get("node");
                if (!XsdUtil.isValidTypeName(name)) {
                    collector.addError(ASSERTION_ID_TYPE, "Illegal type name",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Type '" + name + "' (" + node + ")");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_TYPE);
        }
    }

    public static void checkElements(String xsd, AnalysisInformationCollector collector) {
        try {
            // check name for all explicitly named elements
            String elementNames = XQuery.runXQuery(Paths.get("xsd"), "elementNames.xq", xsd);
            for (String name : XQuery.mapResult(elementNames, "name")) {
                if (!XsdUtil.isValidElementName(name)) {
                    collector.addError(ASSERTION_ID_TYPE, "Illegal element name",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Element '" + name + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_TYPE);
        }
    }

    public static void checkBetaNamespace(String xsd, AnalysisInformationCollector collector) {
        try {
            String ns = XQuery.runXQuery(Paths.get("xsd"), "namespaces.xq", xsd);
            List<String> namespaces = XQuery.mapResult(ns, "namespace");
            // remove duplicates as XQuery distinct-values does not work in the current implementation
            Set<String> nsSet = new LinkedHashSet<>(namespaces);
            for (String namespace : nsSet) {
                if (namespace.contains("beta-")) {
                    collector.addError(ASSERTION_ID_CONCEPT, "Namespace containing 'beta-' found",
                            AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Namespace '" + namespace + "'");
                }
            }
        } catch (Exception e) {
            collectException(e, collector, ASSERTION_ID_CONCEPT);
        }
    }
    private static void collectException(Exception e, AnalysisInformationCollector collector, String assertion) {
        collector.addInfo(assertion, "Exception while checking schema",
                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
    }
}
