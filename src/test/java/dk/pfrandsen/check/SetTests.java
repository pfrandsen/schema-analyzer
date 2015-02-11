package dk.pfrandsen.check;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SetTests {

    private AnalysisInformationCollector collector1, collector2;

    @Before
    public void setUp() {
        collector1 = new AnalysisInformationCollector();
        collector2 = new AnalysisInformationCollector();
        collector1.addError("Assertion1", "Msg 1", AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, "Details");
        collector1.addError("Assertion2", "Msg 2", AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, "Details");
        collector1.addError("Assertion3", "Msg 3", AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, "Details");
        collector2.addError("Assertion2", "Msg 2", AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, "Details");
        collector2.addError("Assertion4", "Msg 4", AnalysisInformationCollector.SEVERITY_LEVEL_FATAL, "Details");
        collector1.addWarning("Assertion5", "Msg 5", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Details");
        collector1.addWarning("Assertion6", "Msg 6", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Details");
        collector1.addWarning("Assertion7", "Msg 7", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Details");
        collector2.addWarning("Assertion5", "Msg 5", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Details");
        collector2.addWarning("Assertion6", "Msg 6", AnalysisInformationCollector.SEVERITY_LEVEL_MAJOR, "Details");
        collector2.addInfo("Assertion8", "Msg 8", AnalysisInformationCollector.SEVERITY_LEVEL_MINOR, "Details");
    }

    @Test
    public void testExcept() {
        AnalysisInformationCollector collector = collector1.except(collector1);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testExceptA() {
        AnalysisInformationCollector collector = collector1.except(collector2);
        assertEquals(2, collector.errorCount());
        assertEquals(1, collector.warningCount());
        assertEquals(0, collector.infoCount());
        assertEquals("Assertion1", collector.getErrors().get(0).getAssertion());
        assertEquals("Assertion3", collector.getErrors().get(1).getAssertion());
        assertEquals("Assertion7", collector.getWarnings().get(0).getAssertion());
    }

    @Test
    public void testExceptB() {
        AnalysisInformationCollector collector = collector2.except(collector1);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(1, collector.infoCount());
        assertEquals("Assertion4", collector.getErrors().get(0).getAssertion());
        assertEquals("Assertion8", collector.getInfo().get(0).getAssertion());
    }

}
