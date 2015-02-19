package dk.pfrandsen.file;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.commons.io.IOUtils;

public class Utf8 {
    public static String ASSERTION_ID = "UTF-8";

    private static byte[] getUTF8ByteOrderMark() {
        return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    }

    private static boolean hasUTF8ByteOrderMark(byte[] data) {
        byte[] UTF_8_BOM = getUTF8ByteOrderMark();
        if (data.length < UTF_8_BOM.length) {
            return false;
        }
        for (int idx = 0; idx < UTF_8_BOM.length; idx++) {
            if (Byte.compare(UTF_8_BOM[idx], data[idx]) != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidUTF8WithByteOrderMark(String relPath, URI uri,
                                                       AnalysisInformationCollector collector) {
        try {
            byte[] data = IOUtils.toByteArray(uri);
            if (!hasUTF8ByteOrderMark(data)) {
                return false;
            }
            try {
                int offset = getUTF8ByteOrderMark().length;
                int length = data.length - offset;
                Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(data, offset, length));
            } catch (CharacterCodingException e) {
                return false;
            }
            return true;
        } catch (IOException e) {
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading bytes from '" + relPath  + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume file is not OK
        }
    }

    public static boolean isValidUTF8(String relPath, URI uri, AnalysisInformationCollector collector) {
        try {
            byte[] data = IOUtils.toByteArray(uri);
            try {
                Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(data));
            } catch (CharacterCodingException e) {
                return false;
            }
            return true;
        } catch (IOException e) {
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading bytes from '" + relPath + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume file not OK
        }
    }

    public static boolean hasUTF8ByteOrderMark(String relPath, URI uri, AnalysisInformationCollector collector) {
        try {
            return hasUTF8ByteOrderMark(IOUtils.toByteArray(uri));
        } catch (IOException e) {
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading BOM (ByteOrderMark) from '" +
                            relPath + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume no UTF-8 BOM
        }
    }

    public static void checkUtf8File(Path rootPath, Path filePath, AnalysisInformationCollector collector) {
        String relPath = rootPath.relativize(filePath).toString();
        checkUtf8Uri(relPath, filePath.toUri(), collector);
    }

    public static void checkUtf8Uri(String relPath, URI uri, AnalysisInformationCollector collector) {
        boolean bom = hasUTF8ByteOrderMark(relPath, uri, collector);
        if (bom) {
            collector.addError(ASSERTION_ID, "UTF-8 byte order mark found in '" + relPath + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        if (bom) {
            // check if the data after the byte order mark is UTF-8
            // i.e., file/resource may both have UTF-8 BOM and contain invalid data
            if (!isValidUTF8WithByteOrderMark(relPath, uri, collector)) {
                collector.addError(ASSERTION_ID, "Resource '" + relPath
                                + "' is not recognized as UTF-8.", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                        "Resource has UTF-8 byte order mark.");
            }
        } else {
            if (!isValidUTF8(relPath, uri, collector)) {
                collector.addError(ASSERTION_ID, "Resource '" + relPath +
                        "' is not recognized as UTF-8.", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        }
    }

}