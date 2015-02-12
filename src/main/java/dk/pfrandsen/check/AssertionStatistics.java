package dk.pfrandsen.check;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class AssertionStatistics {
    class Comp implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> assertion1, Map.Entry<String, Integer> assertion2) {
            if (assertion1.getValue().longValue() == assertion2.getValue()) {
                return assertion1.getKey().compareTo(assertion2.getKey());
            }
            return assertion1.getValue() < assertion2.getValue() ? 1 : -1;
        }

    }

    private TreeMap<String, Integer> assertionCollection = new TreeMap<>();

    public void addAssertion(String assertion) {
        if (assertionCollection.containsKey(assertion)) {
            assertionCollection.put(assertion, assertionCollection.get(assertion) + 1);
        } else {
            assertionCollection.put(assertion, 1);
        }
    }

    public void add(List<AnalysisInformation> assertions) {
        for (AnalysisInformation info : assertions) {
            addAssertion(info.getAssertion());
        }
    }

    public void add(AssertionStatistics stats) {
        for (String key : stats.assertionCollection.keySet()) {
            if (assertionCollection.containsKey(key)) {
                assertionCollection.put(key, assertionCollection.get(key) + stats.assertionCollection.get(key));
            } else {
                assertionCollection.put(key, stats.assertionCollection.get(key));
            }
        }
    }

    public SortedSet<Map.Entry<String, Integer>> getSortedByValue() {
        SortedSet<Map.Entry<String, Integer>> set = new TreeSet<>(new Comp());
        for (String key : assertionCollection.keySet()) {
            set.add(new AbstractMap.SimpleEntry<>(key, assertionCollection.get(key)));
        }
        return set;
    }

    public int count() {
        int sum = 0;
        for (int val : assertionCollection.values()) {
            sum += val;
        }
        return sum;
    }

    public int uniqueAssertions() {
        return assertionCollection.size();
    }

    public int countByAssertion(String assertion) {
        return assertionCollection.containsKey(assertion) ? assertionCollection.get(assertion) : 0;
    }

}
