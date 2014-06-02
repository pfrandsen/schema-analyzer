package dk.pfrandsen.filesystem.util

import org.apache.commons.cli.Option

/**
 * This utility recursively checks a filesystem (from the given root folder), to find empty folders. The full
 * path to the empty folders are printed to <stdout>.
 */

class CheckEmptyFolders {

    private static String rootFolder

    private static CliBuilder commandlineParser() {
        CliBuilder cli = new CliBuilder(usage: 'CheckFileTypes [options]')
        cli.with {
            _(longOpt: 'help', 'Show usage information')
            r(longOpt: 'root', "Path to root folder", required: true, args: 1)
        }
        return cli
    }

    private static void processArguments(def options) {
        if (options.r) {
            rootFolder = options.r
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
            System.err.println("'${rootFolder}' is not a directory. Exiting")
            return
        }
        findEmptyRecursive(new File(rootFolder))
    }

}