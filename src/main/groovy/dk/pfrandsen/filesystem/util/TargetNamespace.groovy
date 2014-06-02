package dk.pfrandsen.filesystem.util

 import org.apache.commons.cli.Option

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utility to check target namespace in files against the location of the file. The expected namespace value is
 * "http://" + <domain> + <relative path from root folder> where domain and root folder are given as input.
 * The script will recursively check all the files that matches a given set of extensions (e.g., .xsd) and
 * print error messages if: The file does not include a target namespace, if it has more than one namespace, or
 * if the namespace does not match the location of the file.
 */

class TargetNamespace {

    private static String rootFolder
    private static String[] extensions = ["xsd", "wsdl"]
    private static String domain = ""


    private static CliBuilder commandlineParser() {
        CliBuilder cli = new CliBuilder(usage: 'CheckFileTypes [options]')
        cli.with {
            _(longOpt: 'help', 'Show usage information')
            r(longOpt: 'root', "Path to root folder", required: true, args: 1)
            d(longOpt: 'domain', "Domain for namespace", required: true, args: 1)
            e(longOpt: 'extensions', "File extensions to check. Default ${extensions.join(',')}",
                    args: Option.UNLIMITED_VALUES, valueSeparator: ',')
        }
        return cli
    }

    private static void processArguments(def options) {
        if (options.r) {
            rootFolder = options.r
        }
        if (options.d) {
            domain = options.d
        }
        if (options.e) {
            extensions = options.es // 's' needs to be added to get all values and not just the first
        }
    }

    private static void checkNamespace(File file, String domain, String prefix) {
        String absolutePath = file.getAbsolutePath()
        String ns = "http://" + domain + file.parentFile.getAbsolutePath()[prefix.length()..-1].replace('\\', '/')
        String fileContents = file.text
        Pattern regExp = ~/(?s).*targetNamespace\s*=\s*"([^"]*)".*/
        Matcher matcher = ( fileContents =~ regExp )
        if (matcher.matches()) {
            if (matcher.getCount() > 1) {
                println("Found more than one targetNamespace (${matcher.getCount()} in '${absolutePath}'")
            }
            if (!ns.equals(matcher[0][1])) {
                println("targetNamespace '${matcher[0][1]}' in '${absolutePath}', expected '${ns}'")
            }
        } else {
            println("targetNamespace not found in '${absolutePath}'")
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

        root.eachFileRecurse() { file ->
            if (!file.isDirectory()) {
                boolean match = false
                String extension = file.getName().tokenize('.').last()
                extensions.each { ext ->
                    if (ext.equals(extension)) {
                        match = true
                    }
                }
                if (match) {
                    checkNamespace(file, domain, rootFolder)
                }
            }
        }
    }

}