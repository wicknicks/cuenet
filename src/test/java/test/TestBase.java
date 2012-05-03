package test;

import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import esl.system.SysLoggerUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class TestBase  {

    public TestBase() {
        SysLoggerUtils.initLogger();
    }

    public static class TestAlgorithm extends BaseAlgorithm {
        public TestAlgorithm() throws IOException, ParseException {
            super();
        }
    }

}
