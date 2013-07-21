package esl.cuenet.algorithms.firstk.impl.person.accessor;

import esl.cuenet.algorithms.firstk.personal.Utils.RFC3339DateFormatter;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RFC3339Tester {

    @Test
    public void parseCalTimes() throws Exception {
        String t = "2009-07-22T12:30:00-07:00";


        RFC3339DateFormatter formatter = new RFC3339DateFormatter();
        System.out.println(formatter.parse(t));
        System.out.println(formatter.format(new Date()));
    }

}
