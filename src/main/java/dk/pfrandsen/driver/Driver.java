package dk.pfrandsen.driver;

import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.HtmlUtil;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.util.WsiUtil;
import dk.pfrandsen.wsdl.DocumentationChecker;
import dk.pfrandsen.xsd.SchemaChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

public class Driver {

    static private Path toolJar, toolRoot;
    static int errorCount, warningCount, infoCount;
    static int xsd, wsdl, other;
    static List<String> includeDirs = Arrays.asList("service");
    private static boolean chatty;
    // error related to running the tool
    static AnalysisInformationCollector errors = new AnalysisInformationCollector();

    // args wsiJar
    //      outputDir
    //      sourceDir
    //      copySrc
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Path relTargetSrc = Paths.get("src");
        Path relResult = Paths.get("result");
        Path relResultSchema = relResult.resolve("schema");
        Path relResultWsdl = relResult.resolve("wsdl");
        Path relResultWsi = relResult.resolve("wsi");
        chatty = true;
        boolean copySourceFiles = true;

        // check args (source root, target root)
        // target/check.zip must not exits
        // target/root must not exits
        // target/check.html must not exist
        // well, target folder must be empty
        //Path relativePath = Paths.get("tmp", "schemaroot");
        Path outPath = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("out");
        if (outPath.toFile().exists()) {
            System.err.println("Fatal error: Output directory already exists, " + outPath);
            return;
        }
        // extract wsi-tool
        toolJar = Paths.get("lib", "wsi-checker-1.0-SNAPSHOT.jar");
        toolRoot = outPath.resolve("wsi-tool");
        boolean unpacked = WsiUtil.unpackCheckerTool(toolJar, toolRoot);
        if (!unpacked) {
            System.err.println("Fatal error: Could not unpack WS-I tool.");
            return;
        }
        List<Path> schema = new ArrayList<>();
        List<Path> wsdl = new ArrayList<>();
        List<Path> ignore = new ArrayList<>();
        //Path  root = Paths.get(System.getProperty("user.home")).resolve(relativePath);
        Path  root = Paths.get(System.getProperty("user.home")).resolve("tmp").resolve("0902");

