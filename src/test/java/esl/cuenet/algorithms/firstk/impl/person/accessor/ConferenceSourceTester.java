package esl.cuenet.algorithms.firstk.impl.person.accessor;

import esl.cuenet.algorithms.firstk.personal.accessor.Conference;
import esl.system.SysLoggerUtils;
import org.junit.Test;

public class ConferenceSourceTester {

    static {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void test() {

        Conference.getInstance();

    }

}
