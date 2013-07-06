package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class MultiLocationMergeTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(MultiLocationMergeTest.class);

    public ContextNetwork testMultiNetwork(ContextNetwork network1, int sampleCount, double samplePercentage) throws Exception {

        assert sampleCount > 1;
        assert samplePercentage > 0;
        assert samplePercentage <= 1;

        NetworkBuildingHelper.createTimeIntervals(network1);

        ContextNetwork merge = NetworkBuildingHelper.prepareRootsForSampling(network1);
        logger.info(merge.nodeCount());

        int i=0;
        long s, e;
        double total = 0;
        List<ContextNetwork> samples = null;
        for (int t = 0; t < sampleCount; t+=10) {
            samples = NetworkBuildingHelper.sample(network1, 10, samplePercentage);
            for (ContextNetwork sample : samples) {
                i++;
                s = System.currentTimeMillis();
                merge.merge(sample);
                e = System.currentTimeMillis();
                total += (e-s);
                if (i%20 == 0) logger.info("Post merge node count #" + i + " ; Avg. Time  = " + (total/i));
            }
        }

        logger.info("Average Merge Time = " + (total/sampleCount));

        return merge;
    }

    public void instanceMergeTest(ContextNetwork network1) {
        ContextNetwork merge = NetworkBuildingHelper.prepareRootsForSampling(network1);
        logger.info("Nodes in merge CN = " + merge.nodeCount());

        long s = System.currentTimeMillis();
        NetworkBuildingHelper.mergeEachInstance(merge, network1);
        logger.info("Instance Nets Merge Time = " + (System.currentTimeMillis() - s));

        logger.info(merge.nodeCount() + " " + network1.nodeCount());
        Assert.assertEquals(network1.compareNetwork(merge), true);
    }

    public void testMultiFile(String filename, int sampleCount, double samplePercentage) throws Exception {

        assert sampleCount > 1;
        assert samplePercentage > 0;
        assert samplePercentage <= 1;

        DataReader dReader = new DataReader();
        logger.info("----------------------------------------------------------------------------");
        logger.info("L O A D I N G    " + filename);
        logger.info("----------------------------------------------------------------------------");

        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        testMultiNetwork(network1, sampleCount, samplePercentage);
    }

    @Test
    public void testSmallDataset() throws Exception {
        testMultiFile("/data/osm/multi/instance.sim.1", 10, 0.33);
    }

    @Test
    public void testDataset() throws Exception {
        testMultiFile("/home/arjun/data/increasing_locations/instance.sim.7", 500, 0.33);
    }

    @Test
    public void instanceMergeTest() throws Exception {
        String filename = "/data/osm/increasing_locations/instance.sim.7";

        DataReader dReader = new DataReader();
        logger.info("Loading ---- " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        instanceMergeTest(network1);
    }

    @Test
    public void multiSampleTest() throws Exception {
        String filename = "/data/osm/multi/instance.sim.5";

        DataReader dReader = new DataReader();
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        ContextNetwork finalMerge = null;
        for (int i=0; i<10; i++) {
            logger.info("Starting iteration ... " + (i+1));
            ContextNetwork merge = testMultiNetwork(network1, 5*(i+1), 0.1);
            if (finalMerge == null) finalMerge = merge;
            else finalMerge.merge(merge);
        }

        NetworkBuildingHelper.mergeEachInstance(finalMerge, network1);
        Assert.assertEquals(network1.compareNetwork(finalMerge), true);
    }

    @Test
    public void increasingSampleTest() throws Exception {
        String filename = "/data/osm/multi/instance.sim.5";

        DataReader dReader = new DataReader();
        logger.info("Loading ---- " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        for (double i=0.1; i<0.99; i+=0.1) {
            logger.info("Starting iteration ... " + (i+1));
            testMultiNetwork(network1, 10, i);
        }
    }

    @Test
    public void increasingLocations() throws Exception {
        String file = "/home/arjun/data/increasing_locations/instance.sim.";
        for (int i=1; i<15; i++) {
            testMultiFile(file + i, 10, 0.33);
        }
    }
}
