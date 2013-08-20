package esl.cuenet.generative;

import com.google.common.collect.Sets;
import com.mongodb.util.JSON;
import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.cuenet.generative.structs.Propagate;
import esl.cuenet.generative.structs.SpaceTimeValueGenerators;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class PropagationTests {

    static{
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(getClass());

    @Test
    public void propagate() throws IOException {
        SpaceTimeValueGenerators stGenerator = new SpaceTimeValueGenerators("/data/osm/uci.osm.locations.samples");
        ContextNetwork network = NetworkBuildingHelper.loadNetworkForPropagation(
                "/data/ranker/instances.10.ic.100.txt",
                stGenerator);

        Propagate propagator = new Propagate(network, "/data/ranker/ontology_edgelist.10.distances.txt", stGenerator);
        propagator.show();

        String[] entities = new String[10];
        for (int i=1; i<=10; i++) entities[i-1] = "" + i;
        propagator.prepare(Sets.newHashSet(entities));

        double l1delta;
        double[] deltas = new double[10];

        for (int i=0; i<10; i++) {
            l1delta = propagator.propagateOnce();
            logger.info(l1delta);
            deltas[i] = l1delta;
        }

        logger.info(Arrays.toString(deltas));
        //propagator.dispScores();
    }


}
