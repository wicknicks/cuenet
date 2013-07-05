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

    public void testMultiFile(String filename) throws Exception {
        DataReader dReader = new DataReader();
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);
        NetworkBuildingHelper.createTimeIntervals(network1);

        ContextNetwork merge = NetworkBuildingHelper.prepareRootsForSampling(network1);
        logger.info(merge.nodeCount());

        List<ContextNetwork> samples = NetworkBuildingHelper.sample(network1, 5, 0.1);
        for (ContextNetwork sample : samples) {
            logger.info("sample size: " +  sample.nodeCount());
            merge.merge(sample);
        }

        List<ContextNetwork> instanceNets = NetworkBuildingHelper.createNetworkForEachInstace(network1);
        for (ContextNetwork cn: instanceNets)
            merge.merge(cn);

        logger.info(merge.nodeCount() + " " + network1.nodeCount());
        Assert.assertEquals(network1.compareNetwork(merge), true);
    }

    @Test
    public void testSmallDataset() throws Exception {
        testMultiFile("/data/osm/multi/instance.sim.1");
    }

    @Test
    public void testMediumDataset() throws Exception {
        testMultiFile("/data/osm/multi/instance.sim.3");
    }

}
