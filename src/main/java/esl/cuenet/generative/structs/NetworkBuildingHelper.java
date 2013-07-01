package esl.cuenet.generative.structs;

import java.util.ArrayList;
import java.util.List;

public class NetworkBuildingHelper {

    private final ContextNetwork network;

    public NetworkBuildingHelper(ContextNetwork network) {
        this.network = network;
    }

    public void updateTimeIntervals(ContextNetwork.Instance root) {
        ContextNetwork.IndexedSubeventTree temp = null;
        for (int i=network.eventTrees.size()-1; i>=0; i--) {
            if (network.eventTrees.get(i).root.equals(root)) {
                temp = network.eventTrees.get(i);
                break;
            }
        }

        if (temp == null) throw new RuntimeException("Invalid root: " + root);

        updateTimeIntervals(temp, temp.root);
    }

    private void updateTimeIntervals(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance current) {
        int count = current.immediateSubevents.size();
        if (count == 0) return;

        int span = (current.intervalEnd - current.intervalStart) / count;

        int i=0;
        for (ContextNetwork.InstanceId instanceid: current.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            subevent.location = current.location;
            subevent.intervalStart = current.intervalStart + (span * i);
            subevent.intervalEnd = current.intervalStart + (span * (i+1));
            i++;
            updateTimeIntervals(root, subevent);
        }
    }

    public void populateEntities(List<ContextNetwork.Entity> entities) {
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees)
            populateEntities(entities, tree);
    }

    private void populateEntities(List<ContextNetwork.Entity> entities, ContextNetwork.IndexedSubeventTree tree) {
        for (int event: tree.typeIndex.keySet()) {
            for (ContextNetwork.Instance instance: tree.typeIndex.get(event))
                instance.participants = new ArrayList<ContextNetwork.Entity>(entities);
        }
    }

}
