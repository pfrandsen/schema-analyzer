package dk.pfrandsen.driver;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.AnalyzeWsdl;
import dk.pfrandsen.UnpackTool;
import dk.pfrandsen.check.AnalysisInformation;
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
import dk.pfrandsen.wsdl.PortBindingNameChecker;
import dk.pfrandsen.wsdl.PortTypeChecker;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringEscapeUtils;

public class Driver {

    // report html template names
    private static final String FR_TEMPLATE = "FinalReport";
    private static final String FR_SUMMARY_TEMPLATE = "FinalReportSummary";
    private static final String FR_FOOTER_TEMPLATE = "FinalReportFooter";
    private static final String FR_WSDL_SUMMARY_TEMPLATE = "FinalReportWsdlSummary";
    private static final String FR_SCHEMA_SUMMARY_TEMPLATE = "FinalReportSchemaSummary";
    private static final String FR_WSDL_FILE_LIST_TEMPLATE = "FinalReportWsdlFileList";
    private static final String FR_WSDL_FILE_LIST_ELEMENT_TEMPLATE = "FinalReportWsdlFileListElement";
    private static final String FR_SCHEMA_FILE_LIST_TEMPLATE = "FinalReportSchemaFileList";
    private static final String FR_SCHEMA_FILE_LIST_ELEMENT_TEMPLATE = "FinalReportSchemaFileListElement";
    private static final String STANDARD_REPORT_TEMPLATE = "StandardReport";
    private static final String DIFF_REPORT_TEMPLATE = "DiffReport";
    private static final String WSDL_CSS_TEMPLATE = "WsdlCss";
    private static final String SCHEMA_CSS_TEMPLATE = "XsdCss";

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

    // top-level directories to include in analysis
    List<String> includeDirs = Arrays.asList("concept", "process", "service", "simpletype", "technical");
    // top-level directories that are skipped without generating error
    List<String> skipDirs = new ArrayList<>(); // Arrays.asList("external");
    private int compareCount = 0;
    private boolean chatty = false;
    private boolean empty = false;
    private boolean copySourceFiles = false;
    private boolean skipWsi = false;
    // error related to running the tool
    AnalysisInformationCollector otherErrors = new AnalysisInformationCollector();
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

    Map<String, String> getFinalReportTemplates(boolean includeCompare) throws IOException {
        String path = includeCompare ? "" : "nodiff/";
        String root = "FinalReport/";
        Map<String, String> templates = new TreeMap<>();

        templates.put(FR_TEMPLATE, getHtmlTemplate("FinalReport/Report.html"));
        templates.put(FR_SUMMARY_TEMPLATE, getHtmlTemplate("FinalReport/" + path + "SummaryTable.html"));
        templates.put(FR_FOOTER_TEMPLATE, getHtmlTemplate("FinalReport/Footer.html"));
        templates.put(FR_WSDL_SUMMARY_TEMPLATE, getHtmlTemplate(root + "types/" + path + "SummaryTable.html"));
        templates.put(FR_SCHEMA_SUMMARY_TEMPLATE, getHtmlTemplate(root + "types/" + path + "SummaryTable.html"));

        String fileListRoot = root + "types/filelist/" + path;
        templates.put(FR_WSDL_FILE_LIST_TEMPLATE, getHtmlTemplate(fileListRoot + "Container.html"));
        templates.put(FR_WSDL_FILE_LIST_ELEMENT_TEMPLATE, getHtmlTemplate(fileListRoot + "Element.html"));
        templates.put(FR_SCHEMA_FILE_LIST_TEMPLATE, getHtmlTemplate(fileListRoot + "Container.html"));
        templates.put(FR_SCHEMA_FILE_LIST_ELEMENT_TEMPLATE, getHtmlTemplate(fileListRoot + "Element.html"));

        root = "Report/";
        templates.put(STANDARD_REPORT_TEMPLATE, getHtmlTemplate(root + "Standard.html"));
        templates.put(DIFF_REPORT_TEMPLATE, getHtmlTemplate(root + "Diff.html"));

        templates.put(WSDL_CSS_TEMPLATE, getHtmlTemplate(root + "css/wsdl.css"));
        templates.put(SCHEMA_CSS_TEMPLATE, getHtmlTemplate(root + "css/xsd.css"));
        return templates;
    }

