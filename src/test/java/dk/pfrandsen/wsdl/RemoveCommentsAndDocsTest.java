package dk.pfrandsen.wsdl;

import dk.pfrandsen.util.Utilities;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class RemoveCommentsAndDocsTest {
    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "wsdl", "remove");

    private static String removeWhitespace(String txt) {
        return txt.replaceAll("[\\t\\n\\r]"," ").replaceAll("\\s+", "").trim();
    }

    @Test
    public void testRemoveComments() throws Exception {
        Path path = RELATIVE_PATH.resolve("CommentsAndDocs.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()), StandardCharsets.UTF_8);
        Path noCommentsPath = RELATIVE_PATH.resolve("NoComments.wsdl");
        String noCommentsWsdl = IOUtils.toString(new FileInputStream(noCommentsPath.toFile()), StandardCharsets.UTF_8);
        String noComment = Utilities.removeXmlComments(wsdl);
        assertEquals(removeWhitespace(noComment), removeWhitespace(noCommentsWsdl));
    }

    @Test
    public void testRemoveDocumentation() throws Exception {
        Path path = RELATIVE_PATH.resolve("CommentsAndDocs.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()), StandardCharsets.UTF_8);
        Path noDocsPath = RELATIVE_PATH.resolve("NoDocs.wsdl");
        String noDocsWsdl = IOUtils.toString(new FileInputStream(noDocsPath.toFile()), StandardCharsets.UTF_8);
        String noDocs = Utilities.removeXmlDocumentation(wsdl);
        assertEquals(removeWhitespace(noDocs), removeWhitespace(noDocsWsdl));
    }

    @Test
    public void testRemoveCommentsAndDocumentation() throws Exception {
        Path path = RELATIVE_PATH.resolve("CommentsAndDocs.wsdl");
        String wsdl = IOUtils.toString(new FileInputStream(path.toFile()), StandardCharsets.UTF_8);
        Path noCommentsOrDocsPath = RELATIVE_PATH.resolve("NoCommentsOrDocs.wsdl");
        String noCommentsDocsWsdl = IOUtils.toString(new FileInputStream(noCommentsOrDocsPath.toFile()),
                StandardCharsets.UTF_8);
        String noCommentsOrDocs = Utilities.removeXmlDocumentation(Utilities.removeXmlComments(wsdl));
        assertEquals(removeWhitespace(noCommentsOrDocs), removeWhitespace(noCommentsDocsWsdl));
    }
}
