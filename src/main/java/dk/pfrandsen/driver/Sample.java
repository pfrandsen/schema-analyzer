package dk.pfrandsen.driver;


import com.fasterxml.jackson.jr.ob.JSON;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.file.Utf8;
import dk.pfrandsen.util.Utilities;
import dk.pfrandsen.xsd.SchemaChecker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/*

simpletype http://simpletype.schemas.nykreditnet.net/
technical http://technical.schemas.nykreditnet.net/fault/v1
concept http://concept.schemas.nykreditnet.net/bank/account/currency/v1
service http://service.schemas.nykreditnet.net/
process http://process.schemas.nykreditnet.net/

Skip external

 */

public class Sample {

    private static class Visitor extends SimpleFileVisitor {

    }

    static int errorCount, warningCount, infoCount;
    static int xsd, wsdl, other;
    static List<String> includeDirs = Arrays.asList("concept", "service", "technical", "simpletype", "process");
    static AnalysisInformationCollector errors = new AnalysisInformationCollector();

    private static void createDirs(Path path) {
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
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
                    Path fileRelPath = srcRoot.relativize(filePath);
                    Path target = targetRoot.resolve(fileRelPath);
                    System.out.println(target);
                    createDirs(target.getParent());
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
                            checkSchema(fileContents, collector, f.getName(), fileRelPath);
                            xsd++;
                        } else if ("wsdl".equalsIgnoreCase(ext)) {
                            wsdl++;
                        }
                    }
                    errorCount += collector.errorCount();
                    warningCount += collector.warningCount();
                    infoCount += collector.infoCount();
                    System.out.println("e " + collector.errorCount() + " w " + collector.warningCount() + " i "
                            + collector.infoCount());
                    String out = FilenameUtils.getBaseName(f.getName()) + ".json";
                    Path jsonOut = target.getParent().resolve(out);
                    System.out.println(jsonOut);
                    JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).write(collector, jsonOut.toFile()); //new File(""));
                } else {
                    other++;
                    System.out.println("File with invalid extension: '" + file + "'");
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
        System.out.println(xsd + " files processed in " + minutes + "m " + seconds + "s " + millis + "ms");
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
        //path = ;
        //int xsd = 0;
        //int wsdl = 0;
        //int other = 0;
        runChecks(root, outPath);

/*
        // Iterator<File> FileUtils .iterateFiles(path.toFile(), null, true);
        Iterator<File> it = FileUtils.iterateFiles(root.toFile(), null, true);
        while (it.hasNext()) {
            File file = it.next();
            Path filePath = Paths.get(file.getAbsolutePath());
            Path fileRelPath = root.relativize(filePath);
            //file.getName();
            //System.out.print(filePath.getFileName().getNameCount() + " ");
            String ext = FilenameUtils.getExtension(file.getName());
            String basename = FilenameUtils.getBaseName(file.getName());
            Path relPath = fileRelPath.getParent();

            if ("xsd".equalsIgnoreCase(ext)) {
                xsd++;
            } else if ("wsdl".equalsIgnoreCase(ext)) {
                wsdl++;
            } else {
                other++;
                unknown.add(fileRelPath.toString());
            }

            AnalysisInformationCollector collector = new AnalysisInformationCollector();
            try {
                FileInputStream is = new FileInputStream(file);
                String fileContents = IOUtils.toString(is);
                is.close();
                Utf8.checkUtf8File(filePath, collector);
                if (Utilities.hasUtf8Bom(fileContents)) {
                    fileContents = Utilities.removeUtf8Bom(fileContents);
                }

                if ("xsd".equalsIgnoreCase(ext) && xsd < 5) {
                    System.out.print("File " + xsd + " (" + file.getName() + ") ");
                    checkSchema(fileContents, collector, file.getName(), fileRelPath);
                    System.out.println("e " + collector.errorCount() + " w " + collector.warningCount() + " i "
                            + collector.infoCount());
                    //if (xsd == 1) {
                        System.out.println(fileRelPath.toString());
                        for (AnalysisInformation info : collector.getErrors()) {
                            System.out.println("E " + info.getMessage() + " - " + info.getDetails());
                        }
                        for (AnalysisInformation info : collector.getWarnings()) {
                            System.out.println("W " + info.getMessage() + " - " + info.getDetails());
                        }
                    //}
                } else if ("wsdl".equalsIgnoreCase(ext)) {
                    ;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Utf8.checkUtf8File()



            // System.out.println(ext + " " + basename + " " + relPath + " " + Utilities.pathToNamespace(relPath) + " " + fileRelPath);
        }
        System.out.println("xsd: " + xsd);
        System.out.println("wsdl: " + wsdl);
        System.out.println("unknown: " + other);
        for (String u : unknown) {
            System.out.println(u);
        }
        long duration = System.currentTimeMillis() - start;
        long seconds = duration / 1000 % 60;
        long minutes = duration / (60 * 1000) % 60;
        System.out.println(xsd + " files processed in " + minutes + " minutes " + seconds + " seconds");
        */
    }

    private static void checkSchema(String schema, AnalysisInformationCollector collector, String fileName,
                                    Path relPath) {
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

        // file and path checks
        SchemaChecker.checkSchemaFilename(fileName, collector);
        SchemaChecker.checkEnterpriseConceptNamespace(schema, fileName, collector);
        SchemaChecker.checkServiceConceptNamespace(schema, fileName, collector);
        //SchemaChecker.checkPathAndTargetNamespace(schema, relPath, collector);
    }

}