    public static void main(String[] args) {
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
        System.out.println("Analyzer version: " + driver.getToolVersion());
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
                            System.out.println("Warning: " + OPTIONS_SKIP_DIRS + " argument '" + dir + "' does not"
                                    + " exist in source root.");
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
        Path relReport = relResult.resolve("AnalysisReport.html");
        Path relLog = relResult.resolve("log");
        Path relErr = relLog.resolve("System-err.log");
        Path relWsiOut = relLog.resolve("wsi-out.log"); // wsi analyzer prints messages to stdout, redirect to file
        Path relResultSchema = relResult.resolve("schema");
        Path relResultSchemaDiff = relResult.resolve("schema-diff");
        Path relSchemaHtml = relResult.resolve("schema-html");
        Path relResultWsdl = relResult.resolve("wsdl");
        Path relResultWsdlDiff = relResult.resolve("wsdl-diff");
        Path relWsdlHtml = relResult.resolve("wsdl-html");
        Path relResultWsi = relResult.resolve("wsi");
        Path toolRoot = outputPath.resolve("wsi-tool");
        Path tmp = outputPath.resolve("tmp");
        Path srcDiff = outputPath.resolve("src-diff");

        // find the relative path between the source location and the result location, both locations are in local fs
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
        Utilities.createDirs(outputPath.resolve(relLog));
        UnpackTool unpackTool = new UnpackTool();
        boolean unpacked =  unpackTool.extractTool(toolRoot);
        if (!unpacked) {
            System.err.println("Fatal error: Could not unpack WS-I tool to " + toolRoot);
            return false;
        }
        // unpack resources used for wsdl and schema source diff reports
        if (!unpackSourceDiffResources(srcDiff)) {
            // TODO:
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
            Map<String, String> templates = getFinalReportTemplates(compareToRoot != null);
            // until issue with sax parser is fixed (newer version is likely needed) redirect error stream to file
            PrintStream stdErr = System.err;
            boolean redirected = redirectStdErr(outputPath.resolve(relErr).toFile());
            try {
                analyzeSchemas(templates, sourcePath, outputPath.resolve(relResultSchema), schema, compareToRoot,
                        outputPath.resolve(relResultSchemaDiff), outputPath.resolve(relSchemaHtml), resultToSrc);
                logMsg("Analyzing " + wsdl.size() + " wsdls");
                FileUtils.deleteQuietly(outputPath.resolve(relWsiOut).toFile());
                analyzeWsdls(templates, sourcePath, outputPath.resolve(relResultWsdl), outputPath.resolve(relResultWsi),
                        wsdl, compareToRoot, outputPath.resolve(relResultWsdlDiff), outputPath.resolve(relWsdlHtml),
                        resultToSrc, outputPath.resolve(relWsiOut), tmp, toolRoot);
            }
            finally {
                // reset std err
                if (redirected) {
                    System.err.close();
                    System.setErr(stdErr);
                }
            }
            generateFinalReport(templates, outputPath.resolve(relReport),  compareToRoot != null, start);

        } catch (IOException e) {
            logMsg("Exception while processing files, " +  e.getMessage());
            otherErrors.addError("", "Exception while processing files",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
            return false;
        }
        return analysisSucceeded();
    }

    boolean redirectStdErr(File errFile) {
        try {
            System.setErr(new PrintStream(new FileOutputStream(errFile)));
            return true;
        } catch (FileNotFoundException e) {
            logMsg("Could not redirect stderr to " + errFile);
        }
        return false;
    }

    boolean unpackSourceDiffResources(Path srcDiff) {
        byte[] buffer = new byte[2 * 1024];

        Utilities.createDirs(srcDiff);
        InputStream stream = Driver.class.getResourceAsStream("/predic8/zip/web.zip");
        ZipInputStream zipStream = new ZipInputStream(stream);
        try {
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                if (entry.isDirectory()) {
                    Path folder = srcDiff.resolve(fileName);
                    Utilities.createDirs(folder);
                } else {
                    File f = srcDiff.resolve(fileName).toFile();
                    FileOutputStream outputStream = new FileOutputStream(f);
                    int length;
                    while ((length = zipStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                }
                entry = zipStream.getNextEntry();
            }
            zipStream.close();
        } catch (IOException e) {
            System.err.println("Exception " + e.getMessage());
            return false;
        }
        return true;

    }

    boolean analysisSucceeded() {
        int errorCount = otherErrors.errorCount(), warningCount = 0;
        for (WsdlSummary summary : wsdlSummary) {
            errorCount += summary.getErrorsAdded();
            warningCount += summary.getWarningsAdded();
        }
        for (SchemaSummary summary : schemasSummary) {
            errorCount += summary.getErrorsAdded();
            warningCount += summary.getWarningsAdded();
        }
        return errorCount == 0 && warningCount == 0;
    }

    // resultToSrc - path to source files (used for creating links in report)
    private void analyzeSchemas(Map<String, String> templates, Path root, Path xsdTarget, List<Path> schema,
                                URI compareRoot, Path diffTarget, Path schemaHtml, Path resultToSrc)
            throws IOException {
        int count = 1;
        for (Path file : schema) {
            logMsg(".", count++ % 50 == 0);
            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            Path relPath = root.relativize(file);
            Path topLevel = relPath.subpath(0, 1); // top level is logically equal to domain (prefix of domain)
            Path logicalPath = topLevel.relativize(relPath).getParent(); // remove top level and filename
            String fileName = file.toFile().getName();
            String domain = dirToNamespace(topLevel);
            String html = "", compareHtml = "";

            Utf8.checkUtf8File(root, file, collector);
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            String fileContents = Utilities.getContentWithoutUtf8Bom(file);
            try {
                html = HtmlUtil.schemaToHtml(fileContents, false);
            } catch (Exception ignored) {
            }
            checkSchema(fileContents, collector, fileName, logicalPath, domain);
            AnalysisInformationCollector added = collector; // default is that all errors/warnings are new
            AnalysisInformationCollector resolved = new AnalysisInformationCollector(); // none resolved
            if (compareRoot != null) {
                // if "compare to" resource exists run analyzer on it and compute diff
                URI uri = Utilities.appendPath(compareRoot, relPath);
                try (InputStream is = uri.toURL().openStream()) {
                    is.close(); // will be auto closed. but done here to avoid multiple concurrent open connections
                    logMsg(" ", count++ % 50 == 0);
                    compareCount++;
                    AnalysisInformationCollector ref = new AnalysisInformationCollector();
                    Utf8.checkUtf8Uri(relPath.toString(), uri, ref);
                    // assume source is UTF-8
                    String cContent = Utilities.getContentWithoutUtf8Bom(uri, Charset.availableCharsets().get("UTF-8"));
                    try {
                        compareHtml = HtmlUtil.schemaToHtml(cContent, false);
                    } catch (Exception ignored) {
                    }
                    // perform checks with content loaded from compare uri - then compute diff
                    checkSchema(cContent, ref, fileName, logicalPath, domain);
                    added = collector.except(ref);
                    resolved = ref.except(collector);
                } catch (Exception ignored) {
                    logMsg("-", count++ % 50 == 0);
                    // assume that compare target does not exist - all errors/warnings are new
                    collector.addInfo("", "Compare to schema source not found (new schema?)",
                            AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, uri + ", " + relPath);
                }
            }
            // Generate reports and collect stats about errors/warnings
            Path outputDirFull = xsdTarget.resolve(relPath).getParent();
            Path outputDirDiff = diffTarget.resolve(relPath).getParent();
            String baseName = FilenameUtils.getBaseName(fileName);
            SchemaSummary summary = new SchemaSummary(resultToSrc.resolve(relPath), added, resolved,
                    collector.infoCount());
            addAssertionStatistics(summary, collector);
            if (html.length() > 0) {
                Path htmlOut = schemaHtml.resolve(relPath).getParent().resolve(baseName + ".html");
                summary.setSourceHtml(htmlOut);
                writeHtmlSource(htmlOut, html);
            }
            if ((!collector.isEmpty()) || (empty)) {
                // write full report
                writeJsonReport(outputDirFull, fileName, collector);
                summary.setFullReport(outputDirFull.resolve(baseName + ".json"));
                // write html report
                writeHtmlReport(templates.get(STANDARD_REPORT_TEMPLATE), templates.get(SCHEMA_CSS_TEMPLATE),
                        HtmlUtil.htmlBody(html), outputDirFull, fileName, collector);
                summary.setFullReportHtml(outputDirFull.resolve(baseName + ".html"));
            }
            if ((!added.isEmpty()) || (empty)) {
                // write report of added errors/warnings
                writeJsonReport(outputDirDiff, fileName, added);
                summary.setAddedReport(outputDirDiff.resolve(baseName + ".json"));
            }
            if ((!added.isEmpty()) || (!resolved.isEmpty()) || (empty)) {
                // write html report of added/resolved errors/warnings
                writeHtmlReport(templates.get(DIFF_REPORT_TEMPLATE), outputDirDiff, fileName, added, resolved, collector);
                summary.setDiffReportHtml(outputDirDiff.resolve(baseName + ".html"));
            }
            schemasSummary.add(summary);
        }
        logMsg("\nDone analyzing schemas");
    }

    // resultToSrc - path to source files (used for creating links in report)
    private void analyzeWsdls(Map<String, String> templates, Path root, Path wsdlTarget, Path wsiTarget,
                              List<Path> wsdl, URI compareRoot,  Path diffTarget, Path wsdlHtml, Path resultToSrc,
                              Path wsiOut, Path tmp, Path toolRoot) throws IOException {
        int count = 1;
        for (Path file : wsdl) {
            logMsg(".", count++ % 50 == 0);
            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            Path relPath = root.relativize(file);
            Path topLevel = relPath.subpath(0, 1); // top level is logically equal to domain (prefix of domain)
            Path logicalPath = topLevel.relativize(relPath).getParent(); // remove top level and filename
            String fileName = file.toFile().getName();
            String domain = dirToNamespace(topLevel);
            String html = "", compareHtml = "";

            Utf8.checkUtf8File(root, file, collector);
            // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
            String fileContents = Utilities.getContentWithoutUtf8Bom(file);
            try {
                html = HtmlUtil.wsdlToHtml(fileContents, false);
            } catch (Exception ignored) {
            }

            // first do ws-i check the do the other wsdl checks
            if (!skipWsi) {
                Path location = wsiTarget.resolve(relPath).getParent();
                checkWsdlWsi(file, collector, fileName, location, wsiOut, toolRoot);
            }
            checkWsdl(fileContents, collector, fileName, logicalPath, domain);
            AnalysisInformationCollector added = collector; // default is that all errors/warnings are new
            AnalysisInformationCollector resolved = new AnalysisInformationCollector(); // none resolved
            if (compareRoot != null) {
                // if "compare to" resource exists run analyzer on it and compute diff
                URI uri = Utilities.appendPath(compareRoot, relPath);
                try (InputStream is = uri.toURL().openStream()) {
                    is.close(); // will be auto closed. but done here to avoid multiple concurrent open connections
                    logMsg(" ", count++ % 50 == 0);
                    compareCount++;
                    Utilities.createDirs(tmp); // make sure tmp directory for ws-i config file is created
                    AnalysisInformationCollector ref = new AnalysisInformationCollector();
                    Utf8.checkUtf8Uri(relPath.toString(), uri, ref);
                    // assume source is UTF-8
                    String cContent = Utilities.getContentWithoutUtf8Bom(uri, Charset.availableCharsets().get("UTF-8"));
                    try {
                        compareHtml = HtmlUtil.wsdlToHtml(cContent, false);
                    } catch (Exception ignored) {
                    }
                    // perform checks with content loaded from compare uri - then compute diff
                    if (!skipWsi) {
                        checkWsdlWsi(file, ref, fileName, tmp, wsiOut, toolRoot);
                    }
                    checkWsdl(cContent, ref, fileName, logicalPath, domain);
                    added = collector.except(ref);
                    resolved = ref.except(collector);
                } catch (Exception ignored) {
                    logMsg("-", count++ % 50 == 0);
                    // assume that compare target does not exist - all errors/warnings are new
                    collector.addInfo("", "Compare to wsdl source not found (new wsdl?)",
                            AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, uri + ", " + relPath);
                }
            }
            // Generate reports and collect stats about errors/warnings
            Path outputDirFull = wsdlTarget.resolve(relPath).getParent();
            Path outputDirDiff = diffTarget.resolve(relPath).getParent();
            String baseName = FilenameUtils.getBaseName(fileName);
            WsdlSummary summary = new WsdlSummary(resultToSrc.resolve(relPath), added, resolved, collector.infoCount());
            addAssertionStatistics(summary, collector);
            if (html.length() > 0) {
                Path htmlOut = wsdlHtml.resolve(relPath).getParent().resolve(baseName + ".html");
                summary.setSourceHtml(htmlOut);
                writeHtmlSource(htmlOut, html);
            }
            if ((!collector.isEmpty()) || (empty)) {
                // write full report
                writeJsonReport(outputDirFull, fileName, collector);
                summary.setFullReport(outputDirFull.resolve(baseName + ".json"));
                // write html report
            //    writeHtmlReport(outputDirFull, fileName, collector);
                writeHtmlReport(templates.get(STANDARD_REPORT_TEMPLATE), templates.get(WSDL_CSS_TEMPLATE),
                        HtmlUtil.htmlBody(html), outputDirFull,
                        fileName, collector);
                summary.setFullReportHtml(outputDirFull.resolve(baseName + ".html"));
            }
            if ((!added.isEmpty()) || (empty)) {
                // write report of added errors/warnings
                writeJsonReport(outputDirDiff, fileName, added);
                summary.setAddedReport(outputDirDiff.resolve(baseName + ".json"));
            }
            if ((!added.isEmpty()) || (!resolved.isEmpty()) || (empty)) {
                // write html report of added/resolved errors/warnings
                writeHtmlReport(templates.get(DIFF_REPORT_TEMPLATE), outputDirDiff, fileName, added, resolved, collector);
                summary.setDiffReportHtml(outputDirDiff.resolve(baseName + ".html"));
            }
            wsdlSummary.add(summary);
        }
        logMsg("\nDone analyzing wsdls");
    }

    private void addAssertionStatistics(FileSummary summary, AnalysisInformationCollector collector) {
        AssertionStatistics errorStatistics = new AssertionStatistics();
        AssertionStatistics warningStatistics = new AssertionStatistics();
        errorStatistics.add(collector.getErrors());
        warningStatistics.add(collector.getWarnings());
        summary.setErrors(errorStatistics);
        summary.setWarnings(warningStatistics);
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

    private void checkWsdlWsi(Path wsdl, AnalysisInformationCollector collector, String fileName, Path location,
                              Path wsiOut, Path toolRoot) {
        String baseName = FilenameUtils.getBaseName(fileName);
        Utilities.createDirs(location);
        Path report = location.resolve(WsiUtil.getReportFilename(baseName));
        Path config = location.resolve(WsiUtil.getConfigFilename(baseName));

        PrintStream stdOut = System.out;
        boolean redirected = false;
        if (wsiOut != null) { // redirect stdout from ws-i analyzer to file
            try {
                System.setOut(new PrintStream(new FileOutputStream(wsiOut.toFile(), true))); // append
                redirected = true;
            } catch (FileNotFoundException ignore) {
            }
        }
        if (WsiUtil.generateConfigurationFile(toolRoot, wsdl, report, config)) {
            AnalyzeWsdl wsiAnalyzer = new AnalyzeWsdl();
            if (!wsiAnalyzer.analyzeWsdl(toolRoot, config, collector)) {
                logMsg("Error running ws-i analysis\n");
            } else {
                logMsg("SUCCESS running ws-i analysis\n");
            }
        } else {
            otherErrors.addError("", "Could not generate WS-I configuration file for " + wsdl,
                    AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Target location: " + config);
            logMsg("Error generating ws-i config file\n");
        }
        // restore std out
        if (redirected) {
            System.out.close();
            System.setOut(stdOut);
        }
    }

    private void checkWsdl(String wsdl, AnalysisInformationCollector collector, String fileName,
                           Path relPath, String domain/*, Path location, Path toolRoot*/) {
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
        // file and path checks
        ServiceChecker.checkServiceFileName(relPath.resolve(fileName), wsdl, collector);
        WsdlChecker.checkPathCharacters(Utilities.pathToNamespace(domain, relPath), collector);
        WsdlChecker.checkPathAndTargetNamespace(wsdl, domain, relPath, collector);
        WsdlChecker.checkServiceNamespace(wsdl, fileName, collector);
    }

    private void generateFinalReport(Map<String, String> templates, Path reportFile, boolean compare, long start)
            throws IOException {
        // generate overall report
        logMsg("Generating final report");
        String template = templates.get(FR_TEMPLATE);
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
        template = template.replace("{{wsdl-file-count}}", wsdlSummary.size() + " WSDL file" +
                (wsdlSummary.size() == 1 ? "" : "s"));
        template = template.replace("{{wsdl-files-content}}", wsdlSummary.size() == 0 ? "display:none" : "");
        template = addAssertionsByCount(template, "{{summary-wsdl-by-count}}", wsdlTotalErrors, wsdlTotalWarnings);
        List<FileSummary> summaryList = new ArrayList<>();
        summaryList.addAll(wsdlSummary);
        template = addAssertionsByFile(template, "{{summary-wsdl-by-assertion}}",
                wsdlTotalErrors, wsdlTotalWarnings, summaryList, reportFile.getParent());
        template = addFileList(template, "{{wsdl-file-summary}}", templates.get(FR_WSDL_FILE_LIST_TEMPLATE),
                templates.get(FR_WSDL_FILE_LIST_ELEMENT_TEMPLATE), summaryList, reportFile.getParent());
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
        template = template.replace("{{schema-file-count}}", schemasSummary.size() + " schema file" +
                (schemasSummary.size() == 1 ? "" : "s"));
        template = template.replace("{{schema-files-content}}", schemasSummary.size() == 0 ? "display:none" : "");
        template = addAssertionsByCount(template, "{{summary-schema-by-count}}",
                schemaTotalErrors, schemaTotalWarnings);
        summaryList = new ArrayList<>();
        summaryList.addAll(schemasSummary);
        template = addAssertionsByFile(template, "{{summary-schema-by-assertion}}",
                schemaTotalErrors, schemaTotalWarnings, summaryList, reportFile.getParent());
        template = addFileList(template, "{{schema-file-summary}}", templates.get(FR_SCHEMA_FILE_LIST_TEMPLATE),
                templates.get(FR_SCHEMA_FILE_LIST_ELEMENT_TEMPLATE), summaryList, reportFile.getParent());
        template = addAnalysisSummaryHtml(template, "{{summary}}", templates.get(FR_SUMMARY_TEMPLATE), wsdlErrors,
                wsdlWarnings, wsdlErrorsAdded, wsdlWarningsAdded, wsdlErrorsResolved, wsdlWarningsResolved,
                schemaErrors, schemaWarnings, schemaErrorsAdded, schemaWarningsAdded, schemaErrorsResolved,
                schemaWarningsResolved, compare);
        template = addSummaryHtml(template, "{{wsdlsummary}}", "{{wsdlsummary-content}}",
                templates.get(FR_WSDL_SUMMARY_TEMPLATE), wsdlErrors,  wsdlWarnings, wsdlErrorsAdded, wsdlWarningsAdded,
                wsdlErrorsResolved, wsdlWarningsResolved);
        template = addSummaryHtml(template, "{{schemasummary}}", "{{schemasummary-content}}",
                templates.get(FR_SCHEMA_SUMMARY_TEMPLATE), schemaErrors, schemaWarnings, schemaErrorsAdded,
                schemaWarningsAdded, schemaErrorsResolved, schemaWarningsResolved);

        template = template.replace("{{other-errors-summary}}", spanOKFail(otherErrors.errorCount()));
        if (otherErrors.errorCount() > 0) {
            otherErrors.errorCount();
            String container = "\n<div>Errors: " + spanOKFail(otherErrors.errorCount()) + "</div>";
            container += "</p><a href=\"javascript:toggle('other_errors')\">Show/hide details</a></p>";
            container += "\n<div id='other_errors' style='display:none'>";
            container += "\n<table class='other-errors'>\n  <tr><th>Description</th><th>Details</th></tr>\n";
            for (AnalysisInformation err : otherErrors.getErrors()) {
                container += "\n  <tr><td>" + StringEscapeUtils.escapeHtml4(err.getMessage()) + "</td><td>"
                        + StringEscapeUtils.escapeHtml4(err.getDetails()) + "</td></tr>";
            }
            container += "\n</div>\n</table>\n";
            template = template.replace("{{other-errors}}", container);
        } else {
            template = template.replace("{{other-errors}}", "No errors in this category.");
        }

        if (analysisSucceeded()) {
            template = template.replace("{{result}}", "<span class='result-ok'>OK</span>");
        } else {
            template = template.replace("{{result}}", "<span class='result-failed'>Failed</span>");
        }
        template = addReportFooter(template, "{{footer}}", templates.get(FR_FOOTER_TEMPLATE), start, compare);
        logMsg("Report: " + reportFile);
        FileUtils.writeStringToFile(reportFile.toFile(), template);
    }

    String addFileList(String src, String tag, String template, String rowTemplate, List<FileSummary> summaryList,
                       Path reportLocation) {
        StringBuilder builder = new StringBuilder();
        for (FileSummary summary : summaryList) {
            String t = rowTemplate;
            String name = "<a href='" + summary.getFilePath() + "' target='_blank'>" + summary.getName();
            String report = StringEscapeUtils.escapeHtml4("<none>");
            if (summary.hasFullReportHtml()) {
                Path relLoc = reportLocation.relativize(summary.getFullReportHtml());
                report = "<a href='" + relLoc + "' target='_blank'>Report</a>";
            }
            String pretty = StringEscapeUtils.escapeHtml4("<none>");
            if (summary.hasSourceHtml()) {
                Path relLoc = reportLocation.relativize(summary.getSourceHtml());
                pretty = "<a href='" + relLoc + "' target='_blank'>Pretty</a>";
            }
            String diff = StringEscapeUtils.escapeHtml4("<none>");
            if (summary.hasDiffReportHtml()) {
                Path relLoc = reportLocation.relativize(summary.getDiffReportHtml());
                diff = "<a href='" + relLoc + "' target='_blank'>Diff</a>";
            }
            t = t.replace("{{ERR_ADDED}}", spanNeutralFail(summary.getErrorsAdded()))
                    .replace("{{ERR_RESOLVED}}", spanNeutralOK(summary.getErrorsResolved()))
                    .replace("{{WARN_ADDED}}", spanNeutralFail(summary.getWarningsAdded()))
                    .replace("{{WARN_RESOLVED}}", spanNeutralOK(summary.getWarningsResolved()))
                    .replace("{{ERR_TOTAL}}", spanOKFail(summary.getErrors().count()))
                    .replace("{{WARN_TOTAL}}", spanOKFail(summary.getWarnings().count()))
                    .replace("{{INFO_TOTAL}}", "" + summary.getInfoCount()).replace("{{name}}", name)
                    .replace("{{report}}", report).replace("{{diff}}", diff).replace("{{pretty}}", pretty);
            builder.append(t);
        }
        return src.replace(tag, template.replace("{{1}}", builder.toString()));
    }

    String addAssertionsByFile(String src, String tag, AssertionStatistics errors, AssertionStatistics warnings,
                               List<FileSummary> summaryList, Path reportLocation) {
        String t = "<h4>Assertion errors</h4>\n{{errors}}\n<h4>Assertion warnings</h4>\n{{warnings}}\n";
        String rt =  "\n<tr><td>{{1}}</td><td><a href='{{3}}' target='_blank'>{{2}}</a></td><td>{{4}}</td>"
                + "<td>{{5}}</td></tr>";
        String eRows = "";
        for (Map.Entry<String, Integer> entry : errors.getSortedByValue()) {
            eRows += "\n<tr><th colspan='3'>" + StringEscapeUtils.escapeHtml4(entry.getKey()) + "</th></tr>";
            for (FileSummary summary : summaryList) {
                int count = summary.getErrors().countByAssertion(entry.getKey());
                if (count > 0) {
                    String report = "";
                    String html = "";
                    if (summary.hasFullReportHtml()) {
                        Path relLoc = reportLocation.relativize(summary.getFullReportHtml());
                        report = "<a href='" + relLoc + "' target='_blank'>Report</a>";
                    }
                    if (summary.hasSourceHtml()) {
                        Path relLoc = reportLocation.relativize(summary.getSourceHtml());
                        html = "<a href='" + relLoc + "' target='_blank'>pretty</a>";
                    }
                    eRows += rt.replace("{{1}}", "" + count).replace("{{2}}", summary.getName())
                            .replace("{{3}}", summary.getFilePath().toString()).replace("{{4}}", html)
                            .replace("{{5}}", report);
                }
            }
        }
        if (eRows.length() == 0) {
            eRows = "<p>No errors found.</p>";
        } else {
            eRows = "<table class=''>\n" + eRows + "\n</table>";
        }
        String wRows = "";
        for (Map.Entry<String, Integer> entry : warnings.getSortedByValue()) {
            wRows += "\n<tr><th colspan='3'>" + StringEscapeUtils.escapeHtml4(entry.getKey()) + "</th></tr>";
            for (FileSummary summary : summaryList) {
                int count = summary.getWarnings().countByAssertion(entry.getKey());
                if (count > 0) {
                    String report = "";
                    String html = "";
                    if (summary.hasFullReportHtml()) {
                        Path relLoc = reportLocation.relativize(summary.getFullReportHtml());
                        report = "<a href='" + relLoc + "' target='_blank'>Report</a>";
                    }
                    if (summary.hasSourceHtml()) {
                        Path relLoc = reportLocation.relativize(summary.getSourceHtml());
                        html = "<a href='" + relLoc + "' target='_blank'>pretty</a>";
                    }
                    wRows += rt.replace("{{1}}", "" + count).replace("{{2}}", summary.getName())
                            .replace("{{3}}", summary.getFilePath().toString()).replace("{{4}}", html)
                            .replace("{{5}}", report);
                }
            }
        }
        if (wRows.length() == 0) {
            wRows  = "<p>No warnings found.</p>";
        } else {
            wRows = "<table>\n" + wRows + "\n</table>";
        }
        return src.replace(tag, t.replace("{{errors}}", eRows).replace("{{warnings}}", wRows));
    }

    String addAssertionsByCount(String src, String tag, AssertionStatistics errors, AssertionStatistics warnings) {
        String t = "\n<table>\n<tr><th>Count</th><th>Assertion errors</th></tr>\n{{errors}}\n" +
                "\n<tr><th>Count</th><th>Assertion warnings</th></tr>\n{{warnings}}\n</table>\n";
        String rt =  "\n<tr><td>{{1}}</td><td>{{2}}</td></tr>";
        String eRows = "";
        for (Map.Entry<String, Integer> entry : errors.getSortedByValue()) {
            eRows += rt.replace("{{1}}", "" + entry.getValue()).replace("{{2}}", entry.getKey());
        }
        if (eRows.length() == 0) {
            eRows += rt.replace("{{1}}", "").replace("{{2}}", "No errors.");
        }
        String wRows = "";
        for (Map.Entry<String, Integer> entry : warnings.getSortedByValue()) {
            wRows += rt.replace("{{1}}", "" + entry.getValue()).replace("{{2}}", entry.getKey());
        }
        if (wRows.length() == 0) {
            wRows += rt.replace("{{1}}", "").replace("{{2}}", "No warnings.");
        }
        return src.replace(tag, t.replace("{{errors}}", eRows).replace("{{warnings}}", wRows));
    }

    private String spanNeutralFail(int value) {
        return "<span" + (value == 0 ? ">" : " class='color-failed'>") + value + "</span>";
    }

    private String spanNeutralOK(int value) {
        return "<span" + (value == 0 ? ">" : " class='color-ok'>") + value + "</span>";
    }

    private String spanOKFail(int value) {
        return "<span class='color" + (value == 0 ? "-ok'>" : "-failed'>") + value + "</span>";
    }

    String addSummaryHtml(String src, String tag, String display, String template, int e, int w, int ea, int wa,
                          int er, int wr) {
        String t = template;
        src = src.replace(display, (e+w+ea+wa+er+wr == 0) ? "display:none" : "");
        t = t.replace("{{ERR_ADDED}}", spanNeutralFail(ea)).replace("{{ERR_RESOLVED}}", spanNeutralOK(er));
        t = t.replace("{{WARN_ADDED}}", spanNeutralFail(wa)).replace("{{WARN_RESOLVED}}", spanNeutralOK(wr));
        t = t.replace("{{ERR_TOTAL}}", spanOKFail(e)).replace("{{WARN_TOTAL}}", spanOKFail(w));
        return src.replace(tag, t);
    }

    String addAnalysisSummaryHtml(String src, String tag, String template, int we, int ww, int wea, int wwa, int wer,
                                  int wwr, int se, int sw, int sea, int swa, int ser, int swr, boolean compare) {
        String t = template;
        // WSDL table row
        t = t.replace("{{WSDL_ERR_ADDED}}", spanNeutralFail(wea))
                .replace("{{WSDL_ERR_RESOLVED}}", spanNeutralOK(wer))
                .replace("{{WSDL_WARN_ADDED}}", spanNeutralFail(wwa))
                .replace("{{WSDL_WARN_RESOLVED}}", spanNeutralOK(wwr))
                .replace("{{WSDL_ERR_TOTAL}}", spanOKFail(we))
                .replace("{{WSDL_WARN_TOTAL}}", spanOKFail(ww));
        // schema table row
        t = t.replace("{{SCHEMA_ERR_ADDED}}", spanNeutralFail(sea))
                .replace("{{SCHEMA_ERR_RESOLVED}}", spanNeutralOK(ser))
                .replace("{{SCHEMA_WARN_ADDED}}", spanNeutralFail(swa))
                .replace("{{SCHEMA_WARN_RESOLVED}}", spanNeutralOK(swr))
                .replace("{{SCHEMA_ERR_TOTAL}}", spanOKFail(se))
                .replace("{{SCHEMA_WARN_TOTAL}}", spanOKFail(sw));

        logMsg("\nWSDL errors: " + we + (compare ? " total, " + wea + " added, " + wer + " resolved." : ""));
        logMsg("WSDL warnings: " + ww + (compare ? " total, " + wwa + " added, " + wwr + " resolved." : ""));
        logMsg("Schema errors: " + se  + (compare ? " total, " + sea + " added, " + ser + " resolved." : ""));
        logMsg("Schema warnings: " + sw  + (compare ?" total, " + swa + " added, " + swr + " resolved." : ""));

        return src.replace(tag, t);
    }

    String getToolVersion() {
        String version = "Not determined";
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
        return version;
    }

    private String addReportFooter(String src, String tag, String template, long start, boolean compare) {
        long duration = System.currentTimeMillis() - start;
        long ms = duration % 1000;
        long s = duration / 1000 % 60;
        long m = duration / (60 * 1000) % 60;
        int files = wsdlSummary.size() + schemasSummary.size();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String msg = files + " schema/wsdl resources analyzed in " + m + "m " + s + "s " + ms + "ms";
        String cmp = compare ? compareCount + " schema/wsdl resources compared" : "";
        template = template.replace("{{analyzed}}", msg);
        template = template.replace("{{compare}}", cmp);
        template = template.replace("{{version}}", "" + StringEscapeUtils.escapeHtml4(getToolVersion()));
        template = template.replace("{{time}}", "" + dateFormat.format(date));
        logMsg("\n" + files + " files processed in " + m + "m " + s + "s " + ms + "ms");
        if (compare) {
            logMsg(cmp);
        }
        return src.replace(tag, template);
    }

    private void writeJsonReport(Path location, String filename, AnalysisInformationCollector collector)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        Path jsonOut = location.resolve(baseName + ".json");
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile());
    }

    private void writeHtmlSource(Path location, String html)
            throws IOException {
        Utilities.createDirs(location.getParent()); // make sure parent dirs are created
        FileUtils.writeStringToFile(location.toFile(), html);
    }

    private void writeHtmlReport(String template, String css, String sourceHtml, Path location, String filename,
                                 AnalysisInformationCollector collector)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        Path htmlOut = location.resolve(baseName + ".html");
        String htmlFragment = HtmlUtil.toHtmlTable(collector, true);
        String html = template.replace("{{title}}", StringEscapeUtils.escapeHtml4(filename))
                .replace("{{styles}}", css).replace("{{file}}", StringEscapeUtils.escapeHtml4(filename))
                .replace("{{result}}", htmlFragment).replace("{{source}}", sourceHtml);
        FileUtils.writeStringToFile(htmlOut.toFile(), html);
    }

    private void writeHtmlReport(String template, Path location, String filename, AnalysisInformationCollector added,
                                        AnalysisInformationCollector resolved, AnalysisInformationCollector all)
            throws IOException {
        Utilities.createDirs(location); // make sure parent dirs are created
        String baseName = FilenameUtils.getBaseName(filename);
        Path htmlOut = location.resolve(baseName + ".html");
        String htmlFragment = "<h2>New errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(added, true);
        htmlFragment += "<h2>Resolved errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(resolved, true);
        htmlFragment += "<h2>All errors/warnings</h2>";
        htmlFragment += HtmlUtil.toHtmlTable(all, true);
        String html = template.replace("{{title}}", StringEscapeUtils.escapeHtml4(filename))
                .replace("{{file}}", StringEscapeUtils.escapeHtml4(filename))
                .replace("{{result}}", htmlFragment);
        FileUtils.writeStringToFile(htmlOut.toFile(), html);
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
        if (invalid.size() > 0) {
            String txt = invalid.size() == 1 ? "file/directory" : "files/directories";
            logMsg("Skipping " + invalid.size() + " " + txt + ", see report for details.");
            for (Path file : invalid) {
                Path rel = rootDirectory.relativize(file);
                String err = "Invalid location";
                String ext = FilenameUtils.getExtension(file.toFile().getName());
                if (!("xsd".equalsIgnoreCase(ext) || "wsdl".equalsIgnoreCase(ext))) {
                    err = "Invalid file extension '" + ext + "'";
                }
                otherErrors.addError("", err, AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL, "Path: '" + rel + "'.");
            }
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
