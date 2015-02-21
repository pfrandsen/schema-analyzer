package dk.pfrandsen.check;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

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
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
        info = new ArrayList<>();
    }

    public AnalysisInformationCollector(AnalysisInformationCollector collector) {
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
        info = new ArrayList<>();
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

    public String toJson(boolean prettyPrint) throws IOException {
        if (prettyPrint) {
            return JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).asString(this);
        }
        return JSON.std.asString(this);
    }

    public static AnalysisInformationCollector fromJson(String json) throws IOException {
        return JSON.std.beanFrom(AnalysisInformationCollector.class, json);
    }

    public static AnalysisInformationCollector fromJson(InputStream inputStream) throws IOException {
        return JSON.std.beanFrom(AnalysisInformationCollector.class, inputStream);
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

    /**
     * Compute the relative complement ot two collections (like SQL EXCEPT operator)
     *
     * @param collector
     * @return the elements of this collection that does not exist in collector
     */
    public AnalysisInformationCollector except(AnalysisInformationCollector collector) {
        AnalysisInformationCollector notIn = new AnalysisInformationCollector();
        for (AnalysisInformation e : errors) {
            if (!collector.getErrors().contains(e)) {
                notIn.addError(e.getAssertion(), e.getMessage(), e.getSeverity(), e.getDetails());
            }
        }
        for (AnalysisInformation w : warnings) {
            if (!collector.getWarnings().contains(w)) {
                notIn.addWarning(w.getAssertion(), w.getMessage(), w.getSeverity(), w.getDetails());
            }
        }
        for (AnalysisInformation i : info) {
            if (!collector.getInfo().contains(i)) {
                notIn.addInfo(i.getAssertion(), i.getMessage(), i.getSeverity(), i.getDetails());
            }
        }
        return notIn;
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

    public boolean isEmpty() {
        return errorCount() == 0 && warningCount() == 0 && infoCount() == 0;
    }

    private void add(List<AnalysisInformation> collection, String assertion, String message, int severity, String details) {
        collection.add(new AnalysisInformation(assertion, message, severity, details));
    }

    private String toHtmlTableFragment(List<AnalysisInformation> collection, String caption, boolean includeEmpty,
                                       String cssClasses) {
        StringBuilder html = new StringBuilder();
        if (collection.size() > 0 || includeEmpty ) {
            html.append("<tr>");
            html.append("<td colspan='4' class='").append(cssClasses).append("'>")
                    .append(StringEscapeUtils.escapeHtml4(caption)).append("</td>");
            html.append("</tr>");
            for (AnalysisInformation inf : collection) {
                html.append(inf.toHtmlTableRow());
            }
        }
        return html.toString();
    }

    public String toHtmlTable(boolean includeEmpty) {
        StringBuilder html = new StringBuilder();
        html.append("<table summary='Analysis result'>");
        html.append(toHtmlTableFragment(errors, "Errors:", includeEmpty, "tblheader error"));
        html.append(toHtmlTableFragment(warnings, "Warnings:", includeEmpty, "tblheader warning"));
        html.append(toHtmlTableFragment(info, "Information:", includeEmpty, "tblheader info"));
        html.append("</table>");
        return html.toString();
    }

}
