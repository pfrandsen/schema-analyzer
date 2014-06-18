package dk.pfrandsen.wsdl.cxf.tools.validator;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.cxf.tools.common.ToolErrorListener;

public class ErrorListener extends ToolErrorListener {

    public static String ASSERTION_ID = "CXF-WSDL-Validation";

    private AnalysisInformationCollector collector;

    public ErrorListener() {
        this.collector = new AnalysisInformationCollector();
    }

    public AnalysisInformationCollector getCollector() {
        // return copy
        return new AnalysisInformationCollector(collector);
    }

    // The severity of the errors and warnings are unknown
    public void addError(String file, int line, int column, String message) {
        String details = file + " [" + line + "," + column + "]";
        collector.addError(ASSERTION_ID, message, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, details);
    }

    public void addError(String file, int line, int column, String message, Throwable t) {
        String details = file + " [" + line + "," + column + "," + t.getMessage() + "]";
        collector.addError(ASSERTION_ID, message, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, details);
    }

    public void addWarning(String file, int line, int column, String message) {
        String details = file + " [" + line + "," + column + "]";
        collector.addWarning(ASSERTION_ID, message, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, details);
    }

    public void addWarning(String file, int line, int column, String message, Throwable t) {
        String details = file + " [" + line + "," + column + "," + t.getMessage() + "]";
        collector.addWarning(ASSERTION_ID, message, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, details);
    }

}
