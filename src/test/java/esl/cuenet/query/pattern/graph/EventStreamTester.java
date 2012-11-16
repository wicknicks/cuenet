package esl.cuenet.query.pattern.graph;

import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class EventStreamTester {

    static {
        SysLoggerUtils.initLogger();
    }

    private Logger logger = Logger.getLogger(EventStreamTester.class);

    @Test
    public void createEventStreamToken() {
        EventStreamToken stringToken = new EventStreamToken("a");
        EventStreamToken sub = stringToken.createSubEventToken("b");
        sub = sub.createSubEventToken("c");
        sub.createSubEventToken("d");
        logger.info(stringToken);

        EventStreamToken arrayInitializedToken = new EventStreamToken("a", "b", "c", "d");
        logger.info(arrayInitializedToken);

        arrayInitializedToken = new EventStreamToken("a");
        logger.info(arrayInitializedToken);
    }

    @Test
    public void createEventStream() {
        EventStream stream = new EventStream();

        EventStreamToken stringToken = new EventStreamToken("a");
        EventStreamToken sub = stringToken.createSubEventToken("b");
        sub = sub.createSubEventToken("c");
        sub.createSubEventToken("d");
        stream.add(stringToken);

        EventStreamToken arrayInitializedToken = new EventStreamToken("a");
        stream.add(arrayInitializedToken);

        arrayInitializedToken = new EventStreamToken("a", "b", "c", "d");
        stream.add(arrayInitializedToken);

        logger.info(stream);

        stream = new EventStream();
        logger.info("Empty stream: [" + stream + "]");
    }

}
