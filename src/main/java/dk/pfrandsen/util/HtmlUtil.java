package dk.pfrandsen.util;

import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Misc. utilities
 *   - convert AnalysisInformationCollector/AnalysisInformation to html
 *   - transform schema/wsdl to html
 */
public class HtmlUtil {

    public static String toHtmlTableRow(AnalysisInformation info) {
        StringBuilder html = new StringBuilder();
        html.append("<tr>");
        html.append("<td>").append(StringEscapeUtils.escapeHtml4(info.getAssertion())).append("</td>");
        html.append("<td>").append(StringEscapeUtils.escapeHtml4(info.getMessage())).append("</td>");
        html.append("<td>").append(info.getSeverity()).append("</td>");
        html.append("<td>").append(StringEscapeUtils.escapeHtml4(info.getDetails())).append("</td>");
        html.append("</tr>");
        return html.toString();
    }

    private static String toHtmlTableFragment(List<AnalysisInformation> collection, String caption, boolean includeEmpty,
                                              String cssClasses) {
        StringBuilder html = new StringBuilder();
        if (collection.size() > 0 || includeEmpty) {
            html.append("<tr>");
            html.append("<td colspan='4' class='").append(cssClasses).append("'>")
                    .append(StringEscapeUtils.escapeHtml4(caption)).append("</td>");
            html.append("</tr>");
            for (AnalysisInformation inf : collection) {
                html.append(toHtmlTableRow(inf));
            }
        }
        return html.toString();
    }

    public static String toHtmlTable(AnalysisInformationCollector collector, boolean includeEmpty) {
        StringBuilder html = new StringBuilder();
        html.append("<table class='analysis-table' summary='Analysis result'>");
        html.append(toHtmlTableFragment(collector.getErrors(), "Errors:", includeEmpty, "tblheader error"));
        html.append(toHtmlTableFragment(collector.getWarnings(), "Warnings:", includeEmpty, "tblheader warning"));
        html.append(toHtmlTableFragment(collector.getInfo(), "Information:", includeEmpty, "tblheader info"));
        html.append("</table>");
        return html.toString();
    }

    // public static String toHtml(AnalysisInformationCollector collector, boolean includeEmpty, boolean tidyUp,
    public static String toHtml(String htmlContent, boolean includeEmpty, boolean tidyUp,
                                String baseName, String ext) {
        String fileName = StringEscapeUtils.escapeHtml4(baseName + "." + ext);
        String head = "<head><title>" + fileName + "</title>" +
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
        html.append("<div>" + fileName + "</div>");
        html.append(htmlContent);
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

    public static String schemaToHtml(String xml, boolean bodyOnly) throws Exception {
        Path stylesheet = Paths.get("/", "xslt", "tohtml", "annotated-xsd.xsl");
        return xmlToHtml(xml, bodyOnly, stylesheet);
    }

    public static String xmlToHtml(String xml, boolean bodyOnly, Path stylesheet) throws Exception {
        String xsl = IOUtils.toString(Utilities.class.getResourceAsStream(stylesheet.toString()));
        TransformerFactory factory = TransformerFactory.newInstance();
        URIResolver resolver = new XslURIResolver();
        factory.setURIResolver(resolver);
        StreamSource xslt = new StreamSource(IOUtils.toInputStream(xsl));
        Transformer transformer = factory.newTransformer(xslt);
        transformer.setURIResolver(resolver);
        Source xmlSource = new StreamSource(IOUtils.toInputStream(xml));
        StringWriter writer = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(writer));
        return bodyOnly ? htmlBody(writer.toString()) : writer.toString();
    }

    public static String wsdlToHtml(String xml, boolean bodyOnly) throws Exception {
        Path stylesheet = Paths.get("/", "xslt", "tohtml", "annotated-wsdl.xsl");
        return xmlToHtml(xml, bodyOnly, stylesheet);
    }

    public static String htmlBody(String html) {
        return StringUtils.substringBetween(html, "<body>", "</body>");
    }

}