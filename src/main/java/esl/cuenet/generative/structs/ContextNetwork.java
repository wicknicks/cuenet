package esl.cuenet.generative.structs;

import java.util.*;

public class ContextNetwork {

    private HashMap<Integer, List<Instance>> instanceMap = new HashMap<Integer, List<Instance>>();
    private int count = 0;

    public void addSubeventEdge(Instance current, Instance superEvent, Instance subInstance) {
        System.out.println(current + " " + superEvent + " " + subInstance);
        if (current.equals(superEvent)) {
            current.subevents.add(subInstance);
            return;
        }
        for (Instance alreadyPresent: current.flatSubeventTree) {
            if (alreadyPresent.equals(superEvent)) {
                alreadyPresent.subevents.add(subInstance);
                return;
            }
        }
        current.flatSubeventTree.add(superEvent);
        superEvent.subevents.add(subInstance);
    }

    //merge single event
    public void addAtomic(Instance instance) {
        List<Instance> instances;
        if (instanceMap.containsKey(instance.eventId)) instances = instanceMap.get(instance.eventId);
        else {
            instances = new ArrayList<Instance>();
            instanceMap.put(instance.eventId, instances);
        }
        instances.add(instance);
        count++;
    }

    //merge an entire network
    public void merge(ContextNetwork tempNetwork) {

    }

    public long count() {
        return count;
    }

    public void updateTimeIntervals(Instance instance) {
        recursivelyUpdateTimeIntervals(instance, instance);
    }

    public void recursivelyUpdateTimeIntervals(Instance current, Instance instance) {
        int length = instance.subevents.size(); //instance.subevents size

        int diff = (instance.intervalStart - instance.intervalEnd)/length;

    }


    public static class Instance {

        final int eventId;
        final int instanceId;

        private HashSet<Instance> flatSubeventTree = new HashSet<Instance>();  //all events which contain a subevent
        private List<Instance> subevents = new ArrayList<Instance>();

        int intervalStart; int intervalEnd;
        String location;

        public Instance(int eventId, int instanceId) {
            this.eventId = eventId;
            this.instanceId = instanceId;
        }

        public String toString() {
            return eventId +  "_" + instanceId;
        }

        public void setInterval(int start, int end) {
            this.intervalStart = start;
            this.intervalEnd = end;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Instance instance = (Instance) o;

            if (eventId != instance.eventId) return false;
            if (instanceId != instance.instanceId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = eventId;
            result = 31 * result + instanceId;
            return result;
        }
    }


}
