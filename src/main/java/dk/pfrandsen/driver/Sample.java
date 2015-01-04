package dk.pfrandsen.driver;


import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.wsdl.*;
import dk.pfrandsen.xsd.SchemaChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/*

simpletype http://simpletype.schemas.nykreditnet.net/
technical http://technical.schemas.nykreditnet.net/fault/v1
concept http://concept.schemas.nykreditnet.net/bank/account/currency/v1
service http://service.schemas.nykreditnet.net/
process http://process.schemas.nykreditnet.net/

Skip external

 */

public class Sample {

    static int errorCount, warningCount, infoCount;
    static int xsd, wsdl, other;
    static List<String> includeDirs = Arrays.asList("concept", "service", "technical", "simpletype", "process");
    static AnalysisInformationCollector errors = new AnalysisInformationCollector();

    private static void createDirs(Path path) {
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
    }

    private static String toHtml(String htmlFragment, String baseName, String ext) {
        String head = "<head><title>" + escapeHtml(baseName + "." + ext) + "</title>" +
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
        html.append(htmlFragment);
        html.append("<div><a href='").append(baseName + "." + ext).append("'>")
                .append("wsdl".equalsIgnoreCase(ext) ? "WSDL" : "Schema").append("</a></div>");
        html.append("<div><a href='").append(baseName + ".json").append("'>")
                .append("JSON report").append("</a></div>");
        html.append("</body>");
        html.append("</html>");
        Tidy tidy = new Tidy();
        tidy.setIndentContent(true);
        tidy.setQuiet(true);
        try (ByteArrayInputStream in = new ByteArrayInputStream(html.toString().getBytes())) {
            Document doc = tidy.parseDOM(in, null);
            try (OutputStream out = new ByteArrayOutputStream()) {
                tidy.pprint(doc, out);
                return out.toString();
            }

        } catch (IOException e) {
            return html.toString();
        }
        /*Document doc = tidy.parseDOM(, null);
        OutputStream out = new ByteArrayOutputStream();
        tidy.pprint(doc, out);
        return html.toString();*/
    }

