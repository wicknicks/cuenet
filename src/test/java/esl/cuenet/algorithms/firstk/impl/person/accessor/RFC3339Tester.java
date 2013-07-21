package esl.cuenet.algorithms.firstk.impl.person.accessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import esl.cuenet.algorithms.firstk.personal.Utils.RFC3339DateFormatter;
import org.junit.Test;

import java.util.Date;

public class RFC3339Tester {

    @Test
    public void parseCalTimes() throws Exception {
        String t = "2009-07-22T12:30:00-07:00";


        RFC3339DateFormatter formatter = new RFC3339DateFormatter();
        System.out.println(formatter.parse(t));
        System.out.println(formatter.format(new Date()));
    }

    @Test
    public void testmultimap() {
        Multimap<String, String> m = HashMultimap.create();
        printMap(m);
        m.put("A", "1");
        m.put("B", "2");
        m.put("A", "3");
        printMap(m);
        m.put("A", "4");
        m.put("A", "5");
        printMap(m);

        m.put("A", "4");
        m.put("A", "5");
        printMap(m);
    }

    void printMap(Multimap<String, String> m) {
        for (String key: m.keySet()) {
            System.out.println(key + " " + m.get(key).toString());
        }
    }

}
