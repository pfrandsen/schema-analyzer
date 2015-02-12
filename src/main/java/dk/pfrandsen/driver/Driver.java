package dk.pfrandsen.driver;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.check.AssertionStatistics;
import dk.pfrandsen.check.FileSummary;
import dk.pfrandsen.check.SchemaSummary;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.HtmlUtil;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsiUtil;
import dk.pfrandsen.wsdl.DocumentationChecker;
import dk.pfrandsen.xsd.SchemaChecker;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class Driver {

    // commandline options
    public static String USAGE = "Usage: java -jar <jar-file> ";
    public static String OPTION_HELP = "help";
    public static String OPTIONS_SOURCE_PATH = "sourcePath";
    public static String OPTIONS_OUTPUT_PATH = "outputPath";
    public static String OPTIONS_WSI_TOOL = "wsiToolJar";
    public static String OPTIONS_COMPARE_ROOT = "compareRootUri";
    public static String OPTIONS_COPY_SRC = "copySource";
    public static String OPTIONS_OUTPUT_EMPTY = "outputEmptyReports";
    public static String OPTIONS_CHATTY = "chatty";

    //static private Path toolJar, toolRoot;
    //static int errorCount, warningCount, infoCount;
    int xsd, wsdl, other;
    // top-level directories to include in analysis
    List<String> includeDirs = Arrays.asList("concept", "process", "service", "simpletype", "technical");
    // top-level directories that are skipped without generating error
    List<String> skipDirs = new ArrayList<>(); // Arrays.asList("external");
    boolean chatty;
    private boolean empty;
    // error related to running the tool
    AnalysisInformationCollector errors = new AnalysisInformationCollector();
    List<SchemaSummary> schemasSummary = new ArrayList<>();

    private Options getCommandlineOptions() {
        Options options = new Options();

        Option help = new Option(OPTION_HELP, "Show usage information.");

        Option source = new Option(OPTIONS_SOURCE_PATH, true, "Root directory containing schema and wsdl source.");
        source.setRequired(true);
        Option target = new Option(OPTIONS_OUTPUT_PATH, true, "Root directory for analysis result. Must not exist.");
        target.setRequired(true);
        Option wsiJar = new Option(OPTIONS_WSI_TOOL, true, "Path to WS-I tools jar file.");
        wsiJar.setRequired(true);

        Option compare = new Option(OPTIONS_COMPARE_ROOT, true, "URI to analysis comparison files. Optional.");
        compare.setRequired(false);

        Option copy = new Option(OPTIONS_COPY_SRC, false, "If present source files will be copied to target."
                + " Optional.");
        copy.setRequired(false);
        Option empty = new Option(OPTIONS_OUTPUT_EMPTY, false, "If present empty reports are included in output."
                + " Default is to only output reports with errors/warnings. Optional.");
        empty.setRequired(false);
        Option chatty = new Option(OPTIONS_CHATTY, false, "If present progress messages wil be printed. Optional.");
        chatty.setRequired(false);

        options.addOption(help);
        options.addOption(source);
        options.addOption(target);
        options.addOption(wsiJar);
        options.addOption(compare);
        options.addOption(copy);
        options.addOption(empty);
        options.addOption(chatty);
        return options;
    }

    public boolean analyze(Path sourcePath, Path outputPath, Path wsiToolJar, URI compareToRoot) {
        return analyze(sourcePath, outputPath, wsiToolJar, compareToRoot, true, true, false);
    }

    public boolean analyze(Path sourcePath, Path outputPath, Path wsiToolJar, URI compareToRoot,
                                  boolean copySourceFiles, boolean chatty, boolean empty) {
        long start = System.currentTimeMillis();
        Path relTargetSrc = Paths.get("src");
        Path relResult = Paths.get("result");
        Path relResultSchema = relResult.resolve("schema");
        Path relResultSchemaDiff = relResult.resolve("schema-diff");
        Path relResultWsdl = relResult.resolve("wsdl");
        Path relResultWsi = relResult.resolve("wsi");
        Path toolRoot = outputPath.resolve("wsi-tool");

        // find the relative path between the source location and the result location
        Path resultToSrc = outputPath.resolve(relResult).relativize(sourcePath);
        if (copySourceFiles) {
            resultToSrc = outputPath.resolve(relResult).relativize(outputPath.resolve(relTargetSrc));
        }
        this.chatty = chatty;
        this.empty = empty;
        if (!(sourcePath.toFile().exists() && sourcePath.toFile().isDirectory())) {
            System.err.println("Fatal error: Source directory must exist, " + sourcePath);
            return false;
        }
        if (outputPath.toFile().exists()) {
            System.err.println("Fatal error: Output directory already exists, " + outputPath);
            return false;
        }
        if (!(wsiToolJar.toFile().exists() && wsiToolJar.toFile().isFile())) {
            System.err.println("Fatal error: WS-I tool jar must exist, " + wsiToolJar);
            return false;
        }
        boolean unpacked = WsiUtil.unpackCheckerTool(wsiToolJar, toolRoot);
        if (!unpacked) {
            System.err.println("Fatal error: Could not unpack WS-I tool '" + wsiToolJar + "' to " + toolRoot);
            return false;
        }

        List<Path> schema = new ArrayList<>();
        List<Path> wsdl = new ArrayList<>();
        List<Path> ignore = new ArrayList<>();
        try {
            List<Path> topLevelDirs = new ArrayList<>();
            logMsg("Collecting top-level folders...");
            getRootDirectories(sourcePath, topLevelDirs, ignore, includeDirs, skipDirs);
            logMsg("Collecting source files...");
            for (Path dir: topLevelDirs) {
                getFiles(dir, schema, wsdl, ignore);
            }
            logMsg("Found: " + schema.size() + " schemas, " + wsdl.size() + " wsdls");
            collectIgnoredErrors(ignore, sourcePath);
            if (copySourceFiles) {
                copySource(sourcePath, outputPath.resolve(relTargetSrc), schema, wsdl);
            }
            logMsg("Analyzing " + schema.size() + " schemas");
            URI compare = compareToRoot != null ? compareToRoot.resolve("schema") : null;
            analyzeSchemas(sourcePath, outputPath.resolve(relResultSchema), schema, compare,
                    outputPath.resolve(relResultSchemaDiff), resultToSrc);
            logMsg("Analyzing " + wsdl.size() + " wsdls");
            analyzeWsdls(sourcePath, outputPath.resolve(relResultWsdl), outputPath.resolve(relResultWsi), wsdl);

            generateFinalReport(start);

        } catch (IOException e) {
            logMsg("Exception while processing files, " +  e.getMessage());
            errors.addError("", "Exception while processing files",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
        }
        return true;
    }

    private void generateFinalReport(long start) {
        long duration = System.currentTimeMillis() - start;
        long millis = duration % 1000;
        long seconds = duration / 1000 % 60;
        long minutes = duration / (60 * 1000) % 60;
        logMsg("\n" + schemasSummary.size() + " files processed in " + minutes + "m " + seconds + "s " + millis + "ms");
        // generate overall report
        AssertionStatistics schemaTotalErrors = new AssertionStatistics();
        AssertionStatistics schemaTotalWarnings = new AssertionStatistics();
        int schemaErrors = 0, schemaWarnings = 0, schemaErrorsAdded = 0, schemaWarningsAdded = 0,
                schemaErrorsResolved = 0, schemaWarningsResolved = 0;
        for (SchemaSummary summary : schemasSummary) {
            schemaTotalErrors.add(summary.getErrors());
            schemaTotalWarnings.add(summary.getWarnings());
            schemaErrors += summary.getErrors().count();
            schemaWarnings += summary.getWarnings().count();
            schemaErrorsAdded += summary.getErrorsAdded();
            schemaWarningsAdded += summary.getWarningsAdded();
            schemaErrorsResolved += summary.getErrorsResolved();
            schemaWarningsResolved += summary.getWarningsResolved();
        }
        logMsg("Schema errors: " + schemaErrors + " total, " + schemaErrorsAdded + " added, "
                + schemaErrorsResolved + " resolved.");
        logMsg("Schema warnings: " + schemaWarnings + " total, " + schemaWarningsAdded + " added, "
                + schemaWarningsResolved + " resolved.");
        // SortedSet<Map.Entry<String, Integer>> set = schemaTotalErrors.getSortedByValue();
        logMsg("\nErrors in schemas:");
        for (Map.Entry<String, Integer> entry : schemaTotalErrors.getSortedByValue()) {
            logMsg(entry.getValue() + " " + entry.getKey());
        }
        logMsg("\nWarnings in schemas:");
        for (Map.Entry<String, Integer> entry : schemaTotalWarnings.getSortedByValue()) {
            logMsg(entry.getValue() + " " + entry.getKey());
        }
        logMsg("\nErrors in schemas by assertion:");
        for (Map.Entry<String, Integer> entry : schemaTotalErrors.getSortedByValue()) {
            logMsg("\nAssertion: " + entry.getKey());
            for (SchemaSummary summary : schemasSummary) {
                int count = summary.getErrors().countByAssertion(entry.getKey());
                if (count > 0) {
                    logMsg("  " + count + " in " + summary.getName() + " " + summary.getFilePath());
                }
            }
        }
        logMsg("\nWarnings in schemas by assertion:");
        for (Map.Entry<String, Integer> entry : schemaTotalWarnings.getSortedByValue()) {
            logMsg("\nAssertion: " + entry.getKey());
            for (SchemaSummary summary : schemasSummary) {
                int count = summary.getWarnings().countByAssertion(entry.getKey());
                if (count > 0) {
                    logMsg("  " + count + " in " + summary.getName() + " " + summary.getFilePath());
                }
            }
        }
    }

    public static void main(String[] args) {
        Driver driver = new Driver();
        Path  sourcePath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("testdata");
        Path outputPath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("out");

        URI uri = null; //Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("compare").toUri();
        driver.analyze(sourcePath, outputPath, Paths.get("lib", "wsi-checker-1.0-SNAPSHOT.jar"), uri);
    }

    private void analyzeSchemas(Path root, Path xsdTarget, List<Path> schema, URI compareRoot, Path diffTarget,
                                       Path resultToSrc) throws IOException {
        int count = 1;
        for (Path file : schema) {
            logMsg(".", count++ % 50 == 0);
            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            Path relPath = root.relativize(file);
            Path topLevel = relPath.subpath(0, 1); // top level is logically equal to domain (prefix of domain)
            Path logicalPath = topLevel.relativize(relPath).getParent(); // remove top level and filename
            Utf8.checkUtf8File(root, file, collector);
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            String fileContents = Utilities.getContentWithoutUtf8Bom(file);
            checkSchema(fileContents, collector, file.toFile().getName(), logicalPath, dirToNamespace(topLevel));
            AnalysisInformationCollector added = collector; // default is that all errors/warnings are new
            AnalysisInformationCollector resolved = new AnalysisInformationCollector(); // none resolved
            if (compareRoot != null) {
                // load json file to compare with
                String name = FilenameUtils.getBaseName(file.toFile().getName()) + ".json";
                URI uri = Utilities.appendPath(compareRoot, relPath.getParent().resolve(name));
                try (InputStream stream = uri.toURL().openStream()) {
                    AnalysisInformationCollector ref = AnalysisInformationCollector.fromJson(stream);
                    added = collector.except(ref);
                    resolved = collector.except(ref);
                } catch (Exception ignored) {
                    // assume that compare target does not exist - all errors/warnings are new
                }
            }

            // Generate reports and collect stats about errors/warnings
            Path outputDirFull = xsdTarget.resolve(relPath).getParent();
            Path outputDirDiff = diffTarget.resolve(relPath).getParent();
            String filename = file.toFile().getName(); // filename
            String baseName = FilenameUtils.getBaseName(filename);
            SchemaSummary summary = new SchemaSummary(resultToSrc.resolve(relPath), added, resolved);
            addAssertionStatistics(summary, collector);
            if ((!collector.isEmpty()) || (empty)) {
                // write full report
                writeJsonReport(outputDirFull, filename, collector);
                summary.setFullReport(outputDirFull.resolve(baseName + ".json"));
                // write html report
                writeHtmlReport(outputDirFull, filename, collector);
                summary.setFullReportHtml(outputDirFull.resolve(baseName + ".html"));
            }
            if ((!added.isEmpty()) || (empty)) {
                // write report of added errors/warnings
                writeJsonReport(outputDirDiff, filename, added);
                summary.setAddedReport(outputDirDiff.resolve(baseName + ".json"));
            }
            if ((!added.isEmpty()) || (!resolved.isEmpty()) || (empty)) {
                // write html report of added/resolved errors/warnings
                writeHtmlReport(outputDirDiff, filename, added, resolved, collector);
                summary.setDiffReportHtml(outputDirDiff.resolve(baseName + ".json"));
            }

            schemasSummary.add(summary);
        }
        logMsg("\nDone analyzing schemas");
    }

    private void addAssertionStatistics(FileSummary summary, AnalysisInformationCollector collector) {
        AssertionStatistics errorStatistics = new AssertionStatistics();
        AssertionStatistics warningStatistics = new AssertionStatistics();
        errorStatistics.add(collector.getErrors());
        warningStatistics.add(collector.getWarnings());
        summary.setErrors(errorStatistics);
        summary.setWarnings(warningStatistics);
    }

    private void analyzeWsdls(Path root, Path wsdlTarget,Path wsiTarget,  List<Path> schema) {

    }

    private void checkSchema(String schema, AnalysisInformationCollector collector, String fileName,
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

    private void writeJsonReport(Path location, String filename, AnalysisInformationCollector collector)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        Path jsonOut = location.resolve(baseName + ".json");
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile());
    }

    private void writeHtmlReport(Path location, String filename, AnalysisInformationCollector collector)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        String ext = FilenameUtils.getExtension(filename);
        Path htmlOut = location.resolve(baseName + ".html");
        String htmlFragment = HtmlUtil.toHtmlTable(collector, true);
        FileUtils.writeStringToFile(htmlOut.toFile(), HtmlUtil.toHtml(htmlFragment, false, true, baseName, ext));
    }

    private void writeHtmlReport(Path location, String filename, AnalysisInformationCollector added,
                                        AnalysisInformationCollector resolved, AnalysisInformationCollector all)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        String ext = FilenameUtils.getExtension(filename);
        Path htmlOut = location.resolve(baseName + ".html");
        String htmlFragment = "<h2>New errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(added, true);
        htmlFragment += "<h2>Resolved errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(resolved, true);
        htmlFragment += "<h2>All errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(all, true);
        FileUtils.writeStringToFile(htmlOut.toFile(), HtmlUtil.toHtml(htmlFragment, false, true, baseName, ext));
    }

    private String dirToNamespace(Path path) {
        String name = path.toFile().getName();
        if (includeDirs.contains(name)) {
            return name + ".schemas.nykreditnet.net";
        }
        return name;
    }

    private void copySource(Path root, Path srcTarget, List<Path> schema, List<Path> wsdl) throws IOException {
        logMsg("Copying schemas");
        for (Path src : schema) {
            copy(src, srcTarget.resolve(root.relativize(src)));
        }
        logMsg("Copying wsdls");
        for (Path src : wsdl) {
            copy(src, srcTarget.resolve(root.relativize(src)));
        }
    }

    private void copy(Path from, Path to) throws IOException {
        Utilities.createDirs(to.getParent());
        Files.copy(from, to);
    }

    private void getRootDirectories(Path rootDirectory, List<Path> include, List<Path> ignored,
                                           List<String> filter, List<String> skipWithoutError) throws IOException {
        // iterate top level directories and find the ones to include
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectory)) {
            for (Path entry : stream) {
                if (entry.toFile().isDirectory()) {
                    String name = entry.toFile().getName();
                    if (filter.contains(name)) {
                        include.add(entry);
                    } else {
                        if (!skipWithoutError.contains(name)) {
                            ignored.add(entry);
                        } else {
                            logMsg("Root directory '" + name + "' not included in analysis.");
                        }
                    }
                } else {
                    ignored.add(entry); // ignoring files found in root directory
                }
            }
        }
    }

    private void getFiles(final Path rootDirectory, final List<Path> schema, final List<Path> wsdl,
                                 final List<Path> ignored) throws IOException {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                String ext = FilenameUtils.getExtension(f.getName());
                Path filePath = Paths.get(f.getAbsolutePath());
                //logMsg(".", false);
                if ("xsd".equalsIgnoreCase(ext)) {
                    schema.add(filePath);
                } else if ("wsdl".equalsIgnoreCase(ext)) {
                    wsdl.add(filePath);
                } else {
                    // file with invalid extension
                    ignored.add(filePath);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void collectIgnoredErrors(List<Path> invalid, Path rootDirectory) {
        if (invalid.size() > 0)
            logMsg("Skipping " + invalid.size() + " files/directories with invalid extension or location");
        for (Path file : invalid) {
            Path rel = rootDirectory.relativize(file);
            logMsg("Ignoring " + rel);
            other++;
            errors.addError("", "Invalid file/directory. Reason: Extension not {xsd,wsdl} or invalid location.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL,
                    "Path: '" + rel + "'.");
        }
    }

    private void logMsg(String msg) {
        logMsg(msg, true);
    }

    private void logMsg(String msg, boolean newline) {
        if (chatty) {
            if (newline) {
                System.out.println(msg);
            } else {
                System.out.print(msg);
            }
        }
    }

}
