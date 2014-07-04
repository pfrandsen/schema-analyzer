package dk.pfrandsen.fiilesystem.util;

import dk.pfrandsen.filesystem.util.FindAllOperations;
import dk.pfrandsen.util.Utilities;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FindAllOperationsTest {
    private static Path path = Paths.get("src", "test", "resources", "operations");

    @Test
    public void testOperations() {
        String[] expectedOp = {"updateEntity", "getEntity", "listEntity", "mergeEntity", "splitEntity"};
        String[] expectedPre = {"update", "get", "list", "merge", "split"};
        String[] expectedObj = {"Entity"};
        List<String> expectedOperations = Arrays.asList(expectedOp);
        Collections.sort(expectedOperations);
        List<String> expectedPrefixes = Arrays.asList(expectedPre);
        Collections.sort(expectedPrefixes);
        List<String> expectedObjects = Arrays.asList(expectedObj);
        Collections.sort(expectedObjects);
        FindAllOperations operations = new FindAllOperations();
        operations.findOperations(path.toFile());
        assertEquals(5, operations.getOperations().size());
        assertEquals(5, operations.getOperationsPrefix().size());
        assertEquals(1, operations.getObjects().size());
        assertTrue("Expected [" + Utilities.join(", ", expectedOperations) + "], found [" +
                        Utilities.join(", ", operations.getOperationsAsSortedList()) + "]",
                operations.getOperations().containsAll(expectedOperations));
        assertTrue("Expected [" + Utilities.join(", ", expectedPrefixes) + "], found [" +
                        Utilities.join(", ", operations.getOperationsPrefixAsSortedList()) + "]",
                operations.getOperationsPrefix().containsAll(expectedPrefixes));
        assertTrue("Expected [" + Utilities.join(", ", expectedObjects) + "], found [" +
                        Utilities.join(", ", operations.getObjectsAsSortedList()) + "]",
                operations.getObjectsAsSortedList().containsAll(expectedObjects));
    }

    /*
    // this test is used to get all the known prefixes
    @Test
    public void testGetOperations() {
        Path path = Paths.get("..", "schemaroot", "service");
        FindAllOperations operations = new FindAllOperations();
        operations.findOperations(path.toFile());
        System.out.println("Checked " + operations.getWsdlCount() + " wsdl's");
        System.out.println("Unique operation names: " + operations.getOperations().size());
        System.out.println("Unique operation prefixes: " + operations.getOperationsPrefix().size());
        System.out.println("String [] operations = {\"" +
                Utilities.join("\", \"", operations.getOperationsAsSortedList()) + "\"};");
        System.out.println("String [] prefixes = {\"" +
                Utilities.join("\", \"", operations.getOperationsPrefixAsSortedList()) + "\"};");
        System.out.println("String [] objects = {\"" + Utilities.join("\", \"", operations.getObjectsAsSortedList()) +
                "\"};");
    }
    */

}
