package esl.cuenet.generative.structs;

import org.apache.log4j.Logger;

import java.util.*;

public class NetworkBuildingHelper {

    private static Logger logger = Logger.getLogger(NetworkBuildingHelper.class);


    public static void createTimeIntervals(ContextNetwork network) {
        time = 0;
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            downwardTime(network, tree, tree.root);
        }
    }


    private static int time = 0;
    public static void updateTimeIntervals(ContextNetwork network, ContextNetwork.Instance root) {
        ContextNetwork.IndexedSubeventTree temp = null;
        for (int i=network.eventTrees.size()-1; i>=0; i--) {
            if (network.eventTrees.get(i).root.equals(root)) {
                temp = network.eventTrees.get(i);
                break;
            }
        }

        if (temp == null) throw new RuntimeException("Invalid root: " + root);

        //System.out.println("max depth " + depth(temp, temp.root));
        //temp.root.setInterval(0, 20);
        //updateTimeIntervals(temp, temp.root);
        time = 0;
        downwardTime(network, temp, temp.root);
        //checktimes(temp, temp.root);
        checkTree(network, temp, temp.root);
    }

    public static void downwardTime(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) {
            start.intervalStart = time++; if (time == Integer.MAX_VALUE) throw new RuntimeException("overflow... WTF!!!");
            start.intervalEnd = time++; if (time == Integer.MAX_VALUE) throw new RuntimeException("overflow... WTF!!!");
            return;
        }

        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            downwardTime(network, root, subevent);
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

    public static boolean checkOrderStrict(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) return true;

        boolean val = true;
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            if (subevent.id.eventId != (start.id.eventId + 1))
                return false;
            val = val & checkOrderStrict(network, root, subevent);
        }
        return val;
    }

    public static boolean checkOrderStrict(ContextNetwork network) {
        return checkOrderStrict(network, network.eventTrees.get(0), network.eventTrees.get(0).root);
    }

    private static void checkTree(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
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



    public static void checktimes(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) return;
        int span = (start.intervalEnd - start.intervalStart) / start.immediateSubevents.size();
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            ContextNetwork.Instance subevent = network.lookup(root, instanceid);
            if ( (subevent.intervalEnd - subevent.intervalStart) != span)
                throw new NullPointerException("bad times " + subevent + " " + (subevent.intervalEnd - subevent.intervalStart) + " " + span);
            else {
                checktimes(network, root, subevent);
            }
        }
    }

    public static int depth(ContextNetwork network) {
        return depth(network, network.eventTrees.get(0), network.eventTrees.get(0).root);
    }


    public static int depth(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance start) {
        if (start.immediateSubevents.size() == 0) return 0;
        int maxdepth = -1;
        for (ContextNetwork.InstanceId instanceid: start.immediateSubevents) {
            int d = depth(network, root, network.lookup(root, instanceid));
            if (d > maxdepth)
                maxdepth = d;
        }
        return maxdepth + 1;
    }

    private static void updateTimeIntervals(ContextNetwork network, ContextNetwork.IndexedSubeventTree root, ContextNetwork.Instance current) {
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
            updateTimeIntervals(network, root, subevent);
        }
    }

    public static void populateEntities(ContextNetwork network, List<ContextNetwork.Entity> entities) {
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees)
            populateEntities(entities, tree);
    }

    private static void populateEntities(List<ContextNetwork.Entity> entities, ContextNetwork.IndexedSubeventTree tree) {
        for (int event: tree.typeIndex.keySet()) {
            for (ContextNetwork.Instance instance: tree.typeIndex.get(event))
                instance.participants = new ArrayList<ContextNetwork.Entity>(entities);
        }
    }

    public static List<ContextNetwork> createNetworkForEachInstace(ContextNetwork network) {
        List<ContextNetwork> networks = new ArrayList<ContextNetwork>();
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (int event: tree.typeIndex.keySet()) {
                for (ContextNetwork.Instance instance: tree.typeIndex.get(event)) {
                    ContextNetwork n = new ContextNetwork();
                    n.addAtomic(instance.attributeClone());
                    networks.add(n);
                    if (networks.size() >= 1000000) return networks;
                }
            }
        }
        return networks;
    }

    public static void mergeEachInstance(ContextNetwork merge, ContextNetwork network) {
        int i = 0;
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (int event: tree.typeIndex.keySet()) {
                for (ContextNetwork.Instance instance: tree.typeIndex.get(event)) {
                    ContextNetwork n = new ContextNetwork();
                    n.addAtomic(instance.attributeClone());
                    merge.merge(n);
                    i++;
                    if (i % 500000 == 0) logger.info(i + " Instance Merges Complete.");
                }
            }
        }
        logger.info("All instances merged!");
    }

    public static ContextNetwork prepareRootsForSampling(ContextNetwork network) {
        ContextNetwork cn = new ContextNetwork();

        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            cn.addAtomic(tree.root.attributeClone());
        }

        return cn;
    }

    public static List<ContextNetwork> sample(ContextNetwork network, int count, double percentage) {
        List<ContextNetwork> networks = new ArrayList<ContextNetwork>();

        Random generator = new Random();
        for (int i=0; i<count; i++) {
            ContextNetwork.IndexedSubeventTree tree = network.eventTrees.get(generator.nextInt(network.eventTrees.size()));
            HashMap<Integer, ContextNetwork.Instance[]> localInstanceMap = new HashMap<Integer, ContextNetwork.Instance[]>();
            for (int event: tree.typeIndex.keySet()) {
                HashSet<ContextNetwork.Instance> instances = tree.typeIndex.get(event);
                ContextNetwork.Instance[] insts = new ContextNetwork.Instance[instances.size()];
                instances.toArray(insts);
                localInstanceMap.put(event, insts);
            }

            //int nodelimit = 6000;
            int nodelimit = (int) Math.ceil(tree.nodeCount() * percentage);

            //logger.info("Generating Sample #" + i);

            ContextNetwork sNet = new ContextNetwork();
            sNet.addAtomic(tree.root.attributeClone());

            sampleIntoNetwork(sNet, localInstanceMap, tree, nodelimit);
            networks.add(sNet);
        }

        return networks;
    }


    private static void sampleIntoNetwork(ContextNetwork sNet, HashMap<Integer, ContextNetwork.Instance[]> localInstanceMap,
                                          ContextNetwork.IndexedSubeventTree tree, int nodeCount) {

        Random generator = new Random();
        Integer[] keys = new Integer[localInstanceMap.keySet().size()];
        localInstanceMap.keySet().toArray(keys);
        while (sNet.nodeCount() < nodeCount) {

            int k = keys[generator.nextInt(keys.length)];
            int l = generator.nextInt(localInstanceMap.get(k).length);
            ContextNetwork.Instance temp = localInstanceMap.get(k)[l];
            assert temp != null;

            if (temp.equals(tree.root)) continue;
            if ( sNet.eventTrees.get(0).instanceMap.containsKey(temp.id)) continue;

            ContextNetwork tempNet = new ContextNetwork();
            ContextNetwork.Instance new_root = tree.root.attributeClone();
            tempNet.addAtomic(new_root);
            tempNet.addSubeventEdge(new_root, new_root, temp.attributeClone());

            sNet.merge(tempNet);
        }
    }

    public static boolean validateSample(ContextNetwork original,
                                         ContextNetwork sample) {

        ContextNetwork.IndexedSubeventTree _sample = sample.eventTrees.get(0);

        for (int event: _sample.typeIndex.keySet()) {
            for (ContextNetwork.Instance instance : _sample.typeIndex.get(event)){
                for (ContextNetwork.InstanceId subeventid: instance.immediateSubevents) {
                    boolean v = validateEdge(instance, subeventid, original);
                    if (v) return true;
                }
            }
        }

        return false;
    }

    private static boolean validateEdge(ContextNetwork.Instance event,
                                        ContextNetwork.InstanceId subeventid, ContextNetwork original) {
        ContextNetwork.Instance sampleEvent = original.eventTrees.get(0).instanceMap.get(event.id);
        Stack<ContextNetwork.InstanceId> stack = new Stack<ContextNetwork.InstanceId>();
        stack.add(sampleEvent.id);

        while ( !stack.isEmpty() ) {
            ContextNetwork.InstanceId instanceid= stack.pop();
            ContextNetwork.Instance instance = original.lookup(original.eventTrees.get(0), instanceid);
            for (ContextNetwork.InstanceId subid: instance.immediateSubevents) {
                if (subid.equals(subeventid)) return true;
                stack.add(subid);
            }
        }

        return false;
    }

}
