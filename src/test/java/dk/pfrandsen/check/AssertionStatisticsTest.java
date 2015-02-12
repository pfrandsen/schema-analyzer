package dk.pfrandsen.check;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

public class AssertionStatisticsTest {
    static private final String ASSERTION_A1 = "A1";
    static private final String ASSERTION_A2 = "A2";
    static private final String ASSERTION_B1 = "B1";
    static private final String ASSERTION_B2 = "B2";
    static private final String ASSERTION_C1 = "C1";
    static private final String ASSERTION_C2 = "C2";

    private AssertionStatistics stats1, stats2, stats3;

    @Before
    public void setUp() {
        stats1 = new AssertionStatistics();
        stats2 = new AssertionStatistics();
        stats3 = new AssertionStatistics();

        stats1.addAssertion(ASSERTION_A1);
        stats1.addAssertion(ASSERTION_A2);
        stats1.addAssertion(ASSERTION_A2);

        stats2.addAssertion(ASSERTION_B1);
        stats2.addAssertion(ASSERTION_B1);
        stats2.addAssertion(ASSERTION_B2);

        stats3.addAssertion(ASSERTION_C2);
        stats3.addAssertion(ASSERTION_C2);
        stats3.addAssertion(ASSERTION_C2);
        stats3.addAssertion(ASSERTION_C1);
    }

    @Test
    public void testSortByValueA() {
        SortedSet<Map.Entry<String, Integer>> set = stats1.getSortedByValue();
        assertEquals(2, set.size());
        List<Map.Entry<String, Integer>> list = new ArrayList<>(set);
        assertEquals(2, (int) list.get(0).getValue());
        assertEquals(ASSERTION_A2, list.get(0).getKey());
    }

    @Test
    public void testSortByValueB() {
        SortedSet<Map.Entry<String, Integer>> set = stats2.getSortedByValue();
        assertEquals(2, set.size());
        List<Map.Entry<String, Integer>> list = new ArrayList<>(set);
        assertEquals(2, (int) list.get(0).getValue());
        assertEquals(ASSERTION_B1, list.get(0).getKey());
        assertEquals(1, (int) list.get(1).getValue());
        assertEquals(ASSERTION_B2, list.get(1).getKey());
    }

    @Test
    public void testSortByValueC() {
        SortedSet<Map.Entry<String, Integer>> set = stats3.getSortedByValue();
        assertEquals(2, set.size());
        List<Map.Entry<String, Integer>> list = new ArrayList<>(set);
        assertEquals(3, (int) list.get(0).getValue());
        assertEquals(ASSERTION_C2, list.get(0).getKey());
        assertEquals(1, (int) list.get(1).getValue());
        assertEquals(ASSERTION_C1, list.get(1).getKey());
    }

    @Test
    public void testSortByValueAll() {
        AssertionStatistics stats = new AssertionStatistics();
        stats.add(stats2);
        stats.add(stats1);
        stats.add(stats3);
        SortedSet<Map.Entry<String, Integer>> set = stats.getSortedByValue();
        assertEquals(6, set.size());
        List<Map.Entry<String, Integer>> list = new ArrayList<>(set);
        assertEquals(3, (int) list.get(0).getValue());
        assertEquals(ASSERTION_C2, list.get(0).getKey());
        assertEquals(2, (int) list.get(1).getValue());
        assertEquals(ASSERTION_A2, list.get(1).getKey());
        assertEquals(2, (int) list.get(2).getValue());
        assertEquals(ASSERTION_B1, list.get(2).getKey());
        assertEquals(1, (int) list.get(3).getValue());
        assertEquals(ASSERTION_A1, list.get(3).getKey());
        assertEquals(1, (int) list.get(4).getValue());
        assertEquals(ASSERTION_B2, list.get(4).getKey());
        assertEquals(1, (int) list.get(5).getValue());
        assertEquals(ASSERTION_C1, list.get(5).getKey());
    }

}
