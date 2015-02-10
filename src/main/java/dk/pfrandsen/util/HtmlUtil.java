package dk.pfrandsen.util;

import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Utility to convert AnalysisInformationCollector/AnalysisInformation to html
 */
public class HtmlUtil {

    public static String toHtmlTableRow(AnalysisInformation info) {
        StringBuilder html = new StringBuilder();
        html.append("<tr>");
        html.append("<td>").append(escapeHtml(info.getAssertion())).append("</td>");
        html.append("<td>").append(escapeHtml(info.getMessage())).append("</td>");
        html.append("<td>").append(info.getSeverity()).append("</td>");
        html.append("<td>").append(escapeHtml(info.getDetails())).append("</td>");
        html.append("</tr>");
        return html.toString();
    }

    private static String toHtmlTableFragment(List<AnalysisInformation> collection, String caption, boolean includeEmpty,
                                       String cssClasses) {
        StringBuilder html = new StringBuilder();
        if (collection.size() > 0 || includeEmpty) {
            html.append("<tr>");
            html.append("<td colspan='4' class='").append(cssClasses).append("'>").append(escapeHtml(caption))
                    .append("</td>");
            html.append("</tr>");
            for (AnalysisInformation inf : collection) {
                html.append(toHtmlTableRow(inf));
            }
        }
        return html.toString();
    }

    public static  String toHtmlTable(AnalysisInformationCollector collector, boolean includeEmpty) {
        StringBuilder html = new StringBuilder();
        html.append("<table summary='Analysis result'>");
        html.append(toHtmlTableFragment(collector.getErrors(), "Errors:", includeEmpty, "tblheader error"));
        html.append(toHtmlTableFragment(collector.getWarnings(), "Warnings:", includeEmpty, "tblheader warning"));
        html.append(toHtmlTableFragment(collector.getInfo(), "Information:", includeEmpty, "tblheader info"));
        html.append("</table>");
        return html.toString();
    }

    public static String toHtml(AnalysisInformationCollector collector, boolean includeEmpty, boolean tidyUp, String filename,
                         String ext) {
        String head = "<head><title>" + escapeHtml(filename) + "</title>" +
                "<style type=\"text/css\">" +
                "table {border-collapse: collapse;}\n" +
                "table, th, td {border: 1px solid black;}\n" +
                "td {padding: 3px;}\n" +
                ".tblheader {font-weight: bold;}\n" +
                ".error {background-color: red; color: white;}\n" +
                ".warning {background-color: LightCoral; color: white;}\n" +
                ".info {background-color: LemonChiffon;}\n" +
                "</style>" +
                "</head>";
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        html.append("<html>");
        html.append(head);
        html.append("<body>");
        html.append(toHtmlTable(collector, includeEmpty));
        /*html.append("<div><a href='").append(filename).append("'>")
                .append("wsdl".equalsIgnoreCase(ext) ? "WSDL" : "Schema").append("</a></div>");
        html.append("<div><a href='").append(filename + ".json").append("'>")
                .append("JSON report").append("</a></div>");*/
        html.append("</body>");
        html.append("</html>");
        return tidyUp ? doTidy(html.toString()) : html.toString();
    }

    private static String doTidy(String html) {
        Tidy tidy = new Tidy();
        tidy.setIndentContent(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        try (ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes())) {
            Document doc = tidy.parseDOM(in, null);
            try (OutputStream out = new ByteArrayOutputStream()) {
                tidy.pprint(doc, out);
                return out.toString();
            }
        } catch (IOException e) {
            return html;
        }
    }

}