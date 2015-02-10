package dk.pfrandsen.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import dk.pfrandsen.check.AnalysisInformationCollector;

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

    public static boolean isValidUTF8WithByteOrderMark(Path rootPath, Path filePath,
                                                       AnalysisInformationCollector collector) {
        try {
            byte[] data = Files.readAllBytes(filePath);
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
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading bytes from '" +
                            rootPath.relativize(filePath) + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume file is not OK
        }
    }

    public static boolean isValidUTF8(Path rootPath, Path filePath, AnalysisInformationCollector collector) {
        try {
            byte[] data = Files.readAllBytes(filePath);
            try {
                Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(data));
            } catch (CharacterCodingException e) {
                return false;
            }
            return true;
        } catch (IOException e) {
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading bytes from '" +
                            rootPath.relativize(filePath) + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume file not OK
        }
    }

    public static boolean hasUTF8ByteOrderMark(Path rootPath, Path filePath, AnalysisInformationCollector collector) {
        try {
            return hasUTF8ByteOrderMark(Files.readAllBytes(filePath));
        } catch (IOException e) {
            collector.addWarning(ASSERTION_ID, "IOException thrown while reading BOM (ByteOrderMark) from '" +
                            rootPath.relativize(filePath) + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            return false; // assume no UTF-8 BOM
        }
    }

    public static void checkUtf8File(Path rootPath, Path filePath, AnalysisInformationCollector collector) {
        boolean bom = hasUTF8ByteOrderMark(rootPath, filePath, collector);
        if (bom) {
            collector.addError(ASSERTION_ID, "UTF-8 byte order mark found in '" + rootPath.relativize(filePath) + "'.",
                    AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        }
        if (bom) {
            // check if the data after the byte order mark is UTF-8
            // i.e., file may both have UTF-8 BOM and contain invalid data
            if (!isValidUTF8WithByteOrderMark(rootPath, filePath, collector)) {
                collector.addError(ASSERTION_ID, "File '" + rootPath.relativize(filePath) +
                                "' is not recognized as UTF-8.", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR,
                        "File has UTF-8 byte order mark.");
            }
        } else {
            if (!isValidUTF8(rootPath, filePath, collector)) {
                collector.addError(ASSERTION_ID, "File '" + rootPath.relativize(filePath) +
                                "' is not recognized as UTF-8.", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
            }
        }
    }
}