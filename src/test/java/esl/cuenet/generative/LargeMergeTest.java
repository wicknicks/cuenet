package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LargeMergeTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(LargeMergeTest.class);

    @Test
    public void testSmall() throws Exception {
        DataReader dReader = new DataReader();

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");
        NetworkBuildingHelper helper = new NetworkBuildingHelper(network1);

        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = helper.createNetworkForEachInstace();

        //Generate samples
        List<ContextNetwork> samples = helper.sample(10);

        ContextNetwork merge = samples.get(0);
        for (int i=1; i<samples.size(); i++)
            merge.merge(samples.get(i));

        for (ContextNetwork n: instanceNets)
            merge.merge(n);

        //Assert.assertEquals(samples.get(0).compareNetwork(network2), true);
        Assert.assertEquals(merge.compareNetwork(network2), true);

    }

    @Test
    public void testSmallWithLogs() throws Exception {
        DataReader dReader = new DataReader();

        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");
        logger.info("Loading network2... ");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");

        NetworkBuildingHelper helper = new NetworkBuildingHelper(network1);

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = helper.createNetworkForEachInstace();

        int _sample_count = 10;
        logger.info("Creating Samples... " + _sample_count);
        //Generate samples
        List<ContextNetwork> samples = helper.sample(_sample_count);

        ContextNetwork merge = samples.get(0);
        for (int i=1; i<samples.size(); i++) {
            logger.info("Merging Sample #" + i);
            merge.merge(samples.get(i));
        }

        logger.info("Merging Instance Nets");
        for (ContextNetwork n: instanceNets)
            merge.merge(n);

        //Assert.assertEquals(samples.get(0).compareNetwork(network2), true);
        Assert.assertEquals(merge.compareNetwork(network2), true);
    }

    @Test
    public void testLarge() throws Exception {
        DataReader dReader = new DataReader();

        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/inst-large.sim");
        logger.info("Loading network2... ");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/inst-large.sim");

        NetworkBuildingHelper helper = new NetworkBuildingHelper(network1);

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = helper.createNetworkForEachInstace();

        int _sample_count = 10;
        logger.info("Creating Samples... " + _sample_count);
        //Generate samples
        List<ContextNetwork> samples = helper.sample(_sample_count);

        ContextNetwork merge = samples.get(0);
        for (int i=1; i<samples.size(); i++) {
            logger.info("Merging Sample #" + i);
            merge.merge(samples.get(i));
        }

        logger.info("Merging Instance Nets");
        for (ContextNetwork n: instanceNets)
            merge.merge(n);

        //Assert.assertEquals(samples.get(0).compareNetwork(network2), true);
        Assert.assertEquals(merge.compareNetwork(network2), true);
    }

}
