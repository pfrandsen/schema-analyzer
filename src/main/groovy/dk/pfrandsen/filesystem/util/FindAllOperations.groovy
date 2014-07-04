package dk.pfrandsen.filesystem.util

import dk.pfrandsen.util.Utilities
import dk.pfrandsen.util.XQuery
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.nio.file.Path
import java.nio.file.Paths

class FindAllOperations {

    private Set<String> operations = new HashSet<>()
    private Set<String> operationsPrefix = new HashSet<>() // action
    private Set<String> objects = new HashSet<>() // action
    int wsdlCount = 0;

    public Set<String> getOperations() {
        return Collections.unmodifiableSet(operations)
    }

    public Set<String> getOperationsPrefix() {
        return Collections.unmodifiableSet(operationsPrefix)
    }

    public Set<String> getObjects() {
        return Collections.unmodifiableSet(objects)
    }

    public int getWsdlCount() {
        return wsdlCount
    }

    private List<String> setAsSortedList(Set<String> set) {
        List<String> lst = new ArrayList<>();
        for (String item : set) {
            lst.add(item)
        }
        Collections.sort(lst)
        return lst
    }

    public List<String> getOperationsAsSortedList() {
        return setAsSortedList(operations)
    }

    public List<String> getOperationsPrefixAsSortedList() {
        return setAsSortedList(operationsPrefix);
    }

    public List<String> getObjectsAsSortedList() {
        return setAsSortedList(objects)
    }

    public void findOperations(File rootFolder) {
        if (!rootFolder.isDirectory()) {
            System.err.println("'${rootFolder}' is not a directory.")
            return
        }

        rootFolder.eachFile { file ->
            if (file.isDirectory()) {
                file.eachFileRecurse() { f ->
                    if (!f.isDirectory()) {
                        checkFile(f)
                    }
                }
            } else {
                checkFile(file)
            }

        }
    }

    private void checkFile(File file) {
        String extension = file.getName().tokenize('.').last()
        if (extension.toLowerCase().equals("wsdl")) {
            wsdlCount++
            String wsdl = IOUtils.toString(new FileInputStream(file))
            if (Utilities.hasUtf8Bom(wsdl)) {
                // System.out.println("BOM found in ${file.getAbsolutePath()}")
                wsdl = Utilities.removeUtf8Bom(wsdl)
            }
            Path xqPortType = Paths.get("wsdl", "operation")
            String xqPortTypeOperations = XQuery.runXQuery(xqPortType, "allOperations.xq", wsdl)
            List<String> operationNames = XQuery.mapResult(xqPortTypeOperations, "name")
            for (String operationName : operationNames) {
                operations.add(operationName)
                List<String> elements = Utilities.splitOnUppercase(operationName)
                operationsPrefix.add(elements.get(0))
                objects.add(operationName.substring(elements.get(0).length()))
                if (elements.size() < 2) {
                    // System.out.println("Single word operation ${operationName} in ${file.getAbsolutePath()}")
                }
            }
        }
    }

}
