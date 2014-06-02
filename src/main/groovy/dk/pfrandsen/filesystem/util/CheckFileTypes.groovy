package dk.pfrandsen.filesystem.util

import org.apache.commons.cli.Option

/**
 * This utility recursively checks a filesystem (from the given root folder), to see if all file names matches a set of
 * extensions. It prints out the full path of all file names that does not match the given extensions.
 */

class CheckFileTypes {

    private static String rootFolder
    private static String[] ignoreFolders = []
    private static String[] matchExtensions = ["wsdl", "xsd"]

    private static CliBuilder commandlineParser() {
        CliBuilder cli = new CliBuilder(usage: 'CheckFileTypes [options]')
        cli.with {
            _(longOpt: 'help', 'Show usage information')
            r(longOpt: 'root', "Path to root folder", required: true, args: 1)
            e(longOpt: 'extensions', "Valid file extensions. Default ${matchExtensions.join(',')}", args: Option.UNLIMITED_VALUES, valueSeparator: ',')
            i(longOpt: 'ignore', "Folders located in root that should be ignored.", args: Option.UNLIMITED_VALUES, valueSeparator: ',')
        }
        return cli
    }

    private static void processArguments(def options) {
        if (options.r) {
            rootFolder = options.r
        }
        if (options.e) {
            matchExtensions = options.es // 's' needs to be added to get all values and not just the first
        }
        if (options.i) {
            ignoreFolders = options.is // 's' needs to be added to get all values and not just the first
        }
    }

    private static void findEmptyRecursive(File folder) {
        folder.eachDir() { dir ->
            findEmptyRecursive(dir)
        }
        String[] files = folder.list()
        if (files != null && files.length == 0) {
            System.out.println("Empty folder ${folder.getAbsolutePath()}")
        }
    }

    private static void checkFile(File file) {
        boolean match = false
        String extension = file.getName().tokenize('.').last()
        matchExtensions.each { ext ->
            if (ext.equals(extension)) {
                match = true
            }
        }
        if (!match) {
            System.out.println("Invalid extension ${file.getAbsolutePath()}")
        }
    }

    public static void main(String[] args) {
        CliBuilder cli = commandlineParser()
        def options = cli.parse(args)
        if (!options) {
            return
        }
        if (options.help) {
            cli.usage()
            return
        }
        processArguments(options)
        File root = new File(rootFolder)
        if (!root.isDirectory()) {
            System.out.println("'${rootFolder}' is not a directory. Exiting")
            return
        }
        System.out.println("Extensions used {${matchExtensions.join(', ')}}")
        root.eachFile { file ->
            if (file.isDirectory()) {
                if (!ignoreFolders.contains(file.getName())) {
                    System.out.println("Checking folder '${file.getName()}'")
                    file.eachFileRecurse() { f ->
                        if (!f.isDirectory()) {
                            checkFile(f)
                        }
                    }
                } else {
                    System.out.println("Ignoring folder '${file.getName()}'")
                }
            } else {
                checkFile(file)
            }
        }
        findEmptyRecursive(new File(rootFolder))
    }

}