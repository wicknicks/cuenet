package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import org.junit.Test;

import java.util.List;

public class MultiLocationMergeTest {

    @Test
    public void loadMultiFile() throws Exception {
        DataReader dReader = new DataReader();
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/multi/instance.sim.1");
        NetworkBuildingHelper.createTimeIntervals(network1);

        List<ContextNetwork> instanceNets = NetworkBuildingHelper.createNetworkForEachInstace(network1);
        for (ContextNetwork cn: instanceNets)
            network1.merge(cn);

        ContextNetwork network2 = dReader.readInstanceGraphs("/data/osm/multi/instance.sim.1");
        NetworkBuildingHelper.createTimeIntervals(network2);
        System.out.println(network1.compareNetwork(network2));
    }

}
