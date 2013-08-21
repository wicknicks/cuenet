package esl.cuenet.generative;

import com.google.common.collect.Sets;
import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.Main;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.cuenet.generative.structs.Propagate;
import esl.cuenet.generative.structs.SpaceTimeValueGenerators;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SimplePropagationTest {

    static{
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(getClass());

    public ContextNetwork load(String eventsfile, String annFile) {

        ContextNetwork network = null;

        try {
            network = NetworkBuildingHelper.coloredLoad(eventsfile,
                    FileUtils.readLines(new File(annFile)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        network.printTree(true);

        int nTimestamp = 1339781224;
        double glat = 33.642, glon = -117.833;
        int event_type = 4;       //(4, 5, 6)
        int instance_count = 9;   //(9, 5, 6)

        String locationKey = UUID.randomUUID().toString();

        //Set<String> objects = Sets.newHashSet("189", "190", "191", "192", "193", "194");
//        Set<String> objects = Sets.newHashSet("64", "65", "66", "67");
        Set<String> objects = Sets.newHashSet("10", "11", "12", "13");

        ContextNetwork tempNet = NetworkBuildingHelper.createNetwork(nTimestamp, locationKey,
                event_type, instance_count, objects);
        NetworkBuildingHelper.addToNetwork(network, tempNet);

        try {
            FileUtils.writeStringToFile(new File("/data/ranker/colored/tempLocations.txt"),
                    locationKey + "," + glat + "," + glon, true);
        } catch (IOException e) {
            e.printStackTrace();
        }



        return network;
    }

    @Test
    public void coloredTest() throws IOException {
        String distanceFile = "/data/ranker/real/ontology_cuenet.distances.txt";

        ContextNetwork network = load("/data/ranker/colored/photos.txt", "/data/ranker/colored/annotations.txt");
        logger.info(network.count());

        SpaceTimeValueGenerators stGenerator = new SpaceTimeValueGenerators("/data/ranker/colored/tempLocations.txt");

        Propagate propagator = new Propagate(network, distanceFile, stGenerator);
        propagator.show();

        propagator.prepare(Sets.newHashSet("64"));

        double l1delta;
        double[] deltas = new double[10];

        for (int i=0; i<10; i++) {
            l1delta = propagator.propagateOnceTable();
            logger.info("delta = " + l1delta);
            propagator.printScores(4, 9);
            deltas[i] = l1delta;
        }

        //propagator.printScores(6, 6);
        propagator.printScores(4, 9);

//        for (int i=0; i<10; i++) {
//            l1delta = propagator.propagateOnce();
//            logger.info(l1delta);
//            deltas[i] = l1delta;
//        }
//
//        logger.info(Arrays.toString(deltas));
//
//        Candidates candidateSet = Candidates.getInstance();
//        List<Map.Entry<String,Double>> objects = propagator.orderObjects();
//
//        int _x = 25;
//        for (Map.Entry<String, Double> o: objects) {
//            logger.info(o.getKey() + " " + candidateSet.get(new Candidates.CandidateReference(Integer.parseInt(o.getKey()))));
//            if (_x-- == 0) break;
//        }
    }

}
