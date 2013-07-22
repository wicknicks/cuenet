package esl.cuenet.algorithms.firstk.impl.person.accessor;

import esl.cuenet.algorithms.firstk.personal.accessor.SourceFactory;
import esl.system.SysLoggerUtils;
import org.junit.Test;

public class SourceFactoryTest {

    @Test
    public void test() {
        SysLoggerUtils.initLogger();
        SourceFactory.getFactory().getSources();
    }
}