        try {
            List<Path> topLevelDirs = new ArrayList<>();
            logMsg("Collecting top-level folders...");
            getRootDirectories(root, topLevelDirs, ignore, includeDirs);
            logMsg("Collecting source files...");
            for (Path dir: topLevelDirs) {
                getFiles(dir, schema, wsdl, ignore);
            }
            logMsg("\nFound: " + schema.size() + " schemas, " + wsdl.size() + " wsdls");
            collectIgnoredErrors(ignore, root);
            if (copySourceFiles) {
                copySource(root, outPath.resolve(relTargetSrc), schema, wsdl);
            }
            logMsg("Analyzing " + schema.size() + " schemas");
            analyzeSchemas(root, outPath.resolve(relResultSchema), schema);
            logMsg("Analyzing " + wsdl.size() + " wsdls");
            analyzeWsdls(root, outPath.resolve(relResultWsdl), outPath.resolve(relResultWsi), wsdl);

            // now generate diff and collect error overview (maybe this should be part of analysis...


        } catch (IOException e) {
            logMsg("Exception while processing files, " +  e.getMessage());
            errors.addError("", "Exception while processing files",
                    AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, e.getMessage());
        }
        // runChecks2(root, outPath);
        printStats(start);
    }

    private static void printStats(long start) {
        long duration = System.currentTimeMillis() - start;
        long millis = duration % 1000;
        long seconds = duration / 1000 % 60;
        long minutes = duration / (60 * 1000) % 60;
        System.out.println((xsd+wsdl) + " files processed in " + minutes + "m " + seconds + "s " + millis + "ms");
        System.out.println(errorCount + " errors, " + warningCount + " warnings, and " + infoCount + " info");
    }


    private static void analyzeSchemas(Path root, Path xsdTarget, List<Path> schema) throws IOException {
        for (Path file : schema) {
            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            Path relPath = root.relativize(file);
            Path topLevel = relPath.subpath(0, 1); // top level is logically equal to domain (prefix of domain)
            Path logicalPath = topLevel.relativize(relPath).getParent(); // remove top level and filename
            Path outputDir = xsdTarget.resolve(relPath).getParent();
            Utilities.createDirs(outputDir);
            Utf8.checkUtf8File(root, file, collector);
            String fileContents = getContentWithoutUtf8Bom(file);
            checkSchema(fileContents, collector, file.toFile().getName(), logicalPath, dirToNamespace(topLevel));
            writeReport(outputDir, file.toFile().getName(), collector);
            //errorCount += collector.errorCount();
            //warningCount += collector.warningCount();
            //infoCount += collector.infoCount();

            // load json file to compare with
        }
    }

    private static void analyzeWsdls(Path root, Path wsdlTarget,Path wsiTarget,  List<Path> schema) {


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

    private static void writeReport(Path location, String filename, AnalysisInformationCollector collector)
            throws IOException {
        System.out.println("e " + collector.errorCount() + " w " + collector.warningCount() + " i "
                + collector.infoCount());
        String baseName = FilenameUtils.getBaseName(filename);
        String ext = FilenameUtils.getExtension(filename);
        Path jsonOut = location.resolve(baseName + ".json");
        Path htmlOut = location.resolve(baseName + ".html");
        JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile());
        FileUtils.writeStringToFile(htmlOut.toFile(), HtmlUtil.toHtml(collector, false, true, filename, ext));
    }


    private static String getContentWithoutUtf8Bom(Path file) throws IOException {
        try (FileInputStream is = new FileInputStream(file.toFile())) {
            String fileContents = IOUtils.toString(is);
            if (Utilities.hasUtf8Bom(fileContents)) {
                // some libs (e.g., SAX parser) do not like the BOM and will throw exception if present
                fileContents = Utilities.removeUtf8Bom(fileContents);
            }
            return fileContents;
        }
    }

    private static String dirToNamespace(Path path) {
        //if (path.toFile().isDirectory()) {
            String name = path.toFile().getName();
            if (includeDirs.contains(name)) {
                return name + ".schemas.nykreditnet.net";
            }
            return name;
        //}
        //return "";
    }

    private static void copySource(Path root, Path srcTarget, List<Path> schema, List<Path> wsdl) throws IOException {
        logMsg("Copying schemas");
        for (Path src : schema) {
            copy(src, srcTarget.resolve(root.relativize(src)));
        }
        logMsg("Copying wsdls");
        for (Path src : wsdl) {
            copy(src, srcTarget.resolve(root.relativize(src)));
        }
    }

    private static void copy(Path from, Path to) throws IOException {
        Utilities.createDirs(to.getParent());
        Files.copy(from, to);
    }

    private static void getRootDirectories(Path rootDirectory, List<Path> include, List<Path> ignored,
                                           List<String> filter) throws IOException {
        // iterate top level directories and find the ones to include
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectory)) {
            for (Path entry : stream) {
                if (entry.toFile().isDirectory()) {
                    if (filter.contains(entry.toFile().getName())) {
                        include.add(entry);
                    } else {
                        ignored.add(entry);
                    }
                } else {
                    ignored.add(entry); // ignoring files found in root directory
                }
            }
        }
    }

    private static void getFiles(final Path rootDirectory, final List<Path> schema, final List<Path> wsdl,
                                 final List<Path> ignored) throws IOException {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                String ext = FilenameUtils.getExtension(f.getName());
                Path filePath = Paths.get(f.getAbsolutePath());
                logMsg(".", false);
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

    private static void collectIgnoredErrors(List<Path> invalid, Path rootDirectory) {
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

    private static void logMsg(String msg) {
        logMsg(msg, true);
    }

    private static void logMsg(String msg, boolean newline) {
        if (chatty) {
            if (newline) {
                System.out.println(msg);
            } else {
                System.out.print(msg);
            }
        }
    }

}
