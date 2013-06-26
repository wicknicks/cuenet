package esl.cuenet.generative.structs;

import java.util.*;

public class ContextNetwork {

    private List<IndexedSubeventTree> eventTrees = new ArrayList<IndexedSubeventTree>();

    public void addAtomic(Instance inst) {
        IndexedSubeventTree tree = new IndexedSubeventTree();
        tree.root = inst;
        tree.typeIndex.put(inst.id.eventId, new HashSet<Instance>());
        eventTrees.add(tree);
    }

    public void addSubeventEdge(Instance root, Instance current, Instance subevent) {
        //find root in eventTrees list
        IndexedSubeventTree subtree = null;
        for (IndexedSubeventTree i: eventTrees)
            if (i.root.equals(root)) subtree = i;

        if (subtree == null) throw new RuntimeException("No corresponding subtree for event " + root);

        if (subtree.typeIndex.containsKey(current.id.eventId)) {
            HashSet<Instance> instances = subtree.typeIndex.get(current.id.eventId);
            instances.add(current);

            Instance temp = null;
            for (Instance i: instances) {
                if (i.equals(current)) temp = i;
            }

            if (temp == null) throw new RuntimeException("Could not find instance " + current);

            temp.immediateSubevents.add(subevent.id);

        } else {
            HashSet<Instance> instances = new HashSet<Instance>();
            subtree.typeIndex.put(current.id.eventId, instances);
            instances.add(current);

            current.immediateSubevents.add(subevent.id);
        }

        HashSet<Instance> subeventInstances;
        if (subtree.typeIndex.containsKey(subevent.id.eventId))
            subeventInstances = subtree.typeIndex.get(subevent.id.eventId);
        else {
            subeventInstances = new HashSet<Instance>();
            subtree.typeIndex.put(subevent.id.eventId, subeventInstances);
        }

        if (subeventInstances.contains(subevent))
            throw new RuntimeException("WTF " + current + " " + subevent);

        subeventInstances.add(subevent);
    }

    public int count() {
        return eventTrees.size();
    }

    public void updateTimeIntervals(Instance root) {
        IndexedSubeventTree temp = null;
        for (int i=eventTrees.size()-1; i>=0; i--) {
            if (eventTrees.get(i).root.equals(root)) {
                temp = eventTrees.get(i);
                break;
            }
        }

        if (temp == null) throw new RuntimeException("Invalid root: " + root);

        updateTimeIntervals(temp, temp.root);

        count();
    }

    private Instance lookup(IndexedSubeventTree root, InstanceId id) {
        HashSet<Instance> instances = root.typeIndex.get(id.eventId);
        for (Instance i: instances) {
            if (i.id.equals(id)) return i;
        }
        throw new NoSuchElementException(id.toString());
    }

    private void updateTimeIntervals(IndexedSubeventTree root, Instance current) {
        int count = current.immediateSubevents.size();
        if (count == 0) return;

        int span = (current.intervalEnd - current.intervalStart) / count;

        int i=0;
        for (InstanceId instanceid: current.immediateSubevents) {
            Instance subevent = lookup(root, instanceid);
            subevent.location = current.location;
            subevent.intervalStart = current.intervalStart + (span * i);
            subevent.intervalEnd = current.intervalStart + (span * (i+1));
            i++;
            updateTimeIntervals(root, subevent);
        }
    }

    private class IndexedSubeventTree {
        Instance root;
        HashMap<Integer, HashSet<Instance>> typeIndex = new HashMap<Integer, HashSet<Instance>>();

        @Override
        public String toString() {
            return root.toString();
        }
    }

    public static class Instance {
        InstanceId id;
        List<InstanceId> immediateSubevents;
        int intervalStart, intervalEnd;
        String location;

        public Instance(int eventId, int instanceId) {
            this.id = new InstanceId(eventId, instanceId);
            this.immediateSubevents = new ArrayList<InstanceId>();
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setInterval(int start, int end) {
            this.intervalStart = start;
            this.intervalEnd = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Instance that = (Instance)o;
            return this.id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public static class InstanceId {
        int eventId;
        int instanceId;

        public InstanceId(int eventId, int instanceId) {
            this.instanceId = instanceId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstanceId that = (InstanceId) o;

            if (eventId != that.eventId) return false;
            if (instanceId != that.instanceId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = eventId;
            result = 31 * result + instanceId;
            return result;
        }

        @Override
        public String toString() {
            return eventId + "_" + instanceId;
        }
    }
}