    private static void writeReport(Path location, File file, AnalysisInformationCollector collector)
            throws IOException {
        errorCount += collector.errorCount();
        warningCount += collector.warningCount();
        infoCount += collector.infoCount();
        System.out.println("e " + collector.errorCount() + " w " + collector.warningCount() + " i "
                + collector.infoCount());
        String baseName = FilenameUtils.getBaseName(file.getName());
        String ext = FilenameUtils.getExtension(file.getName());
        Path jsonOut = location.resolve(baseName + ".json");
        Path htmlOut = location.resolve(baseName + ".html");
        System.out.println(jsonOut);
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile());
        FileUtils.writeStringToFile(htmlOut.toFile(), toHtml(collector.toHtmlTable(false), baseName, ext));
    }

    private static void checkFiles(final Path rootDirectory, final Path srcRoot, final Path targetRoot,
                                   final boolean copySrc) throws IOException {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                String ext = FilenameUtils.getExtension(f.getName());
                if ("xsd".equalsIgnoreCase(ext) || "wsdl".equalsIgnoreCase(ext)) {
                    Path filePath = Paths.get(f.getAbsolutePath());
                    Path target = targetRoot.resolve(srcRoot.relativize(filePath));
                    System.out.println(target);
                    createDirs(target.getParent());
                    String domain = dirToNamespace(rootDirectory.getFileName());
                    Path relPath = rootDirectory.relativize(filePath).getParent();
                    if (copySrc) {
                        Files.copy(file, target);
                    }
                    AnalysisInformationCollector collector = new AnalysisInformationCollector();
                    try (FileInputStream is = new FileInputStream(file.toFile())) {
                        String fileContents = IOUtils.toString(is);
                        Utf8.checkUtf8File(filePath, collector);
                        if (Utilities.hasUtf8Bom(fileContents)) {
                            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
                            fileContents = Utilities.removeUtf8Bom(fileContents);
                        }
                        if ("xsd".equalsIgnoreCase(ext)) {
                            checkSchema(fileContents, collector, f.getName(), relPath, domain);
                            xsd++;
                        } else if ("wsdl".equalsIgnoreCase(ext)) {
                            checkWsdl(fileContents, collector, f.getName(), relPath, domain);
                            wsdl++;
                        }
                    }
                    writeReport(target.getParent(), f, collector);
                } else {
                    // file with invalid extension
                    other++;
                    errors.addError("", "File with invalid extension found. Valid extensions are {xsd,wsdl}",
                            AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL,
                            "File: '" + file + "'.");
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private static String dirToNamespace(Path path) {
        if (path.toFile().isDirectory()) {
            String name = path.toFile().getName();
            if (includeDirs.contains(name)) {
                return name + ".schemas.nykreditnet.net";
            }
        }
        return "";
    }

    private static void printStats(long start) {
        long duration = System.currentTimeMillis() - start;
        long millis = duration % 1000;
        long seconds = duration / 1000 % 60;
        long minutes = duration / (60 * 1000) % 60;
        System.out.println((xsd+wsdl) + " files processed in " + minutes + "m " + seconds + "s " + millis + "ms");
        System.out.println(errorCount + " errors, " + warningCount + " warnings, and " + infoCount + " info");
    }

    public static void runChecks(Path srcRoot, Path targetRoot) {
        long start = System.currentTimeMillis();
        // iterate top level directories
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcRoot)) {
            for (Path entry: stream) {
                if (entry.toFile().isDirectory()) {
                    if (includeDirs.contains(entry.toFile().getName())) {
                        System.out.println("Including " + entry);
                        checkFiles(entry, srcRoot, targetRoot, true);
                    } else {
                        System.out.println("Skipping " + entry);
                        errors.addInfo("", "Skipping directory in root folder",
                                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
                    }
                } else {
                    errors.addError("", "Ignoring file in ",
                            AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
                }
            }
        } catch (IOException e) {
            errors.addError("",
                    "Exception while accessing root directory '" + srcRoot + "'",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
        }
        printStats(start);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        List<String> unknown = new ArrayList<>();
        // check args (source root, target root)
        // target/check.zip must not exits
        // target/root must not exits
        // target/check.html must not exist
        // well, target folder must be empty
        //Path relativePath = Paths.get("tmp", "schemaroot");
        Path outPath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("out");
        //Path  root = Paths.get(System.getProperty("user.home")).resolve(relativePath);
        Path  root = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("schemaroot");
        runChecks(root, outPath);
    }

    private static void checkSchema(String schema, AnalysisInformationCollector collector, String fileName,
                                    Path relPath, String domain) {
        SchemaChecker.checkFormDefault(schema, collector);
        SchemaChecker.checkNillable(schema, collector);
        SchemaChecker.checkMinMaxOccurs(schema, collector);
        SchemaChecker.checkConceptTypes(schema, collector);
        SchemaChecker.checkTypes(schema, collector);
        SchemaChecker.checkElements(schema, collector);
        SchemaChecker.checkBetaNamespace(schema, collector);
        SchemaChecker.checkEnumerationValues(schema, collector);
        SchemaChecker.checkSimpleTypesInConcept(schema, collector);
        SchemaChecker.checkServiceElementDefinition(schema, collector);
        SchemaChecker.checkRedefinition(schema, collector);
        SchemaChecker.checkSchemaUse(schema, collector);
        SchemaChecker.checkTargetNamespaceVersion(schema, collector);
        SchemaChecker.checkTargetNamespaceCase(schema, collector);
        SchemaChecker.checkTargetNamespaceCharacters(schema, collector);
        SchemaChecker.checkAnyType(schema, collector);
        SchemaChecker.checkAny(schema, collector);
        SchemaChecker.checkAnyAttribute(schema, collector);
        SchemaChecker.checkIdenticalElementNames(schema, collector);
        SchemaChecker.checkImportAndIncludeLocation(schema, collector);
        SchemaChecker.checkDeprecated(schema, collector);
        SchemaChecker.checkUnusedNamespacePrefix(schema, collector);
        SchemaChecker.checkUnusedImport(schema, collector);
        DocumentationChecker.checkConceptSchemaDocumentation(schema, collector);
        // file and path checks
        SchemaChecker.checkSchemaFilename(fileName, collector);
        SchemaChecker.checkEnterpriseConceptNamespace(schema, fileName, collector);
        SchemaChecker.checkServiceConceptNamespace(schema, fileName, collector);
        SchemaChecker.checkPathAndTargetNamespace(schema, domain, relPath, collector);
    }

    private static void checkWsdl(String wsdl, AnalysisInformationCollector collector, String fileName,
                                    Path relPath, String domain) {
        // TODO: add missing checks (WS-I)
        BetaNamespaceChecker.checkBetaNamespace(wsdl, collector);
        BetaNamespaceChecker.checkBetaNamespaceDefinitions(wsdl, collector);
        BetaNamespaceChecker.checkBetaNamespaceImports(wsdl, collector);
        BindingChecker.checkFaults(wsdl, collector);
        BindingChecker.checkSoapAction(wsdl, collector);
        DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        MessageChecker.checkMessageNames(wsdl, collector);
        MessageChecker.checkMessageParts(wsdl, collector);
        MessageChecker.checkUnusedMessages(wsdl, collector);
        NamespaceChecker.checkInvalidImports(wsdl, collector);
        NamespaceChecker.checkNamespace(wsdl, collector);
        SchemaChecker.checkUnusedImport(wsdl, collector);
        OperationChecker.checkOperationNames(wsdl, collector);
        OperationChecker.checkPortTypeAndBinding(wsdl, collector);
        PortBindingNameChecker.checkNames(wsdl, collector);
        PortTypeChecker.checkCardinality(wsdl, collector);
        PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        PortTypeChecker.checkName(wsdl, collector);
        SchemaTypesChecker.checkSchemaTypes(wsdl, collector);
        ServiceChecker.checkServices(wsdl, collector);
        SoapBindingChecker.checkBindings(wsdl, collector);
        WsdlNameChecker.checkName(wsdl, collector);

        // WS-I checks

        // file and path checks
        ServiceChecker.checkServiceFileName(relPath.resolve(fileName), wsdl, collector);
        WsdlChecker.checkPathCharacters(Utilities.pathToNamespace(domain, relPath), collector);
        WsdlChecker.checkPathAndTargetNamespace(wsdl, domain, relPath, collector);
        WsdlChecker.checkServiceNamespace(wsdl, fileName, collector);
    }

}
