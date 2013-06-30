package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.Ontology;
import org.junit.Test;

public class DataReaderTest {

    @Test
    public void loadOntTest() throws Exception {
        DataReader dReader = new DataReader();
        dReader.readOntology("/data/osm/events.ont");
    }

    @Test
    public void createTreeTest() throws Exception {
        DataReader dReader = new DataReader();
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/inst-small.sim");
        network1.printTree();
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
    public void mergeTest1() throws Exception {
        DataReader dReader = new DataReader();

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/instance.sim");

        System.out.println(network1.count());
        System.out.println(network2.count());

        network1.merge(network2);
    }

}
