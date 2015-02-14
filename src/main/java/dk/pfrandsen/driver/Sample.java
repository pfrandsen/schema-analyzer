package dk.pfrandsen.driver;


import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.UnpackTool;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsiUtil;
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

    static private Path toolJar, toolRoot;
    static int errorCount, warningCount, infoCount;
    static int xsd, wsdl, other;
    static List<String> includeDirs = Arrays.asList("service");
    // collector to store errors with running this tool itself
    static AnalysisInformationCollector errors = new AnalysisInformationCollector();

    private static void createDirs(Path path) {
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
    }

    private static String doTidy(String html) {
        Tidy tidy = new Tidy();
        tidy.setIndentContent(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        try (ByteArrayInputStream in = new ByteArrayInputStream(html.toString().getBytes())) {
            Document doc = tidy.parseDOM(in, null);
            try (OutputStream out = new ByteArrayOutputStream()) {
                tidy.pprint(doc, out);
                return out.toString();
            }
        } catch (IOException e) {
            return html.toString();
        }
    }

    private static String toHtml(String htmlFragment, String filename, String ext) {
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
        html.append(htmlFragment);
        html.append("<div><a href='").append(filename).append("'>")
                .append("wsdl".equalsIgnoreCase(ext) ? "WSDL" : "Schema").append("</a></div>");
        html.append("<div><a href='").append(filename + ".json").append("'>")
                .append("JSON report").append("</a></div>");
        html.append("</body>");
        html.append("</html>");

        //System.out.println("\n\nStarting Tidy...");
        String retVal = doTidy(html.toString());
        //System.out.println("Tidy done...\n\n");
        return retVal;
        /*Tidy tidy = new Tidy();
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
        }*/
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
        //String baseName = FilenameUtils.getBaseName(file.getName());
        String ext = FilenameUtils.getExtension(file.getName());
        //Path jsonOut = location.resolve(baseName + ".json");
        //Path htmlOut = location.resolve(baseName + ".html");
        // use the full filename including extension to make sure that no name clash can occur
        String filename = file.getName();
        Path jsonOut = location.resolve(filename + ".json");
        Path htmlOut = location.resolve(filename + ".html");
        //System.out.println(jsonOut);
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile());
        FileUtils.writeStringToFile(htmlOut.toFile(), toHtml(collector.toHtmlTable(false), filename, ext));
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
                    String domain = dirToNamespace(rootDirectory);
                    Path relPath = rootDirectory.relativize(filePath).getParent();
                    if (copySrc) {
                        Files.copy(file, target);
                    }
                    AnalysisInformationCollector collector = new AnalysisInformationCollector();
                    try (FileInputStream is = new FileInputStream(file.toFile())) {
                        String fileContents = IOUtils.toString(is);
                        Utf8.checkUtf8File(rootDirectory, filePath, collector);
                        if (Utilities.hasUtf8Bom(fileContents)) {
                            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
                            fileContents = Utilities.removeUtf8Bom(fileContents);
                        }
                        if ("xsd".equalsIgnoreCase(ext)) {
                            checkSchema(fileContents, collector, f.getName(), relPath, domain);
                            xsd++;
                        } else if ("wsdl".equalsIgnoreCase(ext)) {
                            checkWsdl(fileContents, collector, f.getName(), relPath, domain, target.getParent());
                            wsdl++;
                        }
                    }
                    writeReport(target.getParent(), f, collector);
                } else {
                    // file with invalid extension
                    other++;
                    errors.addError("", "File with invalid extension found. Valid extensions are {xsd,wsdl}",
                            AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL,
                            "File: '" + rootDirectory.relativize(file) + "'.");
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
                //return name;
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
                        System.out.println("Skipping directory " + entry);
                        errors.addInfo("", "Skipping directory '" + entry +"' in root folder",
                                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
                    }
                } else {
                    System.out.println("Skipping file " + entry);
                    errors.addError("", "Ignoring file '" + entry +"' in root folder",
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



    private static void checkFiles(final Path outputRoot, final Path subDir) throws IOException {
        Files.walkFileTree(subDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                String ext = FilenameUtils.getExtension(f.getName());
                if ("xsd".equalsIgnoreCase(ext) || "wsdl".equalsIgnoreCase(ext)) {
                    Path filePath = Paths.get(f.getAbsolutePath());
                    System.out.println(wsdl + " " + filePath.toString());
                    //Path target = targetRoot.resolve(srcRoot.relativize(filePath));
                    //System.out.println(target);
                    String domain = dirToNamespace(subDir);
                    Path relPath = subDir.relativize(filePath).getParent();
                    AnalysisInformationCollector collector = new AnalysisInformationCollector();
                    try (FileInputStream is = new FileInputStream(file.toFile())) {
                        String fileContents = IOUtils.toString(is);
                        Utf8.checkUtf8File(outputRoot, filePath, collector);
                        if (Utilities.hasUtf8Bom(fileContents)) {
                            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
                            fileContents = Utilities.removeUtf8Bom(fileContents);
                        }
                        if ("xsd".equalsIgnoreCase(ext)) {
                            //checkSchema(fileContents, collector, f.getName(), relPath, domain);
                            xsd++;
                        } else if ("wsdl".equalsIgnoreCase(ext)) {
                            checkWsdl(fileContents, collector, f.getName(), relPath, domain, file.getParent());
                            wsdl++;
                        }
                    }
                    writeReport(file.getParent(), f, collector);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }



    private static void copyFilesWithValidExtensions(final Path rootDirectory, final Path srcRoot,
                                                      final Path targetRoot) throws IOException {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                String ext = FilenameUtils.getExtension(f.getName());
                if ("xsd".equalsIgnoreCase(ext) || "wsdl".equalsIgnoreCase(ext)) {
                    Path filePath = Paths.get(f.getAbsolutePath());
                    Path target = targetRoot.resolve(srcRoot.relativize(filePath));
                    createDirs(target.getParent());
                    Files.copy(file, target);
                } else {
                    // file with invalid extension
                    System.out.println("Skipping " + rootDirectory.relativize(file));
                    other++;
                    errors.addError("", "File with invalid extension found. Valid extensions are {xsd,wsdl}",
                            AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL,
                            "File: '" + rootDirectory.relativize(file) + "'.");
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // filter root folders to copy
    public static boolean copyFiles(Path srcRoot, Path targetRoot) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcRoot)) {
            for (Path entry: stream) {
                if (entry.toFile().isDirectory()) {
                    if (includeDirs.contains(entry.toFile().getName())) {
                        System.out.println("Including " + entry);
                        copyFilesWithValidExtensions(entry, srcRoot, targetRoot);
                    } else {
                        System.out.println("Skipping directory " + entry);
                        errors.addInfo("", "Skipping directory '" + entry +"' in root folder",
                                AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
                    }
                } else {
                    System.out.println("Skipping file " + entry);
                    errors.addError("", "Ignoring file '" + entry +"' in root folder",
                            AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
                }
            }
        } catch (IOException e) {
            errors.addError("",
                    "Exception while accessing root directory '" + srcRoot + "'",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
            return false;
        }
        return true;
    }

    public static void runChecks2(Path srcRoot, Path targetRoot) {
        long start = System.currentTimeMillis();
        if (copyFiles(srcRoot, targetRoot)) {
            for (String dir : includeDirs) {
                Path subDir = targetRoot.resolve(dir);
                if (subDir.toFile().exists() && subDir.toFile().isDirectory()) {
                    try {
                        checkFiles(targetRoot, subDir);
                    } catch (IOException e) {
                        errors.addError("",
                                "Exception while accessing directory '" + subDir + "'",
                                AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());

                    }
                }
            }
        } else {
            // report error
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
        // extract wsi-tool
        //toolJar = Paths.get("lib", "wsi-checker-1.0-SNAPSHOT.jar");
        toolRoot = outPath.resolve("wsi-tool");
        UnpackTool unpackTool = new UnpackTool();
        boolean status =  unpackTool.extractTool(toolRoot);

        //boolean status = WsiUtil.unpackCheckerTool(toolJar, toolRoot);
        if (status != true) {
            // terminate - fatal error
        }

        //Path  root = Paths.get(System.getProperty("user.home")).resolve(relativePath);
        Path  root = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("0902");
        runChecks2(root, outPath);
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
                                    Path relPath, String domain, Path location) {
        String baseName = FilenameUtils.getBaseName(fileName);
        // TODO: add missing checks (WS-I)
        System.out.println("wsdl BetaNamespaceChecker.checkBetaNamespace");
        BetaNamespaceChecker.checkBetaNamespace(wsdl, collector);
        System.out.println("wsdl BetaNamespaceChecker.checkBetaNamespaceDefinitions");
        BetaNamespaceChecker.checkBetaNamespaceDefinitions(wsdl, collector);
        System.out.println("wsdl BetaNamespaceChecker.checkBetaNamespaceImports");
        BetaNamespaceChecker.checkBetaNamespaceImports(wsdl, collector);
        System.out.println("wsdl BindingChecker.checkFaults");
        BindingChecker.checkFaults(wsdl, collector);
        //System.out.println("wsdl BindingChecker.checkSoapAction");
        //BindingChecker.checkSoapAction(wsdl, collector);
        //System.out.println("wsdl DocumentationChecker.checkWsdlDocumentation");
        //DocumentationChecker.checkWsdlDocumentation(wsdl, collector);
        System.out.println("wsdl MessageChecker.checkMessageNames");
        MessageChecker.checkMessageNames(wsdl, collector);
        System.out.println("wsdl MessageChecker.checkMessageParts");
        MessageChecker.checkMessageParts(wsdl, collector);
        System.out.println("wsdl MessageChecker.checkUnusedMessages");
        MessageChecker.checkUnusedMessages(wsdl, collector);
        System.out.println("wsdl NamespaceChecker.checkInvalidImports");
        NamespaceChecker.checkInvalidImports(wsdl, collector);
        System.out.println("wsdl NamespaceChecker.checkNamespace");
        NamespaceChecker.checkNamespace(wsdl, collector);
        System.out.println("wsdl SchemaChecker.checkUnusedImport");
        SchemaChecker.checkUnusedImport(wsdl, collector);
        //System.out.println("wsdl OperationChecker.checkOperationNames");
        //OperationChecker.checkOperationNames(wsdl, collector);
        System.out.println("wsdl OperationChecker.checkPortTypeAndBinding");
        OperationChecker.checkPortTypeAndBinding(wsdl, collector);
        //System.out.println("wsdl PortBindingNameChecker.checkNames");
        //PortBindingNameChecker.checkNames(wsdl, collector);
        //System.out.println("wsdl PortTypeChecker.checkCardinality");
        //PortTypeChecker.checkCardinality(wsdl, collector);
        //System.out.println("wsdl PortTypeChecker.checkInputOutputMessagesAndFaults");
        //PortTypeChecker.checkInputOutputMessagesAndFaults(wsdl, collector);
        //System.out.println("wsdl PortTypeChecker.checkName");
        //PortTypeChecker.checkName(wsdl, collector);
        System.out.println("wsdl SchemaTypesChecker.checkSchemaTypes");
        SchemaTypesChecker.checkSchemaTypes(wsdl, collector);
        System.out.println("wsdl ServiceChecker.checkServices");
        ServiceChecker.checkServices(wsdl, collector);
        System.out.println("wsdl SoapBindingChecker.checkBindings");
        SoapBindingChecker.checkBindings(wsdl, collector);
        System.out.println("wsdl WsdlNameChecker.checkName");
        WsdlNameChecker.checkName(wsdl, collector);

        // WS-I checks
        Path wsdlFile = location.resolve(fileName);

        //Path relOutputPath = Paths.get("rel", "path");
        Path report = location.resolve(WsiUtil.getReportFilename(baseName));
        //Path report = Paths.get(WsiUtil.getReportFilename(baseName));
        Path config = location.resolve(WsiUtil.getConfigFilename(baseName));
        Path summary = location.resolve(WsiUtil.getSummaryFilename(baseName));
        boolean success = WsiUtil.generateConfigurationFile(toolJar, toolRoot, wsdlFile, report, config);
        if (success) {
            String p = wsdlFile.toString();
            if (p.endsWith(Paths.get("bank", "guarantee", "v1", "Guarantee.wsdl").toString())
                    || p.endsWith(Paths.get("enterprise", "worker", "v2", "Worker.wsdl").toString())
                    || p.endsWith(Paths.get("enterprise", "adresse", "v1", "ws", "AdresseStamdata.wsdl").toString())
                    || p.endsWith(Paths.get("enterprise", "adresse", "v2", "ws", "AdresseStamdata.wsdl").toString())
                    || p.endsWith(Paths.get("enterprise", "adresse", "v3", "ws", "AdresseStamdata.wsdl").toString())) {
                errors.addError("", "WS-I analysis disabled for '" + fileName + "'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Config file '" + config + "'");
                System.out.println("WSDL " + p);
            } else {
                return;
            }
            AnalysisInformationCollector wsiCollector = new AnalysisInformationCollector();
            System.out.println("wsdl wsi");
            if (WsiUtil.runAnalyzer(toolJar, toolRoot, config, summary, wsiCollector)) {
                collector.add(wsiCollector);
            } else {
                errors.addError("", "Could not run WS-I analyzer for '" + fileName + "'",
                        AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Config file '" + config + "'");
            }
        } else {
            errors.addError("", "Could not create WS-I configuration file for '" + fileName + "'",
                    AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Location '" + config + "'");
        }

        // file and path checks
        System.out.println("wsdl checkServiceFileName");
        ServiceChecker.checkServiceFileName(relPath.resolve(fileName), wsdl, collector);
        System.out.println("wsdl checkPathCharacters");
        WsdlChecker.checkPathCharacters(Utilities.pathToNamespace(domain, relPath), collector);
        System.out.println("wsdl checkPathAndTargetNamespace");
        WsdlChecker.checkPathAndTargetNamespace(wsdl, domain, relPath, collector);
        System.out.println("wsdl checkServiceNamespace");
        WsdlChecker.checkServiceNamespace(wsdl, fileName, collector);
    }

}
