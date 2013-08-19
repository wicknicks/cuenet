package esl.cuenet.generative.structs;

import com.google.common.collect.*;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Propagate {

    private Logger logger = Logger.getLogger(getClass());

    private final ContextNetwork network;
    private final double d = 0.85;

    private Map<ContextNetwork.Instance, Double> eventScoreTable;
    private final Table<Integer, Integer, Integer> semanticDistances;
    private Table<ContextNetwork.Instance, ContextNetwork.Instance, Integer> sparseSubeventCountTable;
    private Table<ContextNetwork.Instance, ContextNetwork.Instance, Integer> sparseObjectCountTable;
    private Multimap<ContextNetwork.Instance, ContextNetwork.Instance> eventsWithinSpatialRange;
    private Multimap<ContextNetwork.Instance, ContextNetwork.Instance> eventsWithinTemporalRange;

    final private int timespan = 10000;
    private final SpaceTimeValueGenerators stGenerators;


    public Propagate(ContextNetwork network, String semanticDistanceFile, SpaceTimeValueGenerators generators) {
        this.network = network;
        this.stGenerators = generators;
        this.semanticDistances = HashBasedTable.create();
        try {
            LineIterator iter = FileUtils.lineIterator(new File(semanticDistanceFile));
            while (iter.hasNext()) {
                String line = iter.next();
                String[] parts = line.split(" -> ");
                int typeId = Integer.parseInt(parts[0]);
                Map distMap = ((BasicDBObject) JSON.parse(parts[1])).toMap();
                for (Object entry: distMap.entrySet()) {
                    Map.Entry e = (Map.Entry) entry;
                    semanticDistances.put(typeId, Integer.parseInt(e.getKey().toString()), (Integer) e.getValue());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void prepare(HashSet<String> entities) {
        int nc = network.nodeCount();
        eventScoreTable = Maps.newHashMapWithExpectedSize(nc);
        sparseSubeventCountTable = HashBasedTable.create(nc, nc);
        sparseObjectCountTable = HashBasedTable.create(nc, nc);
        eventsWithinSpatialRange = HashMultimap.create(nc, 100);
        eventsWithinTemporalRange = HashMultimap.create(nc, 100);

        logger.info("Initializing scores");
        double score;
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                score = 0;
                for (ContextNetwork.Entity person: instance.participants) {
                    if (entities.contains(person.id)) {
                        score++;
                    }
                }
                eventScoreTable.put(instance, score);
            }
        }

        logger.info("Creating nearby index, and counting common subevents");
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            findEventsWithinSTRange(tree);
        }

    }

    private void findEventsWithinSTRange(ContextNetwork.IndexedSubeventTree tree) {
        for (ContextNetwork.IndexedSubeventTree otherTree: network.eventTrees) {

            if (Math.abs(tree.root.intervalStart - otherTree.root.intervalStart) < timespan/20) {

                for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                    for (ContextNetwork.Instance otherInstance: otherTree.instanceMap.values()) {
                        eventsWithinTemporalRange.put(instance, otherInstance);
                        findCommonSubs(tree, instance, otherTree, otherInstance);
                    }
                }

            }
        }

        String thisloc = tree.root.location;
        for (ContextNetwork.IndexedSubeventTree otherTree: network.eventTrees) {

            String otherloc = otherTree.root.location;
            if (stGenerators.distance(thisloc, otherloc) < 50) {

                for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                    for (ContextNetwork.Instance otherInstance: otherTree.instanceMap.values()) {
                        eventsWithinSpatialRange.put(instance, otherInstance);
                        findCommonSubs(tree, instance, otherTree, otherInstance);
                    }
                }

            }
        }

    }

    private void findCommonSubs(ContextNetwork.IndexedSubeventTree tree, ContextNetwork.Instance instance,
                                ContextNetwork.IndexedSubeventTree otree, ContextNetwork.Instance otherInstance) {

        if (sparseSubeventCountTable.contains(instance, otherInstance)) return;

        Set<Integer> subtypesInstance = getSubeventTypes(tree, instance);
        Set<Integer> subtypesOtherInstance = getSubeventTypes(otree, otherInstance);

        int count = Sets.intersection(subtypesInstance, subtypesOtherInstance).size();

        sparseSubeventCountTable.put(instance, otherInstance, count);
        sparseSubeventCountTable.put(otherInstance, instance, count);


        Set<String> objectsInstance = getObjects(tree, instance);
        Set<String> objectsOtherInstance = getObjects(tree, instance);

        count = Sets.intersection(objectsInstance, objectsOtherInstance).size();

        sparseObjectCountTable.put(instance, otherInstance, count);
        sparseObjectCountTable.put(otherInstance, instance, count);
    }

    private int getCommonSubeventCount(ContextNetwork.Instance i, ContextNetwork.Instance j) {
        if ( !sparseSubeventCountTable.contains(i, j) ) return 0;
        return sparseSubeventCountTable.get(i, j);
    }

    private int getCommonObjectCount(ContextNetwork.Instance i, ContextNetwork.Instance j) {
        if ( !sparseObjectCountTable.contains(i, j) ) return 0;
        return sparseObjectCountTable.get(i, j);
    }

    private Set<String> getObjects(ContextNetwork.IndexedSubeventTree tree, ContextNetwork.Instance instance) {
        Set<String> objects = Sets.newHashSet();

        Stack<ContextNetwork.Instance> visited = new Stack<ContextNetwork.Instance>();
        visited.add(instance);

        while ( !visited.empty() ) {
            ContextNetwork.Instance i = visited.pop();
            for (ContextNetwork.InstanceId sub: i.immediateSubevents) {
                ContextNetwork.Instance iSub = tree.instanceMap.get(sub);
                visited.add(iSub);
                for (ContextNetwork.Entity p: iSub.participants) objects.add(p.id);
            }
        }

        return objects;
    }

    private Set<Integer> getSubeventTypes(ContextNetwork.IndexedSubeventTree tree, ContextNetwork.Instance instance) {
        Set<Integer> subTypes = Sets.newHashSet();

        Stack<ContextNetwork.Instance> visited = new Stack<ContextNetwork.Instance>();
        visited.add(instance);

        while ( !visited.empty() ) {
            ContextNetwork.Instance i = visited.pop();
            for (ContextNetwork.InstanceId sub: i.immediateSubevents) {
                visited.add(tree.instanceMap.get(sub));
                subTypes.add(sub.eventId);
            }
        }

        return subTypes;
    }

    public double propagateOnce() {

        return computeDelta();
    }

    private double computeDelta() {
        return 0D;
    }

    public void show() {
        logger.info(network.count() + "; " + network.nodeCount());
        logger.info(semanticDistances.toString());
        dispScores();
    }

    public void dispScores() {
        if (eventScoreTable == null) return;
        logger.info(" --- SCORES ---");
        for (Map.Entry<ContextNetwork.Instance, Double> entry: eventScoreTable.entrySet()) {
            if (entry.getValue() > 0) logger.info(entry.getKey() + " " + entry.getValue());
        }
    }

}
