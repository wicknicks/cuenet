package esl.cuenet.generative.structs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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

    public List<ContextNetwork> createNetworkForEachInstace() {
        List<ContextNetwork> networks = new ArrayList<ContextNetwork>();
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (int event: tree.typeIndex.keySet()) {
                for (ContextNetwork.Instance instance: tree.typeIndex.get(event)) {
                    ContextNetwork n = new ContextNetwork();
                    n.addAtomic(instance.attributeClone());
                    networks.add(n);
                }
            }
        }
        return networks;
    }

    public List<ContextNetwork> sample(int count) {
        List<ContextNetwork> networks = new ArrayList<ContextNetwork>();

        for (int i=0; i<count; i++) {
            ContextNetwork network = new ContextNetwork();
            sampleIntoNetwork(network, 0.1);
            networks.add(network);
        }

        return networks;
    }

    private void sampleIntoNetwork(ContextNetwork sNet, double percentage) {
        Random generator = new Random();
        //randomly choose an eventTree
        ContextNetwork.IndexedSubeventTree tree = network.eventTrees.get(generator.nextInt(network.eventTrees.size()));

        sampleFromTree(sNet, tree, (int) Math.ceil(tree.nodeCount() * percentage));
    }

    private void sampleFromTree(ContextNetwork sNet, ContextNetwork.IndexedSubeventTree tree, int nodeCount) {
        Random generator = new Random();
        Integer[] keys = new Integer[tree.typeIndex.keySet().size()];
        tree.typeIndex.keySet().toArray(keys);
        for (int i=0; i<nodeCount; i++) {
            int k = keys[generator.nextInt(keys.length)];

            HashSet<ContextNetwork.Instance> instances = tree.typeIndex.get(k);
            int l = generator.nextInt(instances.size());
            ContextNetwork.Instance temp = null;
            for (ContextNetwork.Instance inst: instances) {
                temp = inst;
                if (l-- == 0) break;
            }
            assert temp != null;
            ContextNetwork tempNet = new ContextNetwork();
            tempNet.addAtomic(temp.attributeClone());
            sNet.merge(tempNet);
        }

//        sNet.printTree();
    }

}
