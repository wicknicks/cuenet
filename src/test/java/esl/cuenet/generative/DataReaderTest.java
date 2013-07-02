package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class DataReaderTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(DataReaderTest.class);


    @Test
    public void loadOntTest() throws Exception {
        DataReader dReader = new DataReader();
        dReader.readOntology("/data/osm/events.ont");
    }

    @Test
    public void loadTreeAndCountTest() throws Exception {
        DataReader dReader = new DataReader();
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");
        System.out.println("trees: " + network1.count());
        System.out.println("nodes: " + network1.nodeCount());
//        network1.printTree();
    }

    @Test
    public void loadTest() throws Exception {
        DataReader dReader = new DataReader();

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/instance.sim");

        System.out.println(network1.count());
        System.out.println(network2.count());
    }

    @Test
    public void compareNetworksTest() throws Exception {
        DataReader dReader = new DataReader();

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/instance.sim");
        ContextNetwork network_small = dReader.readInstanceGraphs("/data/osm/inst-small.sim");

        System.out.println(network1.count() + " " + network2.count() + " " + network_small.count());

        Assert.assertEquals(network1.compareNetwork(network2), true);
        Assert.assertEquals(network1.compareNetwork(network_small), false);

        logger.info("Loading large net #1");
        ContextNetwork network_large = dReader.readInstanceGraphs("/data/osm/instance.sim.4");

        logger.info("Loading large net #2");
        ContextNetwork network_large2 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");

        logger.info("Compare equal networks");
        Assert.assertEquals(network_large.compareNetwork(network_large2), true);

        logger.info("Compare unequal net");
        Assert.assertEquals(network_large.compareNetwork(network1), false);

        logger.info("Done!");
    }

}
