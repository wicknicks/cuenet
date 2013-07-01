package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import org.junit.Assert;
import org.junit.Test;

public class DataReaderTest {

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
    }

}
