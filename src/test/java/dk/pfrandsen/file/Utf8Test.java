package dk.pfrandsen.file;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class Utf8Test {

    private static String RELATIVE_PATH = "src/test/resources/utf8/";
    private String path;
    private AnalysisInformationCollector collector;


    @Before
    public void setUp() {
        path = System.getProperty("user.dir") + File.separator + RELATIVE_PATH;
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValidFile() {
        assertTrue(Utf8.checkUtf8File(path + "utf8.xml", collector));
    }

    @Test
    public void testFileWithBOM() {
        assertFalse(Utf8.checkUtf8File(path + "utf8-with-bom.xml", collector));
    }

    @Test
    public void testInvalidFile() {
        assertFalse(Utf8.checkUtf8File(path + "not-utf8.xml", collector));
    }


    @Test
    public void testVerifyByteOrderMarkInFileWithUtf8BOM() {
        assertTrue(Utf8.hasUTF8ByteOrderMark(path + "utf8-with-bom.xml", collector));
    }

    @Test
    public void testUtfDateInFileWithUtf8ByteOrderMark() {
        assertTrue(Utf8.isValidUTF8WithByteOrderMark(path + "utf8-with-bom.xml", collector));
    }
}
