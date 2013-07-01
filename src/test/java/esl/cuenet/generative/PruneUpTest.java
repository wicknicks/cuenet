package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PruneUpTest {

    @Test
    public void mergeTest3() throws Exception {
        String location = "## 171169284 $$ 1822416005";

        ContextNetwork network1 = new ContextNetwork();
        ContextNetwork.Instance i1_0 = new ContextNetwork.Instance(1, 0);
        i1_0.setLocation(location);
        i1_0.setInterval(0, 1000);
        ContextNetwork.Instance i2_0 = new ContextNetwork.Instance(2, 0);
        i2_0.setLocation(location);
        i2_0.setInterval(0, 300);
        ContextNetwork.Instance i3_0 = new ContextNetwork.Instance(3, 0);
        i3_0.setLocation(location);
        i3_0.setInterval(300, 700);
        ContextNetwork.Instance i4_0 = new ContextNetwork.Instance(4, 0);
        i4_0.setLocation(location);
        i4_0.setInterval(700, 1000);
        network1.addAtomic(i1_0);
        network1.addSubeventEdge(i1_0, i1_0, i2_0);
        network1.addSubeventEdge(i1_0, i1_0, i3_0);
        network1.addSubeventEdge(i1_0, i1_0, i4_0);

        ContextNetwork.Instance i7_0 = new ContextNetwork.Instance(7, 0);
        i7_0.setLocation(location);
        i7_0.setInterval(700, 800);
        ContextNetwork.Instance i8_0 = new ContextNetwork.Instance(8, 0);
        i8_0.setLocation(location);
        i8_0.setInterval(800, 900);
        network1.addSubeventEdge(i1_0, i2_0, i7_0);
        network1.addSubeventEdge(i1_0, i2_0, i8_0);

        List<ContextNetwork.Entity> entities = new ArrayList<ContextNetwork.Entity>();
        for (int i=0; i<10; i++) entities.add(new ContextNetwork.Entity("P", "" + i));
        network1.populateEntities(entities);

        network1.printTree();

        network1.pruneUp();

        network1.printTree();
    }
}
