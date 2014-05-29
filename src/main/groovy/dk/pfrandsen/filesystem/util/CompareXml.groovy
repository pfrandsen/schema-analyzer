package dk.pfrandsen.filesystem.util

import groovy.util.logging.*
import groovy.xml.XmlUtil
// import org.apache.commons.cli.Option

import java.util.logging.Level

@Log
class CompareXml {

    private int filesCompared = 0
    private int filesIgnored = 0
    private List<String> contentDiffer = new ArrayList<String>()
    private List<String> spacingDiffer = new ArrayList<String>()

    int getFilesCompared() {
        return filesCompared
    }

    int getFilesIgnored() {
        return filesIgnored
    }

    List<String> getContentDiffer() {
        return contentDiffer
    }

    List<String> getSpacingDiffer() {
        return spacingDiffer
    }

    private void compareFiles(File master, File clone) {
        String masterContents = master.text
        String cloneContents = clone.text
        if (!masterContents.equals(cloneContents)) {
            if (XmlUtil.serialize(masterContents).equals(XmlUtil.serialize(cloneContents))) {
                spacingDiffer.add(master.getCanonicalPath())
            } else {
                contentDiffer.add(master.getCanonicalPath())
            }
        }
    }

    public void compare(String mastersRoot, String clonesRoot, String[] fileExtensions) {
        File master = new File(mastersRoot)
        File clone = new File(clonesRoot)
        if (!master.isDirectory() || !clone.isDirectory()) {
            if (!master.isDirectory()) {
                log.log(Level.SEVERE, "Master root folder '${mastersRoot}' does not exist or is not a directory.")
            }
            if (!clone.isDirectory()) {
                log.log(Level.SEVERE, "Clone root folder '${clonesRoot}' does not exist or is not a directory.")
            }
            return
        }
        int masterLength = mastersRoot.length()
        master.eachFileRecurse() { file ->
            String masterPath = file.getPath()
            String clonePath = clonesRoot + masterPath.substring(masterLength)
            if (file.isFile()) {
                File cloneFile = new File(clonePath)
                if (cloneFile.exists()) {
                    String extension = file.getName().tokenize('.').last()
                    boolean match = false
                    fileExtensions.each { ext ->
                        if (ext.equals(extension)) {
                            match = true
                        }
                    }
                    if (match) {
                        filesCompared++
                        compareFiles(file, cloneFile)
                    } else {
                        filesIgnored++
                    }
                }
            }
        }
    }

    private static CliBuilder commandlineParser() {
        CliBuilder cli = new CliBuilder(usage: 'CompareXml [options]')
        cli.with {
            _(longOpt: 'help', 'Show usage information')
            m(longOpt: 'master', "Path to master root", required: true, args: 1)
            c(longOpt: 'clone', "Path to clone root", required: true, args: 1)
            // e(longOpt: 'extensions', "Extension of files to check", required: true, args: Option.UNLIMITED_VALUES, valueSeparator: ',')
            e(longOpt: 'extensions', "Extension of files to check", required: true, args: -2, valueSeparator: ',')
        }
        return cli
    }

    public static void main(String[] args) {
        CliBuilder cli = commandlineParser()
        def options = cli.parse(args)
        if (!options) {
            return
        }
        if(options.help) {
            cli.usage()
            return
        }
        CompareXml runner = new CompareXml()
        String master = options.m
        String clone = options.c
        String[] ext = options.es
        runner.compare(master, clone, ext)
        println("Master: ${master}")
        println("Clone: ${clone}")
        println("File extensions: ${ext.join(',')}")
        println("File statistics (numbers): Compared ${runner.getFilesCompared()}, ignored ${runner.getFilesIgnored()}")
        int dif = runner.getContentDiffer().size()
        int space = runner.getSpacingDiffer().size()
        println("File statistics (content): Content differ in ${dif} file${dif == 1 ? '' : 's'}, spacing differ in ${space}  file${space == 1 ? '' : 's'}")
        if (runner.getSpacingDiffer().size() > 0) {
            println("Spacing differ in:")
            runner.getSpacingDiffer().sort().each {println(it)}
        }
        if (runner.getContentDiffer().size() > 0) {
            println("Content differ in:")
            runner.getContentDiffer().sort().each {println(it)}
        }
    }
}
