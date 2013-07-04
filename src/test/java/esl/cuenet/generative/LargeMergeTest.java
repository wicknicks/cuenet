package esl.cuenet.generative;

import com.javamex.classmexer.MemoryUtil;
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

    public void testLoadSampleMerge(String filename, int _sample_count, double percentage) throws Exception {

        assert _sample_count > 1;

        DataReader dReader = new DataReader();

        logger.info("Loading network1... " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);
        logger.info("Nodes: " + network1.nodeCount() + " ; memory = " + MemoryUtil.deepMemoryUsageOf(network1));

        logger.info("Creating Samples... " + _sample_count);
        //Generate samples
        List<ContextNetwork> samples = NetworkBuildingHelper.sample(network1, _sample_count, percentage);

        for (ContextNetwork sample : samples) {
            if ( !NetworkBuildingHelper.validateSample(network1, sample) )
                logger.info("Invalid Sample");
        }

        ContextNetwork merge = samples.get(0);
        long s;
        for (int i=1; i<samples.size(); i++) {
            s = System.currentTimeMillis();
            logger.info("Merging Sample #" + i);
            merge.merge(samples.get(i));
            logger.info("Post merge node count #" + merge.nodeCount() + " ; time  = " + (System.currentTimeMillis() - s));
            //logger.info("Size of merge graph: " + MemoryUtil.deepMemoryUsageOf(merge));
            if ( !NetworkBuildingHelper.validateSample(network1, merge) )
                logger.info("Merge corrupted merge graph");
        }

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = NetworkBuildingHelper.createNetworkForEachInstace(network1);

        logger.info("Merging Instance Nets");
        s = System.currentTimeMillis();
        for (ContextNetwork instanceNet : instanceNets)
            merge.merge(instanceNet);
        logger.info("Merges Complete Merges = " + instanceNets.size() + "; Nodes = "  + merge.nodeCount() +
                "; time = " + (System.currentTimeMillis() - s));

        if ( !NetworkBuildingHelper.validateSample(network1, merge) )
            logger.info("Merge corrupted merge graph");

        logger.info("checkorder: " + NetworkBuildingHelper.checkOrderStrict(merge));


        logger.info("Loading network2... ");
        ContextNetwork network2 = dReader.readInstanceGraphs(filename);

        logger.info("Final node counts: " + merge.nodeCount() + " " + network2.nodeCount());
        logger.info("Size of merge graph: " + MemoryUtil.deepMemoryUsageOf(merge));

        boolean val = merge.compareNetwork(network2);
        logger.info("Equals: " + val);
        Assert.assertEquals(val, true);
    }

    @Test
    public void checkOrderTest() throws Exception {
        DataReader dReader = new DataReader();
        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");
        logger.info("strict check " + NetworkBuildingHelper.checkOrderStrict(network1));
    }

    @Test
    public void testSmall() throws Exception {
        testLoadSampleMerge("/data/osm/inst-small.sim", 10, 0.1);
    }

    @Test
    public void testMid() throws Exception {
        //testLoadSampleMerge("/data/osm/inst-mid.sim", 5);
        testLoadSampleMerge("/data/osm/10/instance.sim.4", 5, 0.1);
    }

    @Test
    public void repeatTestMid() throws Exception {
        for (int i=1; i<3; i++) {
            testLoadSampleMerge("/data/osm/instance.sim.4", 5*i, 0.1);
            logger.info("------------------------------------------");
        }
    }

    @Test
    public void multiDatasetRepeatTestMid() throws Exception {
        String[] filenames = new String[10];
        for (int i=1; i<=10; i++) filenames[i-1] = "/data/osm/10/instance.sim." + i;
        for (String file: filenames) {
            testLoadSampleMerge(file, 50, 0.1);
            logger.info("------------------------------------------ " + file);
        }
    }

    @Test
    public void testLarge() throws Exception {
        testLoadSampleMerge("/data/osm/instance.sim.6", 5, 0.1);
    }

    @Test
    public void testMergeWithIncreasingSampleSize() throws Exception {
        String filename = "/data/osm/6/instance.sim.7";
        for (double p=0.1; p<=0.7; p+=0.1) {
            testLoadSampleMerge(filename, 10, 0.1);
            logger.info("------------------------------------------ sampling at " + p);
        }
    }


}
