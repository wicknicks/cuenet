package esl.cuenet.generative.structs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static ContextNetwork coloredLoad(String eventsFile, List<String> annotationsFiles) throws IOException {

        ContextNetwork network = new ContextNetwork();
        LocalFilePreprocessor.ExifExtractor extractor = new LocalFilePreprocessor.ExifExtractor();

        LocalFilePreprocessor.Exif exif = null;
        ContextNetwork tempNet;
        FileWriter sWriter = new FileWriter("/data/ranker/colored/tempLocations.txt");
        sWriter.write("bounds\n");

        int ix = 0;
        Candidates candidateSet = Candidates.getInstance();

        for (String line: FileUtils.readLines(new File(eventsFile))) {
            String[] parts = line.split(",");
            ContextNetwork.Instance root = new ContextNetwork.Instance(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            exif = extractor.extractExif(parts[0]);

            logger.info(exif);
            tempNet = new ContextNetwork();

            root.setInterval(exif.timestamp/1000, exif.timestamp/1000);

            String sid = UUID.randomUUID().toString();
            sWriter.write(sid + "," + exif.GPSLatitude + "," + exif.GPSLongitude);
            sWriter.write('\n');

            root.setLocation(sid);

            int maxAnnotations = 10;
            for (String annotation: FileUtils.readLines(new File(annotationsFiles.get(ix)))) {
                if (maxAnnotations-- <= 0) break;
                annotation = annotation.replace('"', ' ').trim();
                if (annotation.length() < 1) continue;
                Candidates.CandidateReference ref =
                        candidateSet.createEntity(Lists.newArrayList(Candidates.NAME_KEY), Lists.newArrayList(annotation));
                root.participants.add(new ContextNetwork.Entity("person", ref.toString()));
            }

            tempNet.addAtomic(root);
            network.eventTrees.add(tempNet.eventTrees.get(0));
            ix++;
        }

        sWriter.close();
        return network;
    }

    public static ContextNetwork createNetwork(long nTimestamp, String locationKey, int event_type, int instance_count, Iterable<String> objects) {
        ContextNetwork net = new ContextNetwork();
        ContextNetwork.Instance root = new ContextNetwork.Instance(event_type, instance_count);
        root.setInterval(nTimestamp, nTimestamp);
        root.setLocation(locationKey);
        for (String ref: objects) root.participants.add(new ContextNetwork.Entity("person", ref));
        net.addAtomic(root);
        return net;
    }

    public static void addToNetwork(ContextNetwork network, ContextNetwork tempNet) {
        network.eventTrees.add(tempNet.eventTrees.get(0));
    }

    public static ContextNetwork loadNetworkForPropagation(String networkDataFilename, SpaceTimeValueGenerators stGenerator) throws IOException {
        LineIterator iter = FileUtils.lineIterator(new File(networkDataFilename));
        ContextNetwork network = new ContextNetwork();

        ContextNetwork tempNet = new ContextNetwork();
        boolean readOneLine = true;
        ContextNetwork.Instance root = null;
        HashMap<Integer,ContextNetwork.Instance> instanceMap = Maps.newHashMap();

        //Iterator<String> locationKeyIterator = stGenerator.getLocationValueIterator();
        long timeRangeStart = 1;
        long timeRangeEnd = 10000;
        //String locationKey = locationKeyIterator.next();
        String locationKey = null;
        long timestamp = stGenerator.getUniformTimestamp(1, 10000);

        while (iter.hasNext()) {

            String line = iter.next();
            if (line.contains("->")) { /* edge */
                String[] parts = line.split(" -> ");
                String[] e1 = parts[0].split(",");
                String[] e2 = parts[1].split(",");

                ContextNetwork.Instance parent = new ContextNetwork.Instance(Integer.parseInt(e1[0]), Integer.parseInt(e1[1]));
                ContextNetwork.Instance child = new ContextNetwork.Instance(Integer.parseInt(e2[0]), Integer.parseInt(e2[1]));

                parent.setInterval(timestamp, timestamp);
                parent.setLocation(locationKey);
                child.setInterval(timestamp, timestamp);
                child.setLocation(locationKey);

                instanceMap.put(Integer.parseInt(e1[0]), parent);
                instanceMap.put(Integer.parseInt(e2[0]), child);

                if (readOneLine) {
                    root = parent;
                    tempNet.addAtomic(root);
                    readOneLine = false;
                }

                tempNet.addSubeventEdge(root, parent, child);
            } else if (line.contains("[")) { /* entity */

                int ix = line.indexOf(',');
                String[] parts = new String[3];
                parts[0] = line.substring(0, ix);
                parts[1] = line.substring(ix + 1);
                int eix = line.indexOf(']');
                parts[2] = line.substring(eix+3);

                ContextNetwork.Instance instance = instanceMap.get(Integer.parseInt(parts[0]));

                BasicDBList list = (BasicDBList) JSON.parse(parts[1]);
                for (Object item: list) {
                    //logger.info(item + " " + instance);
                    instance.participants.add(new ContextNetwork.Entity("person", item.toString()));
                }

                if (parts.length == 3) {
                    timestamp = Long.parseLong(parts[2]);
                    instance.setInterval(timestamp, timestamp);
                }

            } else if (line.contains("=====")) { /* end of object*/
                readOneLine = true;
                network.eventTrees.add(tempNet.eventTrees.get(0));
                tempNet = new ContextNetwork();
                instanceMap = Maps.newHashMap();
                root = null;

                //locationKey = locationKeyIterator.next();
                timestamp = stGenerator.getUniformTimestamp(timeRangeStart, timeRangeEnd);
            }
            else { /* single node */
                String[] e1 = line.split("\\s+");
                root = new ContextNetwork.Instance(Integer.parseInt(e1[0]), Integer.parseInt(e1[1]));
                instanceMap.put(Integer.parseInt(e1[0]), root);
                root.setInterval(timestamp, timestamp);
                root.setLocation(locationKey);
                tempNet.addAtomic(root);
            }

        }

        return network;
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

        long min = Integer.MAX_VALUE, max = -1;
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

    public static void checkTree(ContextNetwork net) {
        checkTree(net, net.eventTrees.get(0), net.eventTrees.get(0).root);
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
        long span = (start.intervalEnd - start.intervalStart) / start.immediateSubevents.size();
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

        long span = (current.intervalEnd - current.intervalStart) / count;
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
