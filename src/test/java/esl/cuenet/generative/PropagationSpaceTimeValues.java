package esl.cuenet.generative;

import esl.cuenet.generative.structs.SpaceTimeValueGenerators;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

public class PropagationSpaceTimeValues {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void sampleLocations() throws IOException {

        SpaceTimeValueGenerators generator = new SpaceTimeValueGenerators("/data/osm/uci.osm.locations.samples");
        int c = 10;
        String[] locKeys = new String[c];
        Iterator<String> iter = generator.getLocationValueIterator();
        while (iter.hasNext() && c-- > 0) {
            locKeys[c] = iter.next();
        }

        for (String s: locKeys) {
            logger.info(s + generator.getLatitude(s) + ", " + generator.getLongitude(s));
        }
    }

    @Test
    public void sampleTime() throws IOException {
        SpaceTimeValueGenerators generator = new SpaceTimeValueGenerators("/data/osm/uci.osm.locations.samples");

        int c = 10;
        while (c-- > 0) {
            logger.info("uniform: " + generator.getUniformTimestamp(0, 100));
        }

        c = 10;
        while (c-- > 0) {
            logger.info("gaussian: " + generator.getGaussianTimestamp(50, 25));
        }

    }


}
