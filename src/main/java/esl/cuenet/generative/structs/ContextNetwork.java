package esl.cuenet.generative.structs;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class ContextNetwork {

    protected List<IndexedSubeventTree> eventTrees = new ArrayList<IndexedSubeventTree>();

    public void addAtomic(Instance inst) {
        IndexedSubeventTree tree = new IndexedSubeventTree();
        tree.root = inst;
        HashSet<Instance> instances = new HashSet<Instance>();

        instances.add(inst);
        tree.instanceMap.put(inst.id, inst);

        tree.typeIndex.put(inst.id.eventId, instances);
        eventTrees.add(tree);
    }

    public void addSubeventEdge(Instance root, Instance parent, Instance subevent) {
        //find root in eventTrees list
        IndexedSubeventTree subtree = null;
        for (IndexedSubeventTree i: eventTrees)
            if (i.root == root) subtree = i;     //instance comparison

        if (subtree == null) throw new RuntimeException("No corresponding subtree for event " + root);

        //find parent instance
        if (subtree.typeIndex.containsKey(parent.id.eventId)) {
            HashSet<Instance> instances = subtree.typeIndex.get(parent.id.eventId);
            instances.add(parent);

            Instance temp = subtree.instanceMap.get(parent.id);
            //for (Instance i: instances) {
            //    if (i.equals(parent)) temp = i;
            //}

            if (temp == null) throw new RuntimeException("Could not find instance " + parent);

            if ( !temp.immediateSubevents.contains(subevent.id) ) temp.immediateSubevents.add(subevent.id);

        } else {
            HashSet<Instance> instances = new HashSet<Instance>();
            subtree.typeIndex.put(parent.id.eventId, instances);
            instances.add(parent);

            parent.immediateSubevents.add(subevent.id);
        }

        //all new subevents must be added to typeindex
        HashSet<Instance> subeventInstances;
        if (subtree.typeIndex.containsKey(subevent.id.eventId))
            subeventInstances = subtree.typeIndex.get(subevent.id.eventId);
        else {
            subeventInstances = new HashSet<Instance>();
            subtree.typeIndex.put(subevent.id.eventId, subeventInstances);
        }

        if (subeventInstances.contains(subevent)) {
//            System.out.println("subevent already exists: " + current + " " + subevent);
        }
        else {
            subeventInstances.add(subevent);
        }

        if ( !subtree.instanceMap.containsKey(parent.id) ) subtree.instanceMap.put(parent.id, parent);
        if ( !subtree.instanceMap.containsKey(subevent.id) ) subtree.instanceMap.put(subevent.id, subevent);
    }

    public int count() {
        return eventTrees.size();
    }

    public int nodeCount() {
        int count = 0;
        for (IndexedSubeventTree e: eventTrees)
            count += e.nodeCount();
        return count;
    }

    protected Instance lookup(IndexedSubeventTree root, InstanceId id) {
        if ( !root.instanceMap.containsKey(id) )
            throw new NoSuchElementException(root.toString() + " " + id.toString());
        return root.instanceMap.get(id);

//        HashSet<Instance> instances = root.typeIndex.get(id.eventId);
//        if (instances == null) throw new NullPointerException(id.toString());
//        for (Instance i: instances) {
//            if (i.id.equals(id)) return i;
//        }
//        throw new NoSuchElementException(root.toString() + " " + id.toString());
    }


    public void merge (ContextNetwork other) {
        if (this.eventTrees.size() == 0) {
            this.eventTrees.addAll(other.eventTrees);
            return;
        }
        for (IndexedSubeventTree thisSub: this.eventTrees)
            for (IndexedSubeventTree otherTree: other.eventTrees)
                descendAndMerge(thisSub, otherTree);

//        for (IndexedSubeventTree otherTree: other.eventTrees) merge(otherTree);
    }

    private void descendAndMerge(IndexedSubeventTree subtree, IndexedSubeventTree other) {
        if (!STHelper.lequals(subtree.root, other.root)) return;

        if (STHelper.tequals(subtree.root, other.root)) {
            mergeInformation(subtree.root, other.root);
            for (InstanceId oSubeventId: other.root.immediateSubevents) {
                Instance s = lookup(other, oSubeventId);
                recursiveMerge(subtree, subtree.root, other, s);
            }
        }
        else if (STHelper.contains(subtree.root, other.root)) {
            recursiveMerge(subtree, subtree.root, other, other.root);
        } else if (STHelper.contains(other.root, subtree.root)) {
            Instance new_root = other.root.attributeClone();
            Instance old_root = subtree.root;
            subtree.root = new_root;
            if ( !subtree.instanceMap.containsKey(new_root.id) ) subtree.instanceMap.put(new_root.id, new_root);
            addSubeventEdge(subtree.root, subtree.root, old_root);
//            System.out.println("new root " + other.root + " " + subtree.root);
            for (InstanceId oSubeventId: other.root.immediateSubevents) {
                Instance s = lookup(other, oSubeventId);
                recursiveMerge(subtree, subtree.root, other, s);
            }
        }
    }

    //the parent is guranteed to contain instance. RM will decide to either
    // (a) add as a subevent to parent
    // (b) push instance down to one of its immediate subevents.
    // (c) insert between parent and one of its immediate subevents.
    private void recursiveMerge(IndexedSubeventTree subtree, Instance parent,
                                IndexedSubeventTree other, Instance instance) {
        boolean addAsSubevent = true;

        if ( !STHelper.lequals(parent, instance) ) return;

        List<Instance> containedSubevents = new ArrayList<Instance>();

        //check if any of the immediate subevents accepts instance
        for (InstanceId subs: parent.immediateSubevents) {
            Instance subevent = lookup(subtree, subs);
            if (STHelper.tequals(subevent, instance)) {
                addAsSubevent = false;
                mergeInformation(subevent, instance);
                for (InstanceId osubs: instance.immediateSubevents) {
                    Instance s = lookup(other, osubs);
                    recursiveMerge(subtree, subevent, other, s);
                }
                break;
            }
            else if (STHelper.contains(subevent, instance)) {
                addAsSubevent = false;
                recursiveMerge(subtree, subevent, other, instance);
                break;
            }
            else if (STHelper.contains(instance, subevent)) {
                addAsSubevent = false;
                containedSubevents.add(subevent);

//                Instance new_i = instance.attributeClone();
//
//                parent.immediateSubevents.remove(subevent.id);
//
//                addSubeventEdge(subtree.root, parent, new_i);
//                addSubeventEdge(subtree.root, new_i, subevent);
//
//                for (InstanceId osubs: instance.immediateSubevents) {
//                    Instance s = lookup(other, osubs);
//                    recursiveMerge(subtree, new_i, other, s);
//                }
//                break;
            }
        }

        if (containedSubevents.size() > 0) {
            Instance new_i = instance.attributeClone();
            addSubeventEdge(subtree.root, parent, new_i);
            for (Instance c_subevent: containedSubevents) {
                parent.immediateSubevents.remove(c_subevent.id);
                addSubeventEdge(subtree.root, new_i, c_subevent);
            }
            for (InstanceId osubs: instance.immediateSubevents) {
                Instance s = lookup(other, osubs);
                recursiveMerge(subtree, new_i, other, s);
            }
        }

        if (addAsSubevent) {
            Instance new_i = instance.attributeClone();
            addSubeventEdge(subtree.root, parent, new_i);
            for (InstanceId osubs: instance.immediateSubevents) {
                Instance s = lookup(other, osubs);
                recursiveMerge(subtree, new_i, other, s);
            }
        }
    }

    private void mergeInformation(Instance thisSubevent, Instance otherInstance) {
        //System.out.println("[merge_info] " + thisSubevent + " " + otherInstance);
        if ( !thisSubevent.id.equals(otherInstance.id) ) {
            return;
        }
    }

    public void printTree() {
        printTree(false);
    }

    public void printTree(boolean printEntities) {
        for (IndexedSubeventTree etree: eventTrees)
            etree.print(System.out, printEntities);
    }

    public void printTree(int root_eventid, int root_instanceid) {
        for (IndexedSubeventTree etree: eventTrees)
            if (etree.root.id.eventId == root_eventid && etree.root.id.instanceId == root_instanceid)
                etree.print(System.out, true);
    }

    public boolean compareNetwork(ContextNetwork other) {
        for (IndexedSubeventTree tree: eventTrees) {
            boolean flag = false;
            for (IndexedSubeventTree otherTree: other.eventTrees) {
                if (otherTree.compareTree(tree)) {
                    flag = true;
                    break;
                }
            }
            if ( !flag )
                return false;
        }
        return true;
    }

    public void pruneUp() {
        for (IndexedSubeventTree e: eventTrees) pruneUp(e, e.root);
    }

    private Set<Entity> pruneUp(IndexedSubeventTree e, Instance parent) {

        Set<Entity> entities = new HashSet<Entity>();

        List<Entity> participants = parent.participants;
        for (InstanceId sub: parent.immediateSubevents) {
            Instance is = lookup(e, sub);
            Set<Entity> entitiesSeenSoFar = pruneUp(e, is);
            for (Entity es: entitiesSeenSoFar) {
                if (participants.contains(es)) participants.remove(es);
            }
            entities.addAll(entitiesSeenSoFar);
        }

        entities.addAll(parent.participants);
        return entities;
    }


    public class IndexedSubeventTree {
        public Instance root;
        public HashMap<Integer, HashSet<Instance>> typeIndex = new HashMap<Integer, HashSet<Instance>>();
        public HashMap<InstanceId, Instance> instanceMap = new HashMap<InstanceId, Instance>();

        @Override
        public String toString() {
            return root.toString();
        }

        public void print(PrintStream out, boolean printEntities) {
            try {
                out.write(("ROOT " + root + "\n").getBytes());
                Stack<InstanceId> stack = new Stack<InstanceId>();
                stack.add(root.id);
                while (!stack.isEmpty()) {
                    InstanceId i = stack.pop();
                    Instance inst = lookup(this, i);
                    if ( printEntities ) {
                        inst.print(out);
                    }
                    for (InstanceId subids: inst.immediateSubevents) {
                        out.write(i.toString().getBytes());
                        out.write(" -> ".getBytes());
                        out.write(subids.toString().getBytes());
                        out.write("\n".getBytes());
                        stack.add(subids);
                    }
                }
                out.write(".\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int nodeCount() {
            int count = 0;
            for (int i: this.typeIndex.keySet())
                count += this.typeIndex.get(i).size();
            return count;
        }

        public boolean compareTree(IndexedSubeventTree other) {
            if ( !this.root.equals(other.root) ) {
                return false;
            }
            if ( this.typeIndex.size() != other.typeIndex.size()) {
                return false;
            }
            if ( !this.typeIndex.keySet().containsAll(other.typeIndex.keySet()) ) {
                return false;
            }

            for (Integer thiskey: this.typeIndex.keySet()) {
                HashSet<Instance> thisvalues = this.typeIndex.get(thiskey);
                for (Instance thisinstance: thisvalues) {
                    if ( !other.instanceMap.containsKey(thisinstance.id) )
                        return false;
                    if ( !thisinstance.compareInstance(other.instanceMap.get(thisinstance.id)) )
                        return false;

                    Instance thatinstance = other.instanceMap.get(thisinstance.id);
                    if ( thisinstance.immediateSubevents.size() != thatinstance.immediateSubevents.size())
                        return false;
                    if ( !thisinstance.immediateSubevents.containsAll(thatinstance.immediateSubevents))
                        return false;
                }

//                HashSet<Instance> othervalues = other.typeIndex.get(thiskey);
//                if ( !thisvalues.containsAll(othervalues) )  {
//                    System.out.println("thisvalues");
//                    return false;
//                }
//
//                for (Instance thisinstance: thisvalues) {
//                    boolean flag = true;
//                    for (Instance thatinstance: othervalues) {
//                        if (thatinstance.equals(thisinstance)) {
//                            flag = false;
//                            if ( !thatinstance.immediateSubevents.containsAll(thisinstance.immediateSubevents) ) {
//                                System.out.println("thatvalues");
//                                return false;
//                            }
//
//                            break;
//                        }
//                    }
//                    if ( flag ) {
//                        System.out.println("just flag");
//                        return false;
//                    }
//                }
            }
            return true;
        }
    }

    public static class Instance {
        protected final InstanceId id;
        public List<InstanceId> immediateSubevents;
        protected List<Entity> participants;
        protected long intervalStart, intervalEnd;
        protected String location;

        public Instance(int eventId, int instanceId) {
            this.id = new InstanceId(eventId, instanceId);
            this.immediateSubevents = new ArrayList<InstanceId>();
            this.participants = new ArrayList<Entity>();
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setInterval(long start, long end) {
            this.intervalStart = start;
            this.intervalEnd = end;
        }

        // create a clone of this instance without the immediate subevents.
        public Instance attributeClone() {
            Instance instance = this;
            Instance new_i = new Instance(instance.id.eventId, instance.id.instanceId);
            new_i.setInterval(instance.intervalStart, instance.intervalEnd);
            new_i.setLocation(instance.location);
            return new_i;
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

        public boolean compareInstance(Instance other) {
            if (this.intervalStart != other.intervalStart)
                return false;
            if (this.intervalEnd != other.intervalEnd)
                return false;
            if ( !this.location.equals(other.location) )
                return false;
            return true;
        }

        public void print(PrintStream out) throws IOException {
            out.write(this.toString().getBytes());
            out.write(" : ".getBytes());
            out.write(Arrays.toString(this.participants.toArray()).getBytes());
            out.write("\n".getBytes());
        }
    }

    public static class InstanceId {
        public int eventId;
        public int instanceId;

        public InstanceId(int eventId, int instanceId) {
            this.instanceId = instanceId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstanceId that = (InstanceId) o;

            return eventId == that.eventId && instanceId == that.instanceId;
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

    public static class Entity {
        protected final String type;  //person, organization, company or place
        protected final String id;    //arjun, uci, starbucks corp or "Mason Park"

        public Entity(String type, String id) {
            this.id = id;
            this.type = type;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return type + "_" + id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entity that = (Entity) o;

            return id.equals(that.id) && type.equals(that.type);
        }
    }



}
