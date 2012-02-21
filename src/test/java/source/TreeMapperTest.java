package source;

import esl.cuenet.source.TreeMapper;
import org.apache.log4j.Logger;
import org.junit.Test;

public class TreeMapperTest {

    private Logger logger = Logger.getLogger(TreeMapperTest.class);

    @Test
    public void simpleMapTest() {

        TreeMapper mapper = new TreeMapper();

        mapper.map("", null, null, null);
        try {
        mapper.map(null, null, null, null);
        } catch (Exception e) {
            logger.info("Caught NullPointerException: " + e.getLocalizedMessage());
        }

        mapper.map("a", null, null, null);
        mapper.map("a.b", null, null, null);
        mapper.map("a.c", null, null, null);
        mapper.map("a.b.d", null, null, null);
        mapper.map("a.b.d.e.f.g", null, null, null);
        mapper.map("a.c.h", null, null, null);
        mapper.map("a.c.d.e.i.j", null, null, null);

    }

}
