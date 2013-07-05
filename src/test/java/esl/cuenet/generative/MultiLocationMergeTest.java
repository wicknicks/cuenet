package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MultiLocationMergeTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(MultiLocationMergeTest.class);

    public void testMultiNetwork(ContextNetwork network1, int sampleCount, double samplePercentage) throws Exception {

        assert sampleCount > 1;
        assert samplePercentage > 0;
        assert samplePercentage <= 1;

        NetworkBuildingHelper.createTimeIntervals(network1);

        ContextNetwork merge = NetworkBuildingHelper.prepareRootsForSampling(network1);
        logger.info(merge.nodeCount());

        List<ContextNetwork> samples = NetworkBuildingHelper.sample(network1, sampleCount, samplePercentage);

        int i=0; long s;
        for (ContextNetwork sample : samples) {
            i++;
            s = System.currentTimeMillis();
            logger.info("Merging Sample #" + i);
            merge.merge(sample);
            logger.info("Post merge node count #" + merge.nodeCount() + " ; Time  = " + (System.currentTimeMillis() - s));
        }

        s = System.currentTimeMillis();
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
        logger.info("Loading ---- " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        testMultiNetwork(network1, sampleCount, samplePercentage);
    }

    @Test
    public void testSmallDataset() throws Exception {
        testMultiFile("/data/osm/multi/instance.sim.1", 5, 0.1);
    }

    @Test
    public void multiSampleTest() throws Exception {
        String filename = "/data/osm/multi/instance.sim.5";

        DataReader dReader = new DataReader();
        logger.info("Loading ---- " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        for (int i=0; i<10; i++) testMultiNetwork(network1, 5*(1+1), 0.1);
    }

}
