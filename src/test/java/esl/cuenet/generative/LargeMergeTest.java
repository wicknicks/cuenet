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


    public void testLoadSampleMerge(String filename, int _sample_count) throws Exception {

        assert _sample_count > 1;

        DataReader dReader = new DataReader();

        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        NetworkBuildingHelper helper = new NetworkBuildingHelper(network1);

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = helper.createNetworkForEachInstace();

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

        logger.info("Loading network2... ");
        ContextNetwork network2 = dReader.readInstanceGraphs(filename);

        System.out.println(merge.nodeCount() + " " + network2.nodeCount());

        boolean val = merge.compareNetwork(network2);
        System.out.println("Return: " + val);
        Assert.assertEquals(val, true);
    }

    @Test
    public void testSmall() throws Exception {
        testLoadSampleMerge("/data/osm/inst-small.sim", 10);
    }

    @Test
    public void testMid() throws Exception {
        testLoadSampleMerge("/data/osm/inst-mid.sim", 5);
    }

    @Test
    public void testLarge() throws Exception {
        testLoadSampleMerge("/data/osm/inst-large.sim", 5);
    }

}
