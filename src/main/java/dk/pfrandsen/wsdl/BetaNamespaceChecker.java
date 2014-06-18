package dk.pfrandsen.wsdl;

import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.QName;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XMLSource;

import com.predic8.wsdl.Definitions;
import dk.pfrandsen.Xml;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.util.XQuery;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class BetaNamespaceChecker {
    public static String ASSERTION_ID = "CA28-WSDL-NO-BETA-NAMESPACE";
    private static Path xqLocationDefinitions = Paths.get("wsdl", "definition");
    private static Path xqLocationTypes = Paths.get("wsdl", "types");

    public static void checkBetaNamespace(String wsdl, AnalysisInformationCollector collector) {
        checkBetaNamespaceDefinitions(wsdl, collector);
        checkBetaNamespaceImports(wsdl, collector);
    }

    public static void checkBetaNamespaceDefinitions(String wsdl, AnalysisInformationCollector collector) {
        try {
            String namespaces = XQuery.runXQuery(xqLocationDefinitions, "namespace.xq", wsdl);
            List<Map<String, String>> result = Xml.parseXQueryResult(namespaces);
            if (result.size() > 0) {
                for (Map<String, String> namespace : result) {
                    // the keys corresponds to the xml tags in the XQuery source file
                    String namespacePrefix = namespace.get("prefix");
                    String namespaceUri = "" + namespace.get("namespaceUri");
                    if (namespaceUri.toLowerCase().contains("beta-")) {
                        collector.addError(ASSERTION_ID, "wsdl:definitions references beta namespace in '" + namespacePrefix + "'",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, namespaceUri);
                    }
                }
            }
        } catch (IOException e) {
            collector.addInfo(ASSERTION_ID, "IOException while checking namespaces",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        } catch (MXQueryException e) {
            collector.addInfo(ASSERTION_ID, "XQuery exception while checking namespaces",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        }
    }

    public static void checkBetaNamespaceImports(String wsdl, AnalysisInformationCollector collector) {
        try {
            String namespaces = XQuery.runXQuery(xqLocationTypes, "import.xq", wsdl);
            List<Map<String, String>> result = Xml.parseXQueryResult(namespaces);
            if (result.size() > 0) {
                for (Map<String, String> namespace : result) {
                    // the keys corresponds to the xml tags in the XQuery source file
                    String ns = namespace.get("namespace");
                    String schemaLocation = "" + namespace.get("schemaLocation");
                    if (ns.toLowerCase().contains("beta-")) {
                        collector.addError(ASSERTION_ID, "wsdl:types references beta namespace in import '" + ns + "'",
                                AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "schemaLocation: " + schemaLocation);
                    }
                }
            }
        } catch (IOException e) {
            collector.addInfo(ASSERTION_ID, "IOException while checking namespaces",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        } catch (MXQueryException e) {
            collector.addInfo(ASSERTION_ID, "XQuery exception while checking namespaces",
                    AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, e.getMessage());
        }
    }

}
