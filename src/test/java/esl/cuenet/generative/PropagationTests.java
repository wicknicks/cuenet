package esl.cuenet.generative;

import com.mongodb.util.JSON;
import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class PropagationTests {

    static{
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(getClass());

    @Test
    public void propagate() throws IOException {
        ContextNetwork network = NetworkBuildingHelper.loadNetworkForPropagation(
                "/data/ranker/instances.10.ic.1000.txt",
                "/data/osm/uci.osm.locations.samples");
        logger.info(network.count());
    }
}
