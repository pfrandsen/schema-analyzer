package dk.pfrandsen.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class AnalysisInformationCollector {
    public static int SEVERITY_LEVEL_UNKNOWN = -1;
    public static int SEVERITY_LEVEL_FATAL = 0;
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

    public AnalysisInformationCollector(AnalysisInformationCollector collector) {
        errors = new ArrayList<AnalysisInformation>();
        warnings = new ArrayList<AnalysisInformation>();
        info = new ArrayList<AnalysisInformation>();
        for (AnalysisInformation error : collector.errors) {
            errors.add(new AnalysisInformation(error));
        }
        for (AnalysisInformation warning : collector.warnings) {
            warnings.add(new AnalysisInformation(warning));
        }
        for (AnalysisInformation i : collector.info) {
            info.add(new AnalysisInformation(i));
        }
    }

    public void add(AnalysisInformationCollector collector) {
        for (AnalysisInformation error : collector.errors) {
            errors.add(new AnalysisInformation(error));
        }
        for (AnalysisInformation warning : collector.warnings) {
            warnings.add(new AnalysisInformation(warning));
        }
        for (AnalysisInformation i : collector.info) {
            info.add(new AnalysisInformation(i));
        }
    }

    public List<AnalysisInformation> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    // used by json de-serializer
    public void setErrors(List<AnalysisInformation> errors) {
        this.errors = errors;
    }

    public List<AnalysisInformation> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    // used by json de-serializer
    public void setWarnings(List<AnalysisInformation> warnings) {
        this.warnings = warnings;
    }

    public List<AnalysisInformation> getInfo() {
        return Collections.unmodifiableList(info);
    }

    // used by json de-serializer
    public void setInfo(List<AnalysisInformation> info) {
        this.info = info;
    }

    public int errorCount() {
        return errors.size();
    }

    public void addError(String assertion, String message, int severity) {
        addError(assertion, message, severity, "");
    }

    public void addError(String assertion, String message, int severity, String details) {
        add(errors, assertion, message, severity, details);
    }

    public int warningCount() {
        return warnings.size();
    }

    public void addWarning(String assertion, String message, int severity) {
        addWarning(assertion, message, severity, "");
    }

    public void addWarning(String assertion, String message, int severity, String details) {
        add(warnings, assertion, message, severity, details);
    }

    public int infoCount() {
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

    public String toHtmlTableFragment(List<AnalysisInformation> collection, String caption, boolean includeEmpty) {
        StringBuilder html = new StringBuilder();
        if (collection.size() > 0 || includeEmpty ) {
            html.append("<tr>");
            html.append("<td colspan='4'>").append(escapeHtml(caption)).append("</td>");
            html.append("</tr>");
            for (AnalysisInformation inf : collection) {
                html.append(inf.toHtmlTableRow());
            }
        }
        return html.toString();
    }

    public String toHtmlTable(boolean includeEmpty) {
        StringBuilder html = new StringBuilder();
        html.append("<table>");
        html.append(toHtmlTableFragment(errors, "Errors:", includeEmpty));
        html.append(toHtmlTableFragment(warnings, "Warnings:", includeEmpty));
        html.append(toHtmlTableFragment(info, "Information:", includeEmpty));
        html.append("</able>");
        return html.toString();
    }

}
