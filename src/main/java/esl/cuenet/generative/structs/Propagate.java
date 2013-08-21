package esl.cuenet.generative.structs;

import com.google.common.collect.*;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    private long timespan;
    private final SpaceTimeValueGenerators stGenerators;
    private final int maxSemanticDistance;
    private int maxCommonSubevents = 0;

    private Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> scoreTable = HashBasedTable.create();
    private HashSet<ContextNetwork.Entity> allEntites;



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
            e.printStackTrace();
        }

        int maxDistance = -1;
        for (Table.Cell<Integer, Integer, Integer> cell: semanticDistances.cellSet()) {
            if (cell.getValue() > maxDistance) maxDistance = cell.getValue();
        }

        maxSemanticDistance = maxDistance;

        long mintime = Long.MAX_VALUE, maxtime = Long.MIN_VALUE;
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            if (tree.root.intervalStart < mintime) mintime = tree.root.intervalStart;
            if (tree.root.intervalStart > maxtime) maxtime = tree.root.intervalStart;
        }

        timespan = maxtime - mintime;
        logger.info("Timespan = " + timespan);

        logger.info("MaxSemanticDistance = " + maxDistance);
    }

    public void prepare(HashSet<String> entities) {
        int nc = network.nodeCount();
        eventScoreTable = Maps.newHashMapWithExpectedSize(nc);
        sparseSubeventCountTable = HashBasedTable.create(nc, nc);
        sparseObjectCountTable = HashBasedTable.create(nc, nc);
        eventsWithinSpatialRange = HashMultimap.create(nc, 100);
        eventsWithinTemporalRange = HashMultimap.create(nc, 100);

        allEntites = new HashSet<ContextNetwork.Entity>();

        logger.info("Initializing scores");
        double score;
        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                score = 0;
                for (ContextNetwork.Entity person: instance.participants) {
                    allEntites.add(person);
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

        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                for (ContextNetwork.Entity entity: allEntites) {
                    scoreTable.put(instance, entity, 0.0);
                }
            }
        }

        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                for (ContextNetwork.Entity entity: instance.participants) {
                    scoreTable.put(instance, entity, 1.0);
                }
            }
        }

        logger.info(scoreTable.size());

    }

    private int gzCount() {
        int a=0;
        for (double v: eventScoreTable.values()) if (v > 0) a++;
        logger.info("Events with score > 0 = " + a);
        return a;
    }

    private void findEventsWithinSTRange(ContextNetwork.IndexedSubeventTree tree) {
        for (ContextNetwork.IndexedSubeventTree otherTree: network.eventTrees) {
            if (otherTree == tree) continue;
            if (Math.abs(tree.root.intervalStart - otherTree.root.intervalStart) < (50 * timespan/100)) {

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
            if (otherTree == tree) continue;
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
        if (count > maxCommonSubevents) maxCommonSubevents = count;

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

    public double propagateOnceTable() {
        Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> timeScore = temporalPropagationTable();
        //Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> typeScore = typePropagationTable();

        Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> newScore = HashBasedTable.create();

        double score; Double temp;
        for (Table.Cell<ContextNetwork.Instance, ContextNetwork.Entity, Double> cell: scoreTable.cellSet()) {
            temp = timeScore.get(cell.getRowKey(), cell.getColumnKey());
            score = (temp == null? 0 : temp);
            newScore.put(cell.getRowKey(), cell.getColumnKey(), d * score);
        }

        double delta = computeDeltaTable(scoreTable, newScore);
        scoreTable = newScore;
        return delta;
    }

    private double computeDeltaTable(Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> scoreTable,
                                     Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> newScore) {

        double l1norm = -1;
        for (ContextNetwork.Instance instance: scoreTable.rowKeySet()) {
            Map<ContextNetwork.Entity, Double> original = scoreTable.row(instance);
            Map<ContextNetwork.Entity, Double> updates = newScore.row(instance);
            double l1 = 0;
            for (ContextNetwork.Entity object: original.keySet()) {
                l1 += Math.abs(original.get(object) - updates.get(object));
            }
            l1norm = Math.max(l1norm, l1);
            //logger.info("Delta " + instance + " " + l1);
        }

        return l1norm;
    }

    public double propagateOnce() {
        Map<ContextNetwork.Instance, Double> timeScore = temporalPropagation();
        //Map<ContextNetwork.Instance, Double> spatialScore = spatialPropagation();
        Map<ContextNetwork.Instance, Double> objectScore = objectPropagation();
        Map<ContextNetwork.Instance, Double> typeScore = typePropagation();
        Map<ContextNetwork.Instance, Double> structuralScore = structuralPropagation();

        Map<ContextNetwork.Instance, Double> newScore = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        for (ContextNetwork.Instance item: eventScoreTable.keySet()) {
            double score = 0;
            score += timeScore.get(item);
            //score += spatialScore.get(item);
            //score += objectScore.get(item);
            //score += typeScore.get(item);
            //score += structuralScore.get(item);
            newScore.put(item, d * score);
        }

        double delta = computeDelta(eventScoreTable, newScore);
        eventScoreTable = newScore;

        return delta;
    }

    private Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> temporalPropagationTable() {
        Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> newScore = HashBasedTable.create();

        long range = 5 * timespan/100;
        long diff; double fraction, ns;

        HashMap<ContextNetwork.Instance, Integer> neighborCount = Maps.newHashMap();
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            int n = 0;
            for (ContextNetwork.Instance neighbor: eventScoreTable.keySet()) {
                if (instance == neighbor) continue;
                diff = Math.abs(instance.intervalStart - neighbor.intervalStart);
                if (diff > range) continue;
                n++;
            }
            neighborCount.put(instance, n);
        }


        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            int numOfNeighbors = neighborCount.get(instance);
            if (numOfNeighbors == 0) continue;
            for (ContextNetwork.Instance neighbor: eventScoreTable.keySet()) {
                if (instance == neighbor) continue;

                diff = Math.abs(instance.intervalStart - neighbor.intervalStart);
                if (diff > range) continue;

                fraction = 1 - (double)diff/range;

                Map<ContextNetwork.Entity, Double> scoresAtInstance = scoreTable.row(instance);

                for(Map.Entry<ContextNetwork.Entity, Double> entry: scoresAtInstance.entrySet()) {

                    if (Double.compare(entry.getValue(), 0) == 0) continue;
                    //ns = scoreTable.get(neighbor, entry.getKey());
                    //ns = ns + fraction * entry.getValue();
                    //if (ns > 1) ns = 1;

                    ns = fraction * entry.getValue() / numOfNeighbors;

                    newScore.put(neighbor, entry.getKey(), ns);
                }
            }
        }

        return newScore;
    }

    private Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> typePropagationTable() {
        Table<ContextNetwork.Instance, ContextNetwork.Entity, Double> newScore = HashBasedTable.create();
        return newScore;
    }

    private Map<ContextNetwork.Instance, Double> temporalPropagation() {
        Map<ContextNetwork.Instance, Double> scores = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        double sum;
        double intervalDiff;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            sum = 0;
            for (ContextNetwork.Instance neighbor: eventsWithinTemporalRange.get(instance)) {
                double neighScore = eventScoreTable.get(neighbor);
                if (Double.compare(neighScore, 0) == 0) continue;
                 intervalDiff = 1- (Math.abs((double)instance.intervalStart - neighbor.intervalStart) / timespan);

                neighScore = neighScore * intervalDiff;  //linear drop
                neighScore /= eventsWithinTemporalRange.get(neighbor).size(); //normalize using temporal fanout from each neighbor

                sum += neighScore;
            }

            scores.put(instance, sum);
        }

        return scores;
    }

    private Map<ContextNetwork.Instance, Double> spatialPropagation() {
        Map<ContextNetwork.Instance, Double> scores = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        double sum;
        double spatialDiff;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            sum = 0;
            for (ContextNetwork.Instance neighbor: eventsWithinSpatialRange.get(instance)) {
                double neighScore = eventScoreTable.get(neighbor);
                if (neighScore == 0) continue;

                spatialDiff = 1 - (stGenerators.distance(instance.location, neighbor.location))/stGenerators.getMaxDist();
                neighScore = neighScore * spatialDiff;
                neighScore /= eventsWithinSpatialRange.get(neighbor).size();

                sum += neighScore;
            }

            //logger.info(instance + " " + sum);
            scores.put(instance, sum);
        }

        return scores;
    }

    private Map<ContextNetwork.Instance, Double> objectPropagation() {
        Map<ContextNetwork.Instance, Double> scores = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        double sum;
        double scoreDiff;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            sum = 0;
            for (ContextNetwork.Instance neighbor: sparseObjectCountTable.row(instance).keySet()) {
                scoreDiff = sparseObjectCountTable.get(instance, neighbor);
                scoreDiff = scoreDiff / sparseObjectCountTable.row(neighbor).size();
                sum += scoreDiff;
            }

            //logger.info(instance + " " + sum);
            scores.put(instance, sum);
        }

        return scores;
    }

    private Map<ContextNetwork.Instance, Double> typePropagation() {
        Map<ContextNetwork.Instance, Double> scores = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        double sum;
        double scoreDiff;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            sum = 0;
            for (ContextNetwork.Instance neighbor: sparseObjectCountTable.row(instance).keySet()) {
                scoreDiff = 0;
                if (maxCommonSubevents > 0)
                    scoreDiff = (double) getCommonSubeventCount(instance, neighbor) / maxCommonSubevents;
                scoreDiff += (double) semanticDistances.get(instance.id.eventId, neighbor.id.eventId) / maxSemanticDistance;
                sum = scoreDiff/2;
            }

            //if (sum > 0) logger.info(instance + " " + sum);
            scores.put(instance, sum);
        }

        return scores;
    }

    private Map<ContextNetwork.Instance, Double> structuralPropagation() {
        Map<ContextNetwork.Instance, Double> scores = Maps.newHashMapWithExpectedSize(eventScoreTable.size());

        double sum = 0;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            scores.put(instance, sum);
        }

        return scores;
    }

    private double computeDelta(Map<ContextNetwork.Instance, Double> eventScoreTable, Map<ContextNetwork.Instance, Double> newScore) {
        double totalDiff = 0;
        for (ContextNetwork.Instance instance: eventScoreTable.keySet()) {
            totalDiff += Math.abs(eventScoreTable.get(instance) - newScore.get(instance));
        }
        return totalDiff;
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

    public List<Map.Entry<String,Double>> orderObjects() {
        Set<Map.Entry<ContextNetwork.Instance,Double>> entrySet = eventScoreTable.entrySet();

        PriorityQueue<Map.Entry<ContextNetwork.Instance,Double>> ballot =
                new PriorityQueue<Map.Entry<ContextNetwork.Instance,Double>>(entrySet.size(), new Comparator<Map.Entry<ContextNetwork.Instance,Double>>() {

                    @Override
                    public int compare(Map.Entry<ContextNetwork.Instance,Double> o1, Map.Entry<ContextNetwork.Instance,Double> o2) {
                        if (o2.getValue() > o1.getValue()) return 1;
                        if (o2.getValue() < o1.getValue()) return -1;
                        return 0;
                    }

                });

        ballot.addAll(entrySet);

        List<Map.Entry<ContextNetwork.Instance,Double>> sortedList = Lists.newArrayList();
        while ( !ballot.isEmpty() ) {
            Map.Entry<ContextNetwork.Instance,Double> entry = ballot.remove();
            if (entry.getValue() > 0) sortedList.add(entry);
        }

        HashMap<String, Double> objectScoreTable = new HashMap<String, Double>();

        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees)
            for (ContextNetwork.Instance instance: tree.instanceMap.values())
                for (ContextNetwork.Entity e: instance.participants)
                    objectScoreTable.put(e.id, 0D);


        for (ContextNetwork.IndexedSubeventTree tree: network.eventTrees) {
            for (ContextNetwork.Instance instance: tree.instanceMap.values()) {
                double score = eventScoreTable.get(instance);
                int size = instance.participants.size();
                if (size == 0) continue;
                for (ContextNetwork.Entity e: instance.participants) {
                    double s = objectScoreTable.get(e.id);
                    s += score/size;
                    objectScoreTable.put(e.id, s);
                }
            }
        }

        PriorityQueue<Map.Entry<String, Double>> objBallot =
                new PriorityQueue<Map.Entry<String, Double>>(entrySet.size(), new Comparator<Map.Entry<String, Double>>() {

                    @Override
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        if (o2.getValue() > o1.getValue()) return 1;
                        if (o2.getValue() < o1.getValue()) return -1;
                        return 0;
                    }

                });

        objBallot.addAll(objectScoreTable.entrySet());

        List<Map.Entry<String, Double>> list = Lists.newArrayList();
        while ( !objBallot.isEmpty() ) {
            Map.Entry<String, Double> entry = objBallot.remove();
            if (entry.getValue() > 0) list.add(entry);
        }

        return list;

    }

    public void printScores(int event_id, int instance_id) {
        Map<ContextNetwork.Entity, Double> map = scoreTable.row(new ContextNetwork.Instance(event_id, instance_id));
        PriorityQueue<Map.Entry<ContextNetwork.Entity, Double>> objBallot = new PriorityQueue<Map.Entry<ContextNetwork.Entity, Double>>(map.entrySet().size(),
                new Comparator<Map.Entry<ContextNetwork.Entity, Double>>() {
            @Override
            public int compare(Map.Entry<ContextNetwork.Entity, Double> o1, Map.Entry<ContextNetwork.Entity, Double> o2) {
                if (o2.getValue() > o1.getValue()) return 1;
                if (o2.getValue() < o1.getValue()) return -1;
                return 0;
            }
        });

        objBallot.addAll(map.entrySet());


        while ( !objBallot.isEmpty() ) {
            Map.Entry<ContextNetwork.Entity, Double> entry = objBallot.remove();
            if (entry.getValue() > 0) logger.info(entry.getKey() + "\t" + entry.getValue());
        }

//        for (Map.Entry<ContextNetwork.Entity, Double> entry: map.entrySet()) {
//            if (entry.getValue() > 0) logger.info(entry.getKey() + "\t" + entry.getValue());
//        }
    }
}
