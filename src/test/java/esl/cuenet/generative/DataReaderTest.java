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

    @Test
    public void mergeTest2() throws Exception {
        String location = "## 171169284 $$ 1822416005";

        ContextNetwork network1 = new ContextNetwork();
        ContextNetwork.Instance i14_0 = new ContextNetwork.Instance(14, 0);
        network1.addAtomic(i14_0);
        i14_0.setLocation(location);
        i14_0.setInterval(0,5000);
        ContextNetwork.Instance i79_0 = new ContextNetwork.Instance(79, 0);
        i79_0.setLocation(location);
        i79_0.setInterval(0, 2500);

        network1.addSubeventEdge(i14_0, i14_0, i79_0);



        ContextNetwork network2 = new ContextNetwork();
        i14_0 = new ContextNetwork.Instance(14, 0);
        i14_0.setLocation(location);
        i14_0.setInterval(0,5000);
        ContextNetwork.Instance i79_1 = new ContextNetwork.Instance(79, 1);
        i79_1.setLocation(location);
        i79_1.setInterval(2500, 4500);
        ContextNetwork.Instance i79_3 = new ContextNetwork.Instance(79, 3);
        i79_3.setLocation(location);
        i79_3.setInterval(4500, 5000);

        network2.addAtomic(i14_0);
        network2.addSubeventEdge(i14_0, i14_0, i79_1);
        network2.addSubeventEdge(i14_0, i14_0, i79_3);



        ContextNetwork network3 = new ContextNetwork();
        ContextNetwork.Instance i46_0 = new ContextNetwork.Instance(46, 0);
        i46_0.setLocation(location);
        i46_0.setInterval(4600, 4700);

        network3.addAtomic(i46_0);



        network1.merge(network2);
        network1.merge(network3);

        System.out.println("Merge");
    }
}
