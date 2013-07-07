package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import org.junit.Assert;
import org.junit.Test;

public class MergeTests {

    //@Test
    public void mergeTest1() throws Exception {
        DataReader dReader = new DataReader();

        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");
        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");

        System.out.println(network1.nodeCount() + " " + network2.nodeCount());

        Assert.assertEquals(network1.compareNetwork(network2), true);
        long s = System.currentTimeMillis();
        network1.merge(network2);
        System.out.println("Time " + (System.currentTimeMillis() - s));

        System.out.println(network1.nodeCount() + " " + network2.nodeCount());

        Assert.assertEquals(network1.compareNetwork(network2), true);
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

        ContextNetwork network4 = new ContextNetwork();
        ContextNetwork.Instance i1_0 = new ContextNetwork.Instance(1, 0);
        i1_0.setLocation(location);
        i1_0.setInterval(0, 10000);
        network4.addAtomic(i1_0);


        network1.printTree();
        network1.merge(network2);
        network1.printTree();
        network1.merge(network3);
        network1.printTree();
        network1.merge(network4);
        network1.printTree();

        ContextNetwork network5 = new ContextNetwork();
        ContextNetwork.Instance i2_0 = new ContextNetwork.Instance(2, 0);
        i2_0.setLocation(location);
        i2_0.setInterval(0, 9000);
        ContextNetwork.Instance i3_0 = new ContextNetwork.Instance(3, 0);
        i3_0.setLocation(location);
        i3_0.setInterval(0, 8500);
        ContextNetwork.Instance i4_0 = new ContextNetwork.Instance(4, 0);
        i4_0.setLocation(location);
        i4_0.setInterval(0, 8000);
        network5.addAtomic(i2_0);
        network5.addSubeventEdge(i2_0, i2_0, i3_0);
        network5.addSubeventEdge(i2_0, i3_0, i4_0);

        network1.merge(network5);
        network1.printTree();

        System.out.println("Merge");
    }

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
        network1.printTree();

        ContextNetwork network2 = new ContextNetwork();
        ContextNetwork.Instance A = new ContextNetwork.Instance(100, 0);
        A.setLocation(location);
        A.setInterval(10, 990);
        ContextNetwork.Instance B = new ContextNetwork.Instance(101, 0);
        B.setLocation(location);
        B.setInterval(20, 440);
        ContextNetwork.Instance C = new ContextNetwork.Instance(102, 0);
        C.setLocation(location);
        C.setInterval(450, 980);
        ContextNetwork.Instance D = new ContextNetwork.Instance(103, 0);
        D.setLocation(location);
        D.setInterval(20, 200);
        ContextNetwork.Instance E = new ContextNetwork.Instance(104, 0);
        E.setLocation(location);
        E.setInterval(210, 420);
        ContextNetwork.Instance F = new ContextNetwork.Instance(105, 0);
        F.setLocation(location);
        F.setInterval(480, 740);
        ContextNetwork.Instance G = new ContextNetwork.Instance(106, 0);
        G.setLocation(location);
        G.setInterval(760, 960);

        network2.addAtomic(A);
        network2.addSubeventEdge(A, A, B);
        network2.addSubeventEdge(A, A, C);
        network2.addSubeventEdge(A, B, D);
        network2.addSubeventEdge(A, B, E);
        network2.addSubeventEdge(A, C, F);
        network2.addSubeventEdge(A, C, G);

        network2.printTree();

        network1.merge(network2);
        network1.printTree();
    }


    @Test
    public void mergeMultiTest() {
        String location = "## 171169284 $$ 1822416005";
        ContextNetwork network1 = new ContextNetwork();
        ContextNetwork.Instance i1_0 = new ContextNetwork.Instance(1, 0);
        i1_0.setLocation(location);
        i1_0.setInterval(0, 100);
        ContextNetwork.Instance i2_0 = new ContextNetwork.Instance(2, 0);
        i2_0.setLocation(location);
        i2_0.setInterval(0, 10);
        ContextNetwork.Instance i3_0 = new ContextNetwork.Instance(3, 0);
        i3_0.setLocation(location);
        i3_0.setInterval(10, 20);
        ContextNetwork.Instance i4_0 = new ContextNetwork.Instance(4, 0);
        i4_0.setLocation(location);
        i4_0.setInterval(20, 30);
        ContextNetwork.Instance i5_0 = new ContextNetwork.Instance(5, 0);
        i5_0.setLocation(location);
        i5_0.setInterval(70, 100);
        network1.addAtomic(i1_0);
        network1.addSubeventEdge(i1_0, i1_0, i2_0);
        network1.addSubeventEdge(i1_0, i1_0, i3_0);
        network1.addSubeventEdge(i1_0, i1_0, i4_0);
        network1.addSubeventEdge(i1_0, i1_0, i5_0);
        network1.printTree();

        ContextNetwork network2 = new ContextNetwork();
        ContextNetwork.Instance A = new ContextNetwork.Instance(100, 0);
        A.setLocation(location);
        A.setInterval(0, 50);
        ContextNetwork.Instance B = new ContextNetwork.Instance(101, 0);
        B.setLocation(location);
        B.setInterval(0, 25);
        ContextNetwork.Instance C = new ContextNetwork.Instance(102, 0);
        C.setLocation(location);
        C.setInterval(25, 50);
        network2.addAtomic(A);
        network2.addSubeventEdge(A, A, B);
        network2.addSubeventEdge(A, A, C);
        network2.printTree();

        network1.merge(network2);
        network1.printTree();
    }
}
