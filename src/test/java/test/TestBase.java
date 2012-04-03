package test;

import esl.system.SysLoggerUtils;

public abstract class TestBase  {

    public TestBase() {
        SysLoggerUtils.initLogger();
    }

}
