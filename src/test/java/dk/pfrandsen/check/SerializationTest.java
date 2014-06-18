package dk.pfrandsen.check;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest {

    private static String getLocation(String dataFile) throws URISyntaxException {
        return SerializationTest.class.getResource(dataFile).toURI().getPath();
    }

    @Test
    public void testSerialize() throws IOException {
        AnalysisInformationCollector collector = new AnalysisInformationCollector();
        collector.addError("Assertion id 1", "Error message 1", AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
        collector.addError("Assertion id 2", "Error message 2", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR);
        String json = JSON.std.asString(collector);
        assertTrue(json.startsWith("{\"errors\":[{\"assertion\":\"Assertion id 1\""));
        assertTrue(json.endsWith("\"info\":[],\"warnings\":[]}"));
        assertTrue(json.contains(",{\"assertion\":\"Assertion id 2\",\"details\":\"\",\"message\"" +
                ":\"Error message 2\",\"severity\":2}],"));
    }

    @Test
    public void testCollectorParser() throws IOException {
        String json = "{\"errors\":" +
                "[{\"assertion\":\"Assertion id 1\",\"details\":\"\",\"message\":\"Error message 1\",\"severity\":1}," +
                "{\"assertion\":\"Assertion id 2\",\"details\":\"\",\"message\":\"Error message 2\",\"severity\":2}]," +
                "\"info\":[],\"warnings\":[]}";
        AnalysisInformationCollector collector = JSON.std.beanFrom(AnalysisInformationCollector.class, json);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getErrors().get(1).getSeverity());
    }

    @Test
    public void testCollectorParseFromFile() throws IOException, URISyntaxException {
        String jsonFile = getLocation("/json/serialized.json");
        AnalysisInformationCollector collector = JSON.std.beanFrom(AnalysisInformationCollector.class,
                new FileInputStream(Paths.get(jsonFile).toFile()));
        assertEquals(5, collector.errorCount());
        assertEquals(2, collector.warningCount());
        assertEquals(1, collector.infoCount());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN, collector.getWarnings().get(0).getSeverity());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, collector.getWarnings().get(1).getSeverity());
        assertEquals("Info message 1", collector.getInfo().get(0).getMessage());
    }

    @Test
    public void testCollectorParseEmptyFromFile() throws IOException, URISyntaxException {
        String jsonFile = getLocation("/json/empty.json");
        AnalysisInformationCollector collector = JSON.std.beanFrom(AnalysisInformationCollector.class,
                new FileInputStream(Paths.get(jsonFile).toFile()));
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testSimple() throws IOException {
        AnalysisInformation info = new AnalysisInformation("Assertion id", "Message",
                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR);
        String json = JSON.std.asString(info);
        assertEquals("{\"assertion\":\"Assertion id\",\"details\":\"\",\"message\":\"Message\",\"severity\":3}", json);
    }

    @Test
    public void testSimpleDetails() throws IOException {
        AnalysisInformation info = new AnalysisInformation("id", "msg",
                AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "detail msg");
        String json = JSON.std.without(JSON.Feature.WRITE_NULL_PROPERTIES).asString(info);
        assertEquals("{\"assertion\":\"id\",\"details\":\"detail msg\",\"message\":\"msg\",\"severity\":3}", json);
    }

    @Test
    public void testParseSimple() throws IOException {
        String json = "{\"assertion\":\"Assertion id\",\"details\":\"dtl\",\"message\":\"Message\",\"severity\":3}";
        AnalysisInformation info = JSON.std.beanFrom(AnalysisInformation.class, json);
        assertEquals("Assertion id", info.getAssertion());
        assertEquals("Message", info.getMessage());
        assertEquals(AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, info.getSeverity());
        assertEquals("dtl", info.getDetails());
    }

}
