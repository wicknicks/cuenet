package esl.cuenet.generative.structs;

import java.util.*;

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

        System.out.println("max depth " + depth(temp, temp.root));
        //temp.root.setInterval(0, 20);
        //updateTimeIntervals(temp, temp.root);
        downwardTime(temp, temp.root);
        //checktimes(temp, temp.root);
        checkTree(temp, temp.root);
    }

    private void checkTree(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        Stack<ContextNetwork.InstanceId> ids = new Stack<ContextNetwork.InstanceId>();
        ids.add(start.id);
        HashMap<ContextNetwork.InstanceId, Boolean> seen = new HashMap<ContextNetwork.InstanceId, Boolean>();

        while ( !ids.isEmpty() ) {
            ContextNetwork.InstanceId id = ids.pop();
            ContextNetwork.Instance inst = network.lookup(root, id);
            for (ContextNetwork.InstanceId instanceid: inst.immediateSubevents) {
                if (seen.containsKey(instanceid))
                    throw new RuntimeException("seen instance id before" + instanceid);
                seen.put(instanceid, true);
                ids.add(instanceid);
            }

        }
    }

    private int time = 0;
    public void downwardTime(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) {
            start.intervalStart = time++; if (time == Integer.MAX_VALUE) throw new RuntimeException("overflow... WTF!!!");
            start.intervalEnd = time++; if (time == Integer.MAX_VALUE) throw new RuntimeException("overflow... WTF!!!");
            return;
        }

        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            downwardTime(root, subevent);
        }

        int min = Integer.MAX_VALUE, max = -1;
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            if (subevent.intervalStart < min) min = subevent.intervalStart;
            if (subevent.intervalEnd > max) max = subevent.intervalEnd;
        }
        start.intervalEnd = max;
        start.intervalStart = min;
    }

    public void checktimes(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) return;
        int span = (start.intervalEnd - start.intervalStart) / start.immediateSubevents.size();
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            if ( (subevent.intervalEnd - subevent.intervalStart) != span)
                throw new NullPointerException("bad times " + subevent + " " + (subevent.intervalEnd - subevent.intervalStart) + " " + span);
            else {
                checktimes(root, subevent);
            }
        }
    }


    public int depth(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) return 0;
        int maxdepth = -1;
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            int d = depth(root, network.lookup(root, instanceid));
            if (d > maxdepth)
                maxdepth = d;
        }
        return maxdepth + 1;
    }

    private void updateTimeIntervals(ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance current) {
        int count = current.immediateSubevents.size();
        if (count == 0) return;

        int span = (current.intervalEnd - current.intervalStart) / count;
//        int span = (current.intervalEnd - current.intervalStart) - current.immediateSubevents.size();

        int i=0;
        for (ContextNetwork.InstanceId instanceid: current.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            subevent.location = current.location;
            subevent.intervalStart = current.intervalStart + (span * i);
            subevent.intervalEnd = current.intervalStart + (span * (i+1));
//            subevent.intervalStart = current.intervalStart + i;
//            subevent.intervalEnd = current.intervalStart + i + span + 1;
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
            System.out.println("Generating Sample #" + i);

            ContextNetwork sNet = new ContextNetwork();
            sNet.addAtomic(network.eventTrees.get(0).root.attributeClone());

            sampleIntoNetwork(sNet, 0.1);
            networks.add(sNet);
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
        int i=0;
        while (sNet.nodeCount() < nodeCount) {
            i++;
        //for (int i=0; i<nodeCount; i++) {
            int k = keys[generator.nextInt(keys.length)];

            HashSet<ContextNetwork.Instance> instances = tree.typeIndex.get(k);
            int l = generator.nextInt(instances.size());
            ContextNetwork.Instance temp = null;
            for (ContextNetwork.Instance inst: instances) {
                temp = inst;
                if (l-- == 0) break;
            }
            assert temp != null;

            if (temp.equals(tree.root)) {
                i--;
                //System.out.println("Got root");
                continue;
            }

            if ( sNet.eventTrees.get(0).instanceMap.containsKey(temp.id)) {
                i--;
                //System.out.println("Repeat");
                continue;
            }

            ContextNetwork tempNet = new ContextNetwork();
            tempNet.addAtomic(temp.attributeClone());

            int mc = sNet.nodeCount();
            sNet.merge(tempNet);
            if (sNet.nodeCount() - mc != 1) {
                //System.out.println("Didn't add");
                sNet.merge(tempNet);
            }

            if (i % 500 == 0) System.out.println("Sample contains " + i + " nodes, need: " + nodeCount);
        }
    }

}
