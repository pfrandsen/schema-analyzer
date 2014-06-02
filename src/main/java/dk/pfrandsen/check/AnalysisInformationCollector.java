package dk.pfrandsen.check;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisInformationCollector {
    public static int SEVERITY_LEVEL_CRITICAL = 1;
    public static int SEVERITY_LEVEL_MAJOR = 2;
    public static int SEVERITY_LEVEL_MINOR = 3;
    public static int SEVERITY_LEVEL_COSMETIC = 4;

    private List<AnalysisInformation> errors;
    private List<AnalysisInformation> warnings;
    private List<AnalysisInformation> info;

    public AnalysisInformationCollector() {
        errors = new ArrayList<AnalysisInformation>();
        warnings = new ArrayList<AnalysisInformation>();
        info = new ArrayList<AnalysisInformation>();
    }

    public List<AnalysisInformation> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<AnalysisInformation> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public List<AnalysisInformation> getInfo() {
        return Collections.unmodifiableList(info);
    }

    public int getErrorCount() {
        return errors.size();
    }

    public void addError(String assertion, String message, int severity) {
        addError(assertion, message, severity, "");
    }

    public void addError(String assertion, String message, int severity, String details) {
        add(errors, assertion, message, severity, details);
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public void addWarning(String assertion, String message, int severity) {
        addWarning(assertion, message, severity, "");
    }

    public void addWarning(String assertion, String message, int severity, String details) {
        add(warnings, assertion, message, severity, details);
    }

    public int getInfoCount() {
        return info.size();
    }

    public void addInfo(String assertion, String message, int severity) {
        addInfo(assertion, message, severity, "");
    }

    public void addInfo(String assertion, String message, int severity, String details) {
        add(info, assertion, message, severity, details);
    }

    private void add(List<AnalysisInformation> collection, String assertion, String message, int severity, String details) {
        collection.add(new AnalysisInformation(assertion, message, severity, details));
    }
}
