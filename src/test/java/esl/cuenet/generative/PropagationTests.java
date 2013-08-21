package esl.cuenet.generative;

import com.google.common.collect.Sets;
import com.mongodb.util.JSON;
import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.*;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;
import esl.cuenet.algorithms.firstk.personal.accessor.SourceFactory;
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

public class PropagationTests {

    static{
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(getClass());

    public static void load () {
        LocalFilePreprocessor.ExifExtractor extractor = new LocalFilePreprocessor.ExifExtractor();
        try {
            Main.EXIF = extractor.extractExif(PConstants.IMAGE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SourceFactory.getFactory().getSources();

        Candidates.getInstance().logistics(false);
    }

    @Test
    public void propagate() throws IOException {

        load();
        logger.info(Main.EXIF);

        String locationSamplesFile = "/data/osm/uci.osm.locations.samples";
//        String instanceFile = "/data/ranker/instances.10.ic.100.txt";
//        String distanceFile = "/data/ranker/ontology_edgelist.10.distances.txt";
        String instanceFile = "/data/ranker/real/instances.arjun.txt";
        String distanceFile = "/data/ranker/real/ontology_cuenet.distances.txt";

        SpaceTimeValueGenerators stGenerator = new SpaceTimeValueGenerators(locationSamplesFile);
        ContextNetwork network = NetworkBuildingHelper.loadNetworkForPropagation(
                instanceFile,
                stGenerator);

        Propagate propagator = new Propagate(network, distanceFile, stGenerator);
        propagator.show();

        String[] entities = new String[12];
        //for (int i=1; i<=10; i++) entities[i-1] = "" + i;
        loadEntities(PConstants.IMAGE + ".annotations", entities);
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

        Candidates candidateSet = Candidates.getInstance();
        List<Map.Entry<String,Double>> objects = propagator.orderObjects();

        int _x = 25;
        for (Map.Entry<String, Double> o: objects) {
            logger.info(o.getKey() + " " + candidateSet.get(new Candidates.CandidateReference(Integer.parseInt(o.getKey()))));
            if (_x-- == 0) break;
        }


    }

    private void loadEntities(String filename, String[] entities) {
        int i = 0;
        Candidates candidateSet = Candidates.getInstance();
        Candidates.CandidateReference user = new Candidates.CandidateReference(0);

        try {
            List<String> lines = FileUtils.readLines(new File(filename));
            Collections.shuffle(lines);

            for (String line : lines) {
                line = line.replace('"', ' ');
                line = line.trim();
                Candidates.CandidateReference ref = candidateSet.searchLimitOne(Candidates.NAME_KEY, line.toLowerCase());
                if (ref.equals(user)) continue;
                if (i == entities.length) break;
                entities[i] = ref.toString();
                logger.info(line + " " + entities[i]);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Entities: " + Arrays.toString(entities));
    }


}
