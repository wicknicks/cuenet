package esl.cuenet.algorithms.firstk.impl.person.accessor;

import esl.cuenet.algorithms.firstk.personal.accessor.Twitter;
import esl.system.SysLoggerUtils;
import org.junit.Test;

public class TwitterSourceTester {

    static {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void test() {
        Twitter twitter = Twitter.getInstance();
        twitter.lookupHashtag("#acmturing100");
    }

}
