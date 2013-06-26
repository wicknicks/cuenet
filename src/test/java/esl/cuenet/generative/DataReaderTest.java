package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.Ontology;
import org.junit.Test;

public class DataReaderTest {

    @Test
    public void test() throws Exception {
        DataReader dReader = new DataReader();

        //dReader.readOntology("/data/osm/events.ont");

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/instance.sim");

        System.out.println(network1.count());
        System.out.println(network2.count());

        //network1.merge(network2);
    }
}
