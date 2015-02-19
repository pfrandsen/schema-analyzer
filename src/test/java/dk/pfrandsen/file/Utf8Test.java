package dk.pfrandsen.file;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Utf8Test {

    private static Path RELATIVE_PATH = Paths.get("src", "test", "resources", "utf8");
    private static Path rootPath;
    private AnalysisInformationCollector collector;


    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
        rootPath = RELATIVE_PATH.toAbsolutePath();
    }

    @Test
    public void testValidFile() {
        Path path = RELATIVE_PATH.resolve("utf8.xml").toAbsolutePath();
        Utf8.checkUtf8File(rootPath, path, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testFileWithBOM() {
        Path path = RELATIVE_PATH.resolve("utf8-with-bom.xml").toAbsolutePath();
        Utf8.checkUtf8File(rootPath, path, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertTrue(collector.getErrors().get(0).getMessage().startsWith("UTF-8 byte order mark found in "));
    }

    @Test
    public void testInvalidFile() {
        final String fileName = "not-utf8.xml";
        Path path = RELATIVE_PATH.resolve(fileName).toAbsolutePath();
        Utf8.checkUtf8File(rootPath, path, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertTrue(collector.getErrors().get(0).getMessage().endsWith(" is not recognized as UTF-8."));
    }


    @Test
    public void testVerifyByteOrderMarkInFileWithUtf8BOM() {
        Path path = RELATIVE_PATH.resolve("utf8-with-bom.xml");
        assertTrue(Utf8.hasUTF8ByteOrderMark(path.toString(), path.toAbsolutePath().toUri(), collector));
    }

    @Test
    public void testUtfDateInFileWithUtf8ByteOrderMark() {
        Path path = RELATIVE_PATH.resolve("utf8-with-bom.xml");
        assertTrue(Utf8.isValidUTF8WithByteOrderMark(path.toString(), path.toAbsolutePath().toUri(), collector));
    }
}
