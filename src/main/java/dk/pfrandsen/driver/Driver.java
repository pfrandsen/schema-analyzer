package dk.pfrandsen.driver;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.AnalyzeWsdl;
import dk.pfrandsen.UnpackTool;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.check.AssertionStatistics;
import dk.pfrandsen.check.FileSummary;
import dk.pfrandsen.check.SchemaSummary;
import dk.pfrandsen.check.WsdlSummary;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.HtmlUtil;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsiUtil;
import dk.pfrandsen.wsdl.BetaNamespaceChecker;
import dk.pfrandsen.wsdl.BindingChecker;
import dk.pfrandsen.wsdl.DocumentationChecker;
import dk.pfrandsen.wsdl.MessageChecker;
import dk.pfrandsen.wsdl.NamespaceChecker;
import dk.pfrandsen.wsdl.OperationChecker;
import dk.pfrandsen.wsdl.SchemaTypesChecker;
import dk.pfrandsen.wsdl.ServiceChecker;
import dk.pfrandsen.wsdl.SoapBindingChecker;
import dk.pfrandsen.wsdl.WsdlChecker;
import dk.pfrandsen.wsdl.WsdlNameChecker;
import dk.pfrandsen.xsd.SchemaChecker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class Driver {

    private static String arg(String argument) {
        return "-" + argument;
    }
    // commandline options
    public static final String OPTION_HELP = "help";
    public static final String OPTIONS_SOURCE_PATH = "sourcePath";
    public static final String OPTIONS_OUTPUT_PATH = "outputPath";
    public static final String OPTIONS_COMPARE_ROOT = "compareRootUri";
    public static final String OPTIONS_COPY_SRC = "copySource";
    public static final String OPTIONS_SKIP_WSI = "skipWSI";
    public static final String OPTIONS_OUTPUT_EMPTY = "outputEmptyReports";
    public static final String OPTIONS_NO_OVERWRITE = "noOverwrite";
    public static final String OPTIONS_SKIP_DIRS = "skipDirectories";
    public static final String OPTIONS_CHATTY = "chatty";
    public static final String USAGE = "Usage: java -jar <jar-file> "
            + arg(OPTIONS_SOURCE_PATH) + " <directory> "
            + arg(OPTIONS_OUTPUT_PATH) + " <directory> "
            + "[" + arg(OPTIONS_COMPARE_ROOT) + " <uri>] "
            + "[" + arg(OPTIONS_COPY_SRC) + "] "
            + "[" + arg(OPTIONS_SKIP_WSI) + "] "
            + "[" + arg(OPTIONS_OUTPUT_EMPTY) + "] "
            + "[" + arg(OPTIONS_NO_OVERWRITE) + "] "
            + "[" + arg(OPTIONS_CHATTY) + "] "
            + "[" +  arg(OPTIONS_SKIP_DIRS) + " <subdir>[,<subdir>]*] ";

    int xsd, wsdl, other;
    // top-level directories to include in analysis
    List<String> includeDirs = Arrays.asList("concept", "process", "service", "simpletype", "technical");
    // top-level directories that are skipped without generating error
    List<String> skipDirs = new ArrayList<>(); // Arrays.asList("external");
    private boolean chatty = false;
    private boolean empty = false;
    private boolean copySourceFiles = false;
    private boolean skipWsi = false;
    // error related to running the tool
    AnalysisInformationCollector errors = new AnalysisInformationCollector();
    List<SchemaSummary> schemasSummary = new ArrayList<>();
    List<WsdlSummary> wsdlSummary = new ArrayList<>();

    public static String getHtmlTemplate(String templateFilename) throws IOException {
        String separator = System.getProperty("line.separator");
        StringBuilder template = new StringBuilder();
        InputStream stream = Driver.class.getResourceAsStream("/html/templates/" + templateFilename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                template.append(line).append(separator);
            }
        }
        return template.toString();
    }


    public static void runner() { // for testing

        Driver driver = new Driver();
        String footer = "";
        String template;
        try {
            footer = driver.timing(0);
            template = getHtmlTemplate("FinalReport.html");
        } catch (IOException e) {
            e.printStackTrace();
            template = "Error";
        }
        System.out.println(template);
        System.out.println(footer);

        driver.chatty = true;
        driver.empty = true;
        driver.copySourceFiles = true;
        Path  sourcePath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("testdata");
        Path outputPath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("out");

        URI uri = null; //Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("compare").toUri();
        driver.analyze(sourcePath, outputPath, uri);
    }

    public static void main(String[] args) {
        runner();
        Driver driver = new Driver();
        CommandLine cmd;
        try {
            CommandLineParser parser = new GnuParser(); // replace with BasicParser when Apache commons-cli is released
            cmd = parser.parse(getCommandlineOptions(), args);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, getCommandlineOptions());
            return;
        }

        Path sourcePath = Paths.get(cmd.getOptionValue(OPTIONS_SOURCE_PATH));
        if (!(sourcePath.toFile().exists() && sourcePath.toFile().isDirectory())) {
            System.err.println("Fatal error: Source directory must exist, " + sourcePath);
            return;
        }
        Path outputPath = Paths.get(cmd.getOptionValue(OPTIONS_OUTPUT_PATH));
        if (cmd.hasOption(OPTIONS_NO_OVERWRITE)) {
            if (outputPath.toFile().exists()) {
                System.err.println("Fatal error: Output directory already exists, " + outputPath);
                return;
            }
        }
        if (cmd.hasOption(OPTIONS_SKIP_DIRS)) {
            String[] dirs = cmd.getOptionValues(OPTIONS_SKIP_DIRS);
            List<String> valid = driver.getRootDirectories(sourcePath);
            int whitespace = 0; // the current version of Apache CLI treat space next te separator as separate value...
            for (String d : dirs) {
                String dir = d.trim();
                if (dir.length() > 0) {
                    if (valid.contains(dir)) {
                        driver.skipDirs.add(dir);
                    } else {
                        if (cmd.hasOption(OPTIONS_CHATTY)) {
                            System.out.println("Warning: " + OPTIONS_SKIP_DIRS + " argument '" + dir + "' does not exist in"
                                    + " source root.");
                        }
                    }
                } else {
                    whitespace++;
                }
            }
            if ((driver.skipDirs.size() < (dirs.length) - whitespace) && cmd.hasOption(OPTIONS_CHATTY)) {
                int ignoreCount = dirs.length - whitespace - driver.skipDirs.size();
                System.out.println("Info: Ignoring " + ignoreCount + " director" + (ignoreCount == 1 ? "y" : "ies")
                        + " from ignore list.");
            }
        }
        driver.copySourceFiles = cmd.hasOption(OPTIONS_COPY_SRC);
        driver.empty = cmd.hasOption(OPTIONS_OUTPUT_EMPTY);
        driver.chatty = cmd.hasOption(OPTIONS_CHATTY);
        driver.skipWsi = cmd.hasOption(OPTIONS_SKIP_WSI);
        URI uri = null;
        if (cmd.hasOption(OPTIONS_COMPARE_ROOT)) {
            try {
                uri = new URI(cmd.getOptionValue(OPTIONS_COMPARE_ROOT));
            } catch (URISyntaxException e) {
                System.err.println("Fatal error: Invalid " + OPTIONS_COMPARE_ROOT + "("
                        + cmd.getOptionValue(OPTIONS_COMPARE_ROOT) + "), exception: " + e.getMessage());
                return;
            }
        }
        if (!driver.analyze(sourcePath, outputPath, uri)) {
            System.out.println("Analysis had errors, see result in " + outputPath);
        } else {
            System.out.println("Analysis succeeded");
        }
    }

    private static Options getCommandlineOptions() {
        Options options = new Options();
        Option help = new Option(OPTION_HELP, "Show usage information.");
        Option source = new Option(OPTIONS_SOURCE_PATH, true, "Root directory containing schema and wsdl source.");
        source.setRequired(true);
        Option target = new Option(OPTIONS_OUTPUT_PATH, true, "Root directory for analysis result. Created if it does."
            + " not exist. Files will be overwritten unless -" + OPTIONS_NO_OVERWRITE + " is provided.");
        target.setRequired(true);
        Option skipDirectories = new Option(OPTIONS_SKIP_DIRS, true, "Optional. List of top-level directories in root"
                + " directory to skip during analysis (comma separated).");
        skipDirectories.setRequired(false);
        skipDirectories.setArgs(Option.UNLIMITED_VALUES);
        skipDirectories.setValueSeparator(',');
        Option compare = new Option(OPTIONS_COMPARE_ROOT, true, "Optional. URI to existing errors/warnings comparison"
                + " files. If present, reports of new and resolved errors/warnings will be generated.");
        compare.setRequired(false);
        Option noOverwrite = new Option(OPTIONS_NO_OVERWRITE, false, "Optional. If present, tool will terminate if"
                + " output directory exists.");
        noOverwrite.setRequired(false);
        Option copy = new Option(OPTIONS_COPY_SRC, false, "Optional. If present, source files will be copied to"
                + " target.");
        copy.setRequired(false);
        Option skipWsi = new Option(OPTIONS_SKIP_WSI, false, "Optional. If present, WS-I checks are skipped."
                + " Default include WS-I checks. Option is included because the WS-I tools on rare occations does not"
                + " terminate.");
        skipWsi.setRequired(false);
        Option empty = new Option(OPTIONS_OUTPUT_EMPTY, false, "Optional. If present, empty reports are included in"
                + " output. Default is to only output reports with errors/warnings.");
        empty.setRequired(false);
        Option chatty = new Option(OPTIONS_CHATTY, false, "Optional. If present, progress messages wil be printed.");
        chatty.setRequired(false);

        options.addOption(help);
        options.addOption(source);
        options.addOption(target);
        options.addOption(compare);
        options.addOption(copy);
        options.addOption(noOverwrite);
        options.addOption(skipWsi);
        options.addOption(skipDirectories);
        options.addOption(empty);
        options.addOption(chatty);
        return options;
    }

    public boolean analyze(Path sourcePath, Path outputPath, URI compareToRoot) {
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
        if (!(sourcePath.toFile().exists() && sourcePath.toFile().isDirectory())) {
            System.err.println("Fatal error: Source directory must exist, " + sourcePath);
            return false;
        }
        if (!outputPath.toFile().exists()) {
            try {
                Files.createDirectory(outputPath);
            } catch (IOException e) {
                System.err.println("Fatal error: Could not create output directory, " + outputPath);
                return false;
            }
        }
        UnpackTool unpackTool = new UnpackTool();
        boolean unpacked =  unpackTool.extractTool(toolRoot);
        if (!unpacked) {
            System.err.println("Fatal error: Could not unpack WS-I tool to " + toolRoot);
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
            analyzeWsdls(sourcePath, outputPath.resolve(relResultWsdl), outputPath.resolve(relResultWsi), wsdl,
                    toolRoot);
            return generateFinalReport(start);

        } catch (IOException e) {
            logMsg("Exception while processing files, " +  e.getMessage());
            errors.addError("", "Exception while processing files",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
        }
        return true;
    }

    private boolean generateFinalReport(long start) throws IOException {
        // generate overall report
        logMsg("Generating final report");
        boolean retVal = true;
        String template = getHtmlTemplate("FinalReport.html");
        AssertionStatistics wsdlTotalErrors = new AssertionStatistics();
        AssertionStatistics wsdlTotalWarnings = new AssertionStatistics();
        AssertionStatistics schemaTotalErrors = new AssertionStatistics();
        AssertionStatistics schemaTotalWarnings = new AssertionStatistics();
        int wsdlErrors = 0, wsdlWarnings = 0, wsdlErrorsAdded = 0, wsdlWarningsAdded = 0,
                wsdlErrorsResolved = 0, wsdlWarningsResolved = 0;
        int schemaErrors = 0, schemaWarnings = 0, schemaErrorsAdded = 0, schemaWarningsAdded = 0,
                schemaErrorsResolved = 0, schemaWarningsResolved = 0;

        for (WsdlSummary summary : wsdlSummary) {
            wsdlTotalErrors.add(summary.getErrors());
            wsdlTotalWarnings.add(summary.getWarnings());
            wsdlErrors += summary.getErrors().count();
            wsdlWarnings += summary.getWarnings().count();
            wsdlErrorsAdded += summary.getErrorsAdded();
            wsdlWarningsAdded += summary.getWarningsAdded();
            wsdlErrorsResolved += summary.getErrorsResolved();
            wsdlWarningsResolved += summary.getWarningsResolved();
        }
        if (wsdlErrorsAdded > 0 || wsdlWarningsAdded > 0) {
            retVal = false;
        }




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
        if (schemaErrorsAdded > 0 || schemaWarningsAdded > 0) {
            retVal = false;
        }
        logMsg("Schema errors: " + schemaErrors + " total, " + schemaErrorsAdded + " added, "
                + schemaErrorsResolved + " resolved.");
        logMsg("Schema warnings: " + schemaWarnings + " total, " + schemaWarningsAdded + " added, "
                + schemaWarningsResolved + " resolved.");

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

        String summaryTable = getAnalysisSummaryHtml(wsdlErrors, wsdlWarnings, wsdlErrorsAdded, wsdlWarningsAdded,
        wsdlErrorsResolved, wsdlWarningsResolved, schemaErrors,
        schemaWarnings, schemaErrorsAdded, schemaWarningsAdded,
        schemaErrorsResolved, schemaWarningsResolved);
        template = template.replace("{{summary}}", summaryTable);
        if (retVal) {
            template = template.replace("{{result}}", "<span class='result-ok'>OK</span>");
        } else {
            template = template.replace("{{result}}", "<span class='result-failed'>Failed</span>");
        }
        String footer = timing(start);

        return retVal;
    }

    String getAnalysisSummaryHtml(int wsdlErrors, int wsdlWarnings, int wsdlErrorsAdded, int wsdlWarningsAdded,
                                  int wsdlErrorsResolved, int wsdlWarningsResolved, int schemaErrors,
                                  int schemaWarnings, int schemaErrorsAdded, int schemaWarningsAdded,
                                  int schemaErrorsResolved, int schemaWarningsResolved) throws IOException {
        String template = getHtmlTemplate("SummaryTable.html");
        // WSDL table row
        template = template.replace("{{WSDL_ERR_ADDED}}", "" + wsdlErrorsAdded);
        template = template.replace("{{WSDL_ERR_RESOLVED}}", "" + wsdlErrorsResolved);
        template = template.replace("{{WSDL_WARN_ADDED}}", "" + wsdlWarningsAdded);
        template = template.replace("{{WSDL_WARN_RESOLVED}}", "" + wsdlWarningsResolved);
        template = template.replace("{{WSDL_ERR_TOTAL}}", "" + wsdlErrors);
        template = template.replace("{{WSDL_WARN_TOTAL}}", "" + wsdlWarnings);
        // schema table row
        template = template.replace("{{SCHEMA_ERR_ADDED}}", "" + schemaErrorsAdded);
        template = template.replace("{{SCHEMA_ERR_RESOLVED}}", "" + schemaErrorsResolved);
        template = template.replace("{{SCHEMA_WARN_ADDED}}", "" + schemaWarningsAdded);
        template = template.replace("{{SCHEMA_WARN_RESOLVED}}", "" + schemaWarningsResolved);
        template = template.replace("{{SCHEMA_ERR_TOTAL}}", "" + schemaErrors);
        template = template.replace("{{SCHEMA_WARN_TOTAL}}", "" + schemaWarnings);
        return template;
    }

    private String timing(long start) throws IOException {
        String template = getHtmlTemplate("FinalReportFooter.html");
        long duration = System.currentTimeMillis() - start;
        long ms = duration % 1000;
        long s = duration / 1000 % 60;
        long m = duration / (60 * 1000) % 60;
        int files = wsdlSummary.size() + schemasSummary.size();
        String version = "version not determined";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        // try to load version info from maven properties
        try {
            Properties p = new Properties();
            InputStream is = getClass().
                    getResourceAsStream("/META-INF/maven/pfrandsen/schema-analyzer/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (Exception ignore) {
        }

        template = template.replace("{{files}}", "" + files);
        template = template.replace("{{minutes}}", "" + m);
        template = template.replace("{{seconds}}", "" + s);
        template = template.replace("{{milliseconds}}", "" + ms);
        template = template.replace("{{version}}", "" + escapeHtml(version));
        template = template.replace("{{time}}", "" + dateFormat.format(date));
        logMsg("\n" + files + " files processed in " + m + "m " + s + "s " + ms + "ms");
        return template;
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

    //Path root, Path wsdlTarget, List<Path> wsdl, URI compareRoot, Path diffTarget,
    //Path resultToSrc
    private void analyzeWsdls(Path root, Path wsdlTarget, Path wsiTarget,  List<Path> wsdl, Path toolRoot)
                              throws IOException {
        int count = 1;
        for (Path file : wsdl) {
            logMsg(".", count++ % 50 == 0);
            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            Path relPath = root.relativize(file);
            Path topLevel = relPath.subpath(0, 1); // top level is logically equal to domain (prefix of domain)
            Path logicalPath = topLevel.relativize(relPath).getParent(); // remove top level and filename
            Utf8.checkUtf8File(root, file, collector);
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            String fileContents = Utilities.getContentWithoutUtf8Bom(file);
            String fileName = file.toFile().getName();
            String domain = dirToNamespace(topLevel);

            if (!skipWsi) {
                Path location = wsiTarget.resolve(relPath).getParent();
                checkWsdlWsi(file, collector, fileName, relPath, domain, location, toolRoot);
            }
            // first do ws-i check the do the other wsdl checks
            //checkWsdl(fileContents, collector, file.toFile().getName(), logicalPath, dirToNamespace(topLevel));
        }
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

    private void checkWsdlWsi(Path wsdl, AnalysisInformationCollector collector, String fileName,
                           Path relPath, String domain, Path location, Path toolRoot) {
        String baseName = FilenameUtils.getBaseName(fileName);
        Utilities.createDirs(location);
        Path report = location.resolve(WsiUtil.getReportFilename(baseName));
        //Path report = Paths.get(WsiUtil.getReportFilename(baseName));
        Path config = location.resolve(WsiUtil.getConfigFilename(baseName));
        Path summary = location.resolve(WsiUtil.getSummaryFilename(baseName));
        boolean success = WsiUtil.generateConfigurationFile(toolRoot, wsdl, report, config);
        if (success) {
            logMsg("SUCCESS generating ws-i config file");
            AnalyzeWsdl wsiAnalyzer = new AnalyzeWsdl();
            if (!wsiAnalyzer.analyzeWsdl(toolRoot, config, collector)) {
                logMsg("Error running ws-i analysis");
            } else {
                logMsg("SUCCESS running ws-i analysis");
            }
        } else {
            logMsg("Error generating ws-i config file");
        }
    }

    private void checkWsdl(String wsdl, AnalysisInformationCollector collector, String fileName,
                                  Path relPath, String domain, Path location, Path toolRoot) {


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
        /*boolean success = WsiUtil.generateConfigurationFile(toolRoot, wsdl, report, config);
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
        }*/

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
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
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

    private List<String> getRootDirectories(Path rootDirectory) {
        // iterate top level directories and get their names
        List<String> dirs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectory)) {
            for (Path entry : stream) {
                if (entry.toFile().isDirectory()) {
                    dirs.add(entry.toFile().getName());
                }
            }
        } catch (IOException ignored) {
        }
        return dirs;
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
