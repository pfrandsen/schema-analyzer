package dk.pfrandsen.util;

import dk.pfrandsen.check.AnalysisInfo;
import dk.pfrandsen.check.AnalysisInfoCollector;
import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utilities {

    public static String join(String separator, List<String> values) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (String value : values) {
            counter++;
            builder.append(value);
            if (counter != values.size()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static String pathToNamespace(String domain, Path relPath) {
        int parts = relPath.getNameCount();
        StringBuilder builder = new StringBuilder();
        builder.append("http://").append(domain).append("/");
        for (int idx = 0; idx < parts - 1; idx++) {
            builder.append(relPath.getName(idx)).append("/");
        }
        builder.append(relPath.getName(parts - 1));
        return builder.toString();
    }

    public static URI appendPath(URI uri, Path relPath) {
        int parts = relPath.getNameCount();
        StringBuilder builder = new StringBuilder(uri.getPath());
        for (int idx = 0; idx < parts; idx++) {
            builder.append("/").append(relPath.getName(idx));
        }
        return uri.resolve(builder.toString());
    }


    public static List<String> splitOnUppercase(String text) {
        List<String> words = new ArrayList<>();
        if (text.length() <= 1) {
            words.add(text);
        } else {
            int startOfWord = 0;
            for (int idx = 1; idx < text.length(); idx++) {
                if (Character.isUpperCase(text.charAt(idx))) {
                    words.add(text.substring(startOfWord, idx));
                    startOfWord = idx;
                }
            }
            words.add(text.substring(startOfWord));
        }
        return words;
    }

    // word is only allowed to contain a sequence of characters a-z followed by an optional sequence of digits
    // all letters must be lowercase except the first which must be uppercase if startWithUppercase is true
    private static boolean checkWord(String word, boolean startWithUppercase) {
        if (word.length() == 0) {
            return false;
        }
        if (startWithUppercase) {
            return word.matches("[A-Z][a-z]*[0-9]*");
        }
        return word.matches("[a-z]+[0-9]*");
    }

    public static boolean isCamelCaseAscii(String text, boolean upper) {
        List<String> wordList = splitOnUppercase(text);
        if (!wordList.isEmpty()) {
            for (int idx = 0; idx < wordList.size(); idx++) {
                String word = wordList.get(idx);
                boolean startWithUppercase = upper || (idx != 0);
                if (!checkWord(word, startWithUppercase)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String toLowerCamelCase(String label) {
        return StringUtils.uncapitalize(label);
    }

    public static List<String> toLowerCamelCase(List<String> labels) {
        List<String> unCapitalized = new ArrayList<>();
        for (String label : labels) {
            unCapitalized.add(toLowerCamelCase(label));
        }
        return unCapitalized;
    }

    public static String toUpperCamelCase(String label) {
        return StringUtils.capitalize(label);
    }

    public static List<String> toUpperCamelCase(List<String> labels) {
        List<String>unCapitalized = new ArrayList<>();
        for (String label : labels) {
            unCapitalized.add(toUpperCamelCase(label));
        }
        return unCapitalized;
    }


    public static boolean isUpperCamelCaseAscii(String text) {
        return isCamelCaseAscii(text, true);
    }
    public static boolean isLowerCamelCaseAscii(String text) {
        return isCamelCaseAscii(text, false);
    }

    public static String removeVersion(String name) {
        if (name.matches(".+V[0-9]+")) {
            int index = name.lastIndexOf('V');
            return name.substring(0, index);
        }
        return name;
    }

    public static void mkDirs(Path folder) {
        if (!folder.toFile().exists()) {
            if (!folder.toFile().mkdirs()) {
                System.out.println("Could not create: " + folder.toString());
            }
        }
    }

    /* get the key for a given value */
    public static String getKeyForValue(String value, Map<String, String> namespaces) {
        if (namespaces.containsValue(value)) {
            for (String key : namespaces.keySet()) {
                if (value.equals(namespaces.get(key))) {
                    return key;
                }
            }
        }
        return "";
    }

    public static boolean hasUtf8Bom(String text) {
        String bom = "\uFEFF";
        return text.startsWith(bom);
    }

    public static String removeUtf8Bom(String text) {
        if (hasUtf8Bom(text)) {
            return text.substring(1);
        }
        return text;
    }

    public static String removeXmlComments(String xml) throws Exception {
        String stylesheet = "/xslt/removeComments.xsl";
        String xsl = IOUtils.toString(Utilities.class.getResourceAsStream(stylesheet), StandardCharsets.UTF_8);
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(IOUtils.toInputStream(xsl));
        Transformer transformer = factory.newTransformer(xslt);
        Source xmlSource = new StreamSource(IOUtils.toInputStream(xml));
        StringWriter writer = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(writer));
        return writer.toString();
    }

    public static String removeXmlDocumentation(String xml) throws Exception {
        String stylesheet = "/xslt/removeDocumentation.xsl";
        String xsl = IOUtils.toString(Utilities.class.getResourceAsStream(stylesheet), StandardCharsets.UTF_8);
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(IOUtils.toInputStream(xsl));
        Transformer transformer = factory.newTransformer(xslt);
        Source xmlSource = new StreamSource(IOUtils.toInputStream(xml));
        StringWriter writer = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(writer));
        return writer.toString();
    }

    public static String prettyPrint(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }

    private static String readFileContents(File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return IOUtils.toString(is);
        }
    }

    public static void createDirs(Path path) {
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
    }

    public static String getContentWithoutUtf8Bom(Path file) throws IOException {
        try (FileInputStream is = new FileInputStream(file.toFile())) {
            String fileContents = IOUtils.toString(is);
            if (Utilities.hasUtf8Bom(fileContents)) {
                // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
                fileContents = Utilities.removeUtf8Bom(fileContents);
            }
            return fileContents;
        }
    }

    public static String getContentWithoutUtf8Bom(URI uri, Charset encoding) throws IOException {
        String fileContents = IOUtils.toString(uri, encoding);
        if (Utilities.hasUtf8Bom(fileContents)) {
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            fileContents = Utilities.removeUtf8Bom(fileContents);
        }
        return fileContents;
    }

    public static String getContentWithoutUtf8Bom(InputStream is, Charset encoding) throws IOException {
        String fileContents = IOUtils.toString(is, encoding);
        if (Utilities.hasUtf8Bom(fileContents)) {
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            fileContents = Utilities.removeUtf8Bom(fileContents);
        }
        return fileContents;
    }

    public static void copyCollector(AnalysisInfoCollector from, AnalysisInformationCollector to) {
        for (AnalysisInfo a : from.getErrors()) {
            to.addError(a.getAssertion(), a.getMessage(), a.getSeverity(), a.getDetails());
        }
        for (AnalysisInfo a : from.getWarnings()) {
            to.addWarning(a.getAssertion(), a.getMessage(), a.getSeverity(), a.getDetails());
        }
        for (AnalysisInfo a : from.getInfo()) {
            to.addInfo(a.getAssertion(), a.getMessage(), a.getSeverity(), a.getDetails());
        }
    }
}
