package dk.pfrandsen.check;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class AnalysisInformation {
    private String assertion;
    private String message;
    private int severity;
    private String details;

    public AnalysisInformation() {
        this.assertion = "";
        this.message = "";
        this.severity = AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN;
        this.details = "";
    }

    public AnalysisInformation(String assertion, String message, int severity) {
        this(assertion, message, severity, "");
    }

    public AnalysisInformation(String assertion, String message, int severity, String details) {
        this.assertion = assertion;
        this.message = message;
        this.severity = severity;
        this.details = details;
    }

    public AnalysisInformation(AnalysisInformation info) {
        this.assertion = info.assertion;
        this.message = info.message;
        this.severity = info.severity;
        this.details = info.details;
    }

    public String toJson(boolean writeNullProperties) throws IOException {
        if (writeNullProperties) {
            return JSON.std.with(JSON.Feature.WRITE_NULL_PROPERTIES).asString(this);
        }
        return JSON.std.asString(this);
    }

    public static AnalysisInformation fromJson(String json) throws IOException {
        return JSON.std.beanFrom(AnalysisInformation.class, json);
    }

    public static AnalysisInformation fromJson(InputStream inputStream) throws IOException {
        return JSON.std.beanFrom(AnalysisInformation.class, inputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnalysisInformation that = (AnalysisInformation) o;

        if (severity != that.severity) return false;
        if (assertion != null ? !assertion.equals(that.assertion) : that.assertion != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = assertion != null ? assertion.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + severity;
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean hasDetails() {
        return details.length() > 0;
    }

    @Override
    public String toString() {
        return "AnalysisInformation{" +
                "assertion='" + assertion + '\'' +
                ", message='" + message + '\'' +
                ", severity=" + severity +
                ", details='" + details + '\'' +
                '}';
    }

    public String toHtmlTableRow() {
        StringBuilder html = new StringBuilder();
        html.append("<tr>");
        html.append("<td>").append(escapeHtml(assertion)).append("</td>");
        html.append("<td>").append(escapeHtml(message)).append("</td>");
        html.append("<td>").append(severity).append("</td>");
        html.append("<td>").append(escapeHtml(details)).append("</td>");
        html.append("</tr>");
        return html.toString();
    }
}
